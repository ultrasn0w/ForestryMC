/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture.tiles;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.IAlleleTreeSpecies;
import forestry.api.arboriculture.IFruitProvider;
import forestry.api.arboriculture.ILeafSpriteProvider;
import forestry.api.arboriculture.ILeafTickHandler;
import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.ITreeGenome;
import forestry.api.arboriculture.ITreekeepingMode;
import forestry.api.arboriculture.TreeManager;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IFruitBearer;
import forestry.api.genetics.IFruitFamily;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IPollinatable;
import forestry.api.lepidopterology.ButterflyManager;
import forestry.api.lepidopterology.IButterfly;
import forestry.api.lepidopterology.IButterflyGenome;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.arboriculture.genetics.TreeDefinition;
import forestry.arboriculture.genetics.alleles.AlleleFruit;
import forestry.arboriculture.network.IRipeningPacketReceiver;
import forestry.arboriculture.network.packets.PacketRipeningUpdate;
import forestry.core.network.DataInputStreamForestry;
import forestry.core.network.DataOutputStreamForestry;
import forestry.core.network.packets.PacketTileStream;
import forestry.core.proxy.Proxies;
import forestry.core.render.TextureManager;
import forestry.core.utils.ColourUtil;
import forestry.core.utils.GeneticsUtil;

public class TileLeaves extends TileTreeContainer implements IPollinatable, IFruitBearer, IButterflyNursery, IRipeningPacketReceiver {

	private int colourFruits;

	private short textureIndexFruits = -1;
	private IAlleleTreeSpecies species;

	private boolean isFruitLeaf;
	private boolean isPollinatedState;
	private int ripeningTime;
	private short ripeningPeriod = Short.MAX_VALUE - 1;

	private int maturationTime;
	private int damage;

	private IEffectData effectData[] = new IEffectData[2];

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		ripeningTime = nbttagcompound.getShort("RT");
		damage = nbttagcompound.getInteger("ENC");

		if (nbttagcompound.hasKey("CATER")) {
			maturationTime = nbttagcompound.getInteger("CATMAT");
			caterpillar = ButterflyManager.butterflyRoot.getMember(nbttagcompound.getCompoundTag("CATER"));
		}

		ITree tree = getTree();
		if (tree != null) {
			setTree(tree);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		nbtTagCompound.setInteger("RT", getRipeningTime());
		nbtTagCompound.setInteger("ENC", damage);

		if (caterpillar != null) {
			nbtTagCompound.setInteger("CATMAT", maturationTime);

			NBTTagCompound caterpillarNbt = new NBTTagCompound();
			caterpillar.writeToNBT(caterpillarNbt);
			nbtTagCompound.setTag("CATER", caterpillarNbt);
		}
	}

	@Override
	public void onBlockTick() {
		ITree tree = getTree();
		if (tree == null) {
			return;
		}

		ITreeGenome genome = tree.getGenome();

		boolean isDestroyed = isDestroyed(tree, damage);
		for (ILeafTickHandler tickHandler : genome.getPrimary().getRoot().getLeafTickHandlers()) {
			if (tickHandler.onRandomLeafTick(tree, worldObj, getPos(), isDestroyed)) {
				return;
			}
		}

		if (isDestroyed) {
			return;
		}

		if (damage > 0) {
			damage--;
		}

		if (hasFruit() && getRipeningTime() < ripeningPeriod) {
			ITreekeepingMode treekeepingMode = TreeManager.treeRoot.getTreekeepingMode(worldObj);
			float sappinessModifier = treekeepingMode.getSappinessModifier(genome, 1f);
			float sappiness = genome.getSappiness() * sappinessModifier;

			if (worldObj.rand.nextFloat() < sappiness) {
				ripeningTime++;
				sendNetworkUpdateRipening();
			}
		}

		if (caterpillar != null) {
			matureCaterpillar();
		}

		effectData = tree.doEffect(effectData, worldObj, getPos());
	}

	@Override
	public void setTree(ITree tree) {
		super.setTree(tree);

		ITreeGenome genome = tree.getGenome();
		species = genome.getPrimary();

		if (tree.canBearFruit()) {
			IFruitProvider fruitProvider = genome.getFruitProvider();

			isFruitLeaf = fruitProvider.markAsFruitLeaf(genome, worldObj, getPos());
			if (isFruitLeaf) {
				// Hardcoded because vanilla oak trees don't show fruits.
				if (species == TreeDefinition.Oak.getGenome().getPrimary() && fruitProvider == AlleleFruit.fruitApple.getProvider()) {
					textureIndexFruits = -1;
				} else {
					textureIndexFruits = fruitProvider.getSpriteIndex(genome, worldObj, getPos(), getRipeningTime(), true);
				}

				ripeningPeriod = (short) tree.getGenome().getFruitProvider().getRipeningPeriod();
			}
		} else {
			isFruitLeaf = false;
			textureIndexFruits = -1;
		}

		markDirty();
	}

