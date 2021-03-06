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
package forestry.core.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import forestry.core.blocks.BlockBase;
import forestry.core.config.Config;
import forestry.core.gui.GuiHandler;
import forestry.core.gui.IHintSource;

public abstract class TileBase extends TileForestry implements IHintSource {
	protected final List<String> hints;

	protected TileBase(String hintKey) {
		this.hints = new ArrayList<>(Config.hints.get(hintKey));
	}

	public void openGui(EntityPlayer player) {
		GuiHandler.openGui(player, this);
	}

	public boolean canDrainWithBucket() {
		return false;
	}

	/* IHintSource */
	@Override
	public final List<String> getHints() {
		return hints;
	}

	@Override
	public String getUnlocalizedTitle() {
		Block block = getBlockType();
		if (block instanceof BlockBase) {
			BlockBase blockBase = (BlockBase) block;
			int meta = getBlockMetadata();
			return block.getUnlocalizedName() + "." + blockBase.getNameFromMeta(meta) + ".name";
		}
		return super.getUnlocalizedTitle();
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		Block oldBlock = oldState.getBlock();
		Block newBlock = newState.getBlock();
		if (oldBlock != newBlock || !(oldBlock instanceof BlockBase) || !(newBlock instanceof BlockBase)) {
			return true;
		}

		BlockBase oldBlockBase = (BlockBase) oldBlock;
		BlockBase newBlockBase = (BlockBase) newBlock;
		return oldState.getValue(oldBlockBase.getTypeProperty()) != newState.getValue(newBlockBase.getTypeProperty());
	}
}
