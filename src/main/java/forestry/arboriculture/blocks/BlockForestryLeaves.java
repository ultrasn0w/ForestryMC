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
package forestry.arboriculture.blocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.IGrowable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.arboriculture.EnumGermlingType;
import forestry.api.arboriculture.IToolGrafter;
import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.TreeManager;
import forestry.api.core.IItemModelRegister;
import forestry.api.core.IModelManager;
import forestry.api.core.IToolScoop;
import forestry.api.core.Tabs;
import forestry.api.lepidopterology.ButterflyManager;
import forestry.api.lepidopterology.EnumFlutterType;
import forestry.api.lepidopterology.IButterfly;
import forestry.arboriculture.LeafDecayHelper;
import forestry.arboriculture.PluginArboriculture;
import forestry.arboriculture.genetics.TreeDefinition;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.blocks.propertys.UnlistedBlockAccess;
import forestry.core.blocks.propertys.UnlistedBlockPos;
import forestry.core.proxy.Proxies;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.ItemStackUtil;

public class BlockForestryLeaves extends BlockLeavesBase implements ITileEntityProvider, IShearable, IGrowable, IItemModelRegister {

	public BlockForestryLeaves() {
		super(Material.leaves, false);
		setCreativeTab(Tabs.tabArboriculture);
		setStepSound(soundTypeGrass);
		setTickRandomly(true);
        setHardness(0.2F);
        setLightOpacity(1);
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return ((IExtendedBlockState) super.getExtendedState(state, world, pos)).withProperty(UnlistedBlockPos.POS, pos)
				.withProperty(UnlistedBlockAccess.BLOCKACCESS, world);
	}