	/* INFORMATION */
	private static boolean isDestroyed(ITree tree, int damage) {
		if (tree == null) {
			return false;
		}
		return damage > tree.getResilience();
	}

	@Override
	public boolean isPollinated() {
		ITree tree = getTree();
		return tree != null && !isDestroyed(tree, damage) && tree.getMate() != null;
	}

	public int getFoliageColour(EntityPlayer player) {
		final boolean showPollinated = isPollinatedState && GeneticsUtil.hasNaturalistEye(player);
		final int baseColor = getLeafSpriteProvider().getColor(showPollinated);

		ITree tree = getTree();
		if (isDestroyed(tree, damage)) {
			return ColourUtil.addRGBComponents(baseColor, 92, 61, 0);
		} else if (caterpillar != null) {
			return ColourUtil.multiplyRGBComponents(baseColor, 1.5f);
		} else {
			return baseColor;
		}
	}

	public int getFruitColour() {
		if (colourFruits == 0 && hasFruit()) {
			colourFruits = determineFruitColour();
		}
		return colourFruits;
	}

	private int determineFruitColour() {
		ITree tree = getTree();
		ITreeGenome genome = tree.getGenome();
		IFruitProvider fruit = genome.getFruitProvider();
		return fruit.getColour(genome, worldObj, getPos(), getRipeningTime());
	}

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getLeaveSprite(boolean fancy) {
		final ILeafSpriteProvider leafSpriteProvider = getLeafSpriteProvider();
		return leafSpriteProvider.getSprite(isPollinatedState, fancy);
	}

	@Nonnull
	private ILeafSpriteProvider getLeafSpriteProvider() {
		if (species != null) {
			return species.getLeafSpriteProvider();
		} else {
			IAlleleTreeSpecies oakSpecies = TreeDefinition.Oak.getIndividual().getGenome().getPrimary();
			return oakSpecies.getLeafSpriteProvider();
		}
	}

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getFruitSprite() {
		if (textureIndexFruits >= 0) {
			return TextureManager.getInstance().getSprite(textureIndexFruits);
		} else {
			return null;
		}
	}

	public int getRipeningTime() {
		return ripeningTime;
	}

	/* IPOLLINATABLE */
	@Override
	public EnumSet<EnumPlantType> getPlantType() {
		if (getTree() == null) {
			return EnumSet.noneOf(EnumPlantType.class);
		}

		return getTree().getPlantTypes();
	}

	@Override
	public boolean canMateWith(ITree individual) {
		if (getTree() == null) {
			return false;
		}
		if (getTree().getMate() != null) {
			return false;
		}

		return !getTree().isGeneticEqual(individual);
	}

	@Override
	public void mateWith(ITree individual) {
		if (getTree() == null) {
			return;
		}

		getTree().mate(individual);
		worldObj.markBlockForUpdate(getPos());
	}

	@Override
	public ITree getPollen() {
		return getTree();
	}

	public String getUnlocalizedName() {
		ITree tree = getTree();
		if (tree == null) {
			return "for.leaves.corrupted";
		}
		return tree.getGenome().getPrimary().getUnlocalizedName();
	}

	/* NETWORK */
	private void sendNetworkUpdate() {
		Proxies.net.sendNetworkPacket(new PacketTileStream(this), worldObj);
	}

	private void sendNetworkUpdateRipening() {
		int newColourFruits = determineFruitColour();
		if (newColourFruits == colourFruits) {
			return;
		}
		colourFruits = newColourFruits;

		PacketRipeningUpdate ripeningUpdate = new PacketRipeningUpdate(this);
		Proxies.net.sendNetworkPacket(ripeningUpdate, worldObj);
	}

	private static final short hasFruitFlag = 1;
	private static final short isPollinatedFlag = 1 << 1;

	@Override
	public void writeData(DataOutputStreamForestry data) throws IOException {
		super.writeData(data);

		byte leafState = 0;
		boolean hasFruit = hasFruit();

		if (isPollinated()) {
			leafState |= isPollinatedFlag;
		}

		if (hasFruit) {
			leafState |= hasFruitFlag;
		}

		data.writeByte(leafState);

		if (hasFruit) {
			String fruitAlleleUID = getTree().getGenome().getActiveAllele(EnumTreeChromosome.FRUITS).getUID();
			int colourFruits = getFruitColour();

			data.writeUTF(fruitAlleleUID);
			data.writeInt(colourFruits);
		}
	}

