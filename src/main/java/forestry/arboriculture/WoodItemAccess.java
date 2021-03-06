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
package forestry.arboriculture;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import forestry.api.arboriculture.EnumWoodType;
import forestry.api.arboriculture.IWoodItemAccess;
import forestry.arboriculture.blocks.BlockArbDoor;
import forestry.arboriculture.blocks.BlockArbFence;
import forestry.arboriculture.blocks.BlockArbFenceGate;
import forestry.arboriculture.blocks.BlockArbLog;
import forestry.arboriculture.blocks.BlockArbPlanks;
import forestry.arboriculture.blocks.BlockArbSlab;
import forestry.arboriculture.blocks.BlockArbStairs;
import forestry.core.utils.Log;

public class WoodItemAccess implements IWoodItemAccess {
	private static final WoodMap logs = new WoodMap("logs");
	private static final WoodMap planks = new WoodMap("planks");
	private static final WoodMap slabs = new WoodMap("slabs");
	private static final WoodMap fences = new WoodMap("fences");
	private static final WoodMap fenceGates = new WoodMap("fenceGates");
	private static final WoodMap stairs = new WoodMap("stairs");
	private static final WoodMap doors = new WoodMap("doors") {
		@Nonnull
		@Override
		public EnumMap<EnumWoodType, ItemStack> get(boolean fireproof) {
			return super.get(false);
		}
	};

	private static class WoodMap {
		@Nonnull
		private final EnumMap<EnumWoodType, ItemStack> normal = new EnumMap<>(EnumWoodType.class);
		@Nonnull
		private final EnumMap<EnumWoodType, ItemStack> fireproof = new EnumMap<>(EnumWoodType.class);
		@Nonnull
		private final String name;

		public WoodMap(@Nonnull String name) {
			this.name = name;
		}

		@Nonnull
		public String getName() {
			return name;
		}

		@Nonnull
		public EnumMap<EnumWoodType, ItemStack> get(boolean fireproof) {
			return fireproof ? this.fireproof : this.normal;
		}
	}

	public static void registerLogs(List<BlockArbLog> blocks) {
		for (BlockArbLog block : blocks) {
			register(block, logs);
		}
	}

	public static void registerPlanks(List<BlockArbPlanks> blocks) {
		for (BlockArbPlanks block : blocks) {
			register(block, planks);
		}
	}

	public static void registerSlabs(List<BlockArbSlab> blocks) {
		for (BlockArbSlab block : blocks) {
			register(block, slabs);
		}
	}

	public static void registerFences(List<BlockArbFence> blocks) {
		for (BlockArbFence block : blocks) {
			register(block, fences);
		}
	}
	
	public static void registerFenceGates(List<BlockArbFenceGate> blocks) {
		for (BlockArbFenceGate block : blocks) {
			register(block, fenceGates);
		}
	}

	public static void registerStairs(List<BlockArbStairs> blocks) {
		for (BlockArbStairs block : blocks) {
			register(block, stairs);
		}
	}

	public static void registerDoors(List<BlockArbDoor> blocks) {
		for (BlockArbDoor block : blocks) {
			register(block, doors);
		}
	}

	private static <T extends Block & IWoodTyped> void register(T woodTyped, WoodMap woodMap) {
		boolean fireproof = woodTyped.isFireproof();
		EnumMap<EnumWoodType, ItemStack> registryMap = woodMap.get(fireproof);
		for (IBlockState blockState : woodTyped.getBlockState().getValidStates()) {
			int meta = woodTyped.getMetaFromState(blockState);
			EnumWoodType woodType = woodTyped.getWoodType(meta);
			ItemStack itemStack = new ItemStack(woodTyped, 1, meta);
			registryMap.put(woodType, itemStack);
		}
	}

	@Override
	public ItemStack getPlanks(EnumWoodType woodType, boolean fireproof) {
		return get(woodType, fireproof, planks);
	}

	@Override
	public ItemStack getLog(EnumWoodType woodType, boolean fireproof) {
		return get(woodType, fireproof, logs);
	}

	@Override
	public ItemStack getSlab(EnumWoodType woodType, boolean fireproof) {
		return get(woodType, fireproof, slabs);
	}

	@Override
	public ItemStack getFence(EnumWoodType woodType, boolean fireproof) {
		return get(woodType, fireproof, fences);
	}
	
	@Override
	public ItemStack getFenceGate(EnumWoodType woodType, boolean fireproof) {
		return get(woodType, fireproof, fenceGates);
	}

	@Override
	public ItemStack getStairs(EnumWoodType woodType, boolean fireproof) {
		return get(woodType, fireproof, stairs);
	}

	@Override
	public ItemStack getDoor(EnumWoodType woodType) {
		return get(woodType, false, doors);
	}

	private static ItemStack get(EnumWoodType woodType, boolean fireproof, WoodMap woodMap) {
		ItemStack itemStack = woodMap.get(fireproof).get(woodType);
		if (itemStack == null) {
			Log.error("No stack found for {} {} {}", woodType, woodMap.getName(), fireproof ? "fireproof" : "non-fireproof");
			return null;
		}
		return itemStack.copy();
	}
}