	@Override
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{UnlistedBlockPos.POS, UnlistedBlockAccess.BLOCKACCESS});
	}

	/* TILE ENTITY */
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileLeaves();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {

	}

	/* DROP HANDLING */
	// Hack: 	When harvesting leaves we need to get the drops in onBlockHarvested,
	// 			because Mojang destroys the block and tile before calling getDrops.
	private final ThreadLocal<List<ItemStack>> drops = new ThreadLocal<>();

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		TileLeaves leafTile = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leafTile == null) {
			return;
		}

		int fortune = EnchantmentHelper.getFortuneModifier(player);
		float saplingModifier = 1.0f;

		if (!world.isRemote) {
			ItemStack held = player.inventory.getCurrentItem();
			if (held != null && held.getItem() instanceof IToolGrafter) {
				saplingModifier = ((IToolGrafter) held.getItem()).getSaplingModifier(held, world, player, pos);
				held.damageItem(1, player);
				if (held.stackSize <= 0) {
					player.destroyCurrentEquippedItem();
				}
			}
		}
		GameProfile playerProfile = player.getGameProfile();
		List<ItemStack> leafDrops = getLeafDrop(world, playerProfile, pos, saplingModifier, fortune);
		drops.set(leafDrops);
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> ret = drops.get();
		drops.remove();

		// leaves not harvested, get drops normally
		if (ret == null) {
			ret = getLeafDrop(world, null, pos, 1.0f, fortune);
		}

		return ret;
	}

	private static List<ItemStack> getLeafDrop(IBlockAccess world, @Nullable GameProfile playerProfile, BlockPos pos, float saplingModifier, int fortune) {
		List<ItemStack> prod = new ArrayList<>();

		TileLeaves tile = TileUtil.getTile(world, pos, TileLeaves.class);
		if (tile == null || tile.getTree() == null) {
			return prod;
		}

		// Add saplings
		ITree[] saplings = tile.getTree().getSaplings((World) world, playerProfile, pos, saplingModifier);

		for (ITree sapling : saplings) {
			if (sapling != null) {
				prod.add(TreeManager.treeRoot.getMemberStack(sapling, EnumGermlingType.SAPLING));
			}
		}

		// Add fruits
		if (tile.hasFruit()) {
			prod.addAll(tile.getTree().produceStacks((World) world, pos, tile.getRipeningTime()));
		}

		return prod;
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player) {
		TileLeaves leaves = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leaves == null) {
			return null;
		}
		ITree tree = leaves.getTree();
		String speciesUid = tree.getGenome().getPrimary().getUID();
		return PluginArboriculture.blocks.getDecorativeLeaves(speciesUid);
	}
	
	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
		return true;
	}
	
	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		TileLeaves leaves = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leaves == null) {
			return null;
		}
		ITree tree = leaves.getTree();
		String speciesUid = tree.getGenome().getPrimary().getUID();
		return Collections.singletonList(PluginArboriculture.blocks.getDecorativeLeaves(speciesUid));
	}
	
	@Override
	public void beginLeavesDecay(World world, BlockPos pos) {
		TileLeaves tile = TileUtil.getTile(world, pos, TileLeaves.class);
		if (tile == null) {
			return;
		}
		super.beginLeavesDecay(world, pos);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		TileLeaves tileLeaves = TileUtil.getTile(world, pos, TileLeaves.class);
		if (tileLeaves != null && TreeDefinition.Willow.getUID().equals(tileLeaves.getSpeciesUID())) {
			return null;
		}
		return super.getCollisionBoundingBox(world, pos, state);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entity) {
		entity.motionX *= 0.4D;
		entity.motionZ *= 0.4D;
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		TileLeaves tileLeaves = TileUtil.getTile(world, pos, TileLeaves.class);
		if (tileLeaves == null) {
			return;
		}

		LeafDecayHelper.leafDecay(this, world, pos);

		// check leaves tile again because they can decay in super.updateTick
		if (tileLeaves.isInvalid()) {
			return;
		}

		if (world.rand.nextFloat() > 0.1) {
			return;
		}
		tileLeaves.onBlockTick();
	}

	/* RENDERING */
	@Override
	public boolean isOpaqueCube() {
		return !Proxies.render.fancyGraphicsEnabled();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass) {
		TileLeaves leaves = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leaves == null) {
			return super.colorMultiplier(world, pos, renderPass);
		}

		final int colour;
		if (renderPass == 0) {
			EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
			colour = leaves.getFoliageColour(thePlayer);
		} else {
			colour = leaves.getFruitColour();
		}

		if (colour == 0) {
			return super.colorMultiplier(world, pos, renderPass);
		}
		return colour;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return Proxies.render.fancyGraphicsEnabled() ? EnumWorldBlockLayer.CUTOUT_MIPPED : EnumWorldBlockLayer.SOLID;
	}
	
	/* MODELS */
	@Override
	public void registerModel(Item item, IModelManager manager) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("forestry:leaves", "inventory"));
	}

	/* PROPERTIES */
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return 60;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return true;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		if (face == EnumFacing.DOWN) {
			return 20;
		} else if (face != EnumFacing.UP) {
			return 10;
		} else {
			return 5;
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		ItemStack heldItem = player.getHeldItem();
		TileEntity tile = world.getTileEntity(pos);
		IButterfly caterpillar = tile instanceof TileLeaves ? ((TileLeaves) tile).getCaterpillar() : null;
		if (heldItem != null && heldItem.getItem() instanceof IToolScoop && caterpillar != null) {
			ItemStack butterfly = ButterflyManager.butterflyRoot.getMemberStack(caterpillar, EnumFlutterType.CATERPILLAR);
			ItemStackUtil.dropItemStackAsEntity(butterfly, world, pos);
			((TileLeaves) tile).setCaterpillar(null);
			return true;
		}
		
		return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
	}

	/* IGrowable */

	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		TileLeaves leafTile = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leafTile != null) {
			return leafTile.hasFruit() && leafTile.getRipeness() < 1.0f;
		}
		return false;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		return true;
	}
	
	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		TileLeaves leafTile = TileUtil.getTile(world, pos, TileLeaves.class);
		if (leafTile != null) {
			leafTile.addRipeness(0.5f);
		}
	}
}
