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
package forestry.farming.logic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmable;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.vect.Vect;

public class FarmableCocoa implements IFarmable {

	private static final Block COCOA_PLANT = Blocks.cocoa;
	public static final Item COCOA_SEED = Items.dye;
	public static final int COCOA_META = 3;

	@Override
	public boolean isSaplingAt(World world, BlockPos pos) {
		return BlockUtil.getBlock(world, pos) == COCOA_PLANT;
	}

	@Override
	public ICrop getCropAt(World world, BlockPos pos) {
		Block block = BlockUtil.getBlock(world, pos);
		if (block != COCOA_PLANT) {
			return null;
		}
		int meta = BlockUtil.getBlockMetadata(world, pos);
		if (BlockUtil.getBlockState(world, pos).getValue(BlockCocoa.AGE) < 2) {
			return null;
		}

		return new CropBlock(world, block, meta, new Vect(pos));
	}

	@Override
	public boolean isGermling(ItemStack itemstack) {
		return itemstack.getItem() == COCOA_SEED && itemstack.getItemDamage() == COCOA_META;
	}

	@Override
	public boolean isWindfall(ItemStack itemstack) {
		return false;
	}

	@Override
	public boolean plantSaplingAt(EntityPlayer player, ItemStack germling, World world, BlockPos pos) {
		return BlockUtil.tryPlantCocoaPod(world, pos);
	}

}
