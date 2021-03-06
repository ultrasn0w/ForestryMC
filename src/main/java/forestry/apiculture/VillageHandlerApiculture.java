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
package forestry.apiculture;

import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

/**
 * TODO: Make it to a {@link VillagerProfession} when Forge has Fixed the {@link VillagerRegistry}
 */
public class VillageHandlerApiculture /*implements IVillageCreationHandler, IVillageTradeHandler*/ {

	/*public static void registerVillageComponents() {
		MapGenStructureIO.registerStructureComponent(ComponentVillageBeeHouse.class, "Forestry:BeeHouse");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
		ItemStack wildcardPrincess = new ItemStack(PluginApiculture.items.beePrincessGE, 1, OreDictionary.WILDCARD_VALUE);
		recipeList.add(new MerchantRecipe(wildcardPrincess, new ItemStack(Items.emerald, 1)));

		ItemStack randomComb = PluginApiculture.items.beeComb.getRandomComb(1, random, false);
		recipeList.add(new MerchantRecipe(new ItemStack(Items.wheat, 2), randomComb));

		ItemStack apiary = PluginApiculture.blocks.apiculture.get(BlockTypeApiculture.APIARY);
		recipeList.add(new MerchantRecipe(new ItemStack(Blocks.log, 24, OreDictionary.WILDCARD_VALUE), apiary));

		ItemStack provenFrames = PluginApiculture.items.frameProven.getItemStack(6);
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 1), provenFrames));

		ItemStack monasticDrone = BeeDefinition.MONASTIC.getMemberStack(EnumBeeType.DRONE);
		recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, 12), wildcardPrincess, monasticDrone));
	}

	@Override
	public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int size) {
		return new StructureVillagePieces.PieceWeight(ComponentVillageBeeHouse.class, 15, MathHelper.getRandomIntegerInRange(random, size, 1 + size));
	}

	@Override
	public Class<?> getComponentClass() {
		return ComponentVillageBeeHouse.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List pieces, Random random, int p1, int p2,
			int p3, int p4, int p5) {
		return ComponentVillageBeeHouse.buildComponent(startPiece, pieces, random, p1, p2, p3, p4, p5);
	}*/
}