	@Override
	public void readData(DataInputStreamForestry data) throws IOException {

		String speciesUID = data.readUTF(); // this is called instead of super.readData, be careful!

		byte leafState = data.readByte();
		isFruitLeaf = (leafState & hasFruitFlag) > 0;
		isPollinatedState = (leafState & isPollinatedFlag) > 0;
		String fruitAlleleUID = null;

		if (isFruitLeaf) {
			fruitAlleleUID = data.readUTF();
			colourFruits = data.readInt();
		}

		IAllele[] treeTemplate = TreeManager.treeRoot.getTemplate(speciesUID);

		if (fruitAlleleUID != null) {
			IAllele fruitAllele = AlleleManager.alleleRegistry.getAllele(fruitAlleleUID);
			if (fruitAllele != null) {
				treeTemplate[EnumTreeChromosome.FRUITS.ordinal()] = fruitAllele;
			}
		}

		if (treeTemplate != null) {
			ITree tree = TreeManager.treeRoot.templateAsIndividual(treeTemplate);
			if (isPollinatedState) {
				tree.mate(tree);
			}

			setTree(tree);

			worldObj.markBlockForUpdate(getPos());
		}
	}

	@Override
	public void fromRipeningPacket(int newColourFruits) {
		if (newColourFruits == colourFruits) {
			return;
		}
		colourFruits = newColourFruits;
		worldObj.markBlockForUpdate(getPos());
	}

	/* IFRUITBEARER */
	@Override
	public Collection<ItemStack> pickFruit(ItemStack tool) {
		ITree tree = getTree();
		if (tree == null || !hasFruit()) {
			return Collections.emptyList();
		}

		List<ItemStack> produceStacks = tree.produceStacks(worldObj, getPos(), getRipeningTime());
		ripeningTime = 0;
		sendNetworkUpdateRipening();
		return produceStacks;
	}

	@Override
	public IFruitFamily getFruitFamily() {
		ITree tree = getTree();
		if (tree == null) {
			return null;
		}
		return tree.getGenome().getFruitProvider().getFamily();
	}

	@Override
	public float getRipeness() {
		if (ripeningPeriod == 0) {
			return 1.0f;
		}
		if (getTree() == null) {
			return 0f;
		}
		return (float) getRipeningTime() / ripeningPeriod;
	}

	@Override
	public boolean hasFruit() {
		return isFruitLeaf && !isDestroyed(getTree(), damage);
	}

	@Override
	public void addRipeness(float add) {
		if (getTree() == null || !isFruitLeaf || getRipeningTime() >= ripeningPeriod) {
			return;
		}
		ripeningTime += ripeningPeriod * add;
		sendNetworkUpdateRipening();
	}

	public String getSpeciesUID() {
		if (species == null) {
			return null;
		}
		return species.getUID();
	}

	/* IBUTTERFLYNURSERY */
	private IButterfly caterpillar;

	private void matureCaterpillar() {
		maturationTime++;

		ITree tree = getTree();
		boolean wasDestroyed = isDestroyed(tree, damage);
		damage += caterpillar.getGenome().getMetabolism();

		IButterflyGenome caterpillarGenome = caterpillar.getGenome();
		int caterpillarMatureTime = Math.round((float) caterpillarGenome.getLifespan() / (caterpillarGenome.getFertility() * 2));

		if (maturationTime >= caterpillarMatureTime) {
			ButterflyManager.butterflyRoot.plantCocoon(worldObj, this, getOwner());
			setCaterpillar(null);
		} else if (!wasDestroyed && isDestroyed(tree, damage)) {
			sendNetworkUpdate();
		}
	}

	@Override
	public BlockPos getCoordinates() {
		return getPos();
	}

	@Override
	public IButterfly getCaterpillar() {
		return caterpillar;
	}

	@Override
	public IIndividual getNanny() {
		return getTree();
	}

	@Override
	public void setCaterpillar(IButterfly butterfly) {
		maturationTime = 0;
		caterpillar = butterfly;
		sendNetworkUpdate();
	}

	@Override
	public boolean canNurse(IButterfly butterfly) {
		ITree tree = getTree();
		return !isDestroyed(tree, damage) && caterpillar == null;
	}
}
