package com.tsg0d.lostfragments.infusion;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MiningAbilities {
	private static final Set<UUID> ACTIVE = new HashSet<>();
	private static final int MAX_TREE_LOGS = 1_024;
	private static final int MAX_TREE_RADIUS = 16;
	private static final int MAX_TREE_HEIGHT_ABOVE = 64;
	private static final int MAX_TREE_DEPTH_BELOW = 6;

	private MiningAbilities() {
	}

	public static void initialize() {
		PlayerBlockBreakEvents.AFTER.register((level, player, origin, originalState, blockEntity) -> {
			if (!(player instanceof ServerPlayer serverPlayer) || !player.isShiftKeyDown()) {
				return;
			}

			ItemStack tool = player.getMainHandItem();
			if (!InfusionService.isInfused(tool) || !ACTIVE.add(player.getUUID())) {
				return;
			}

			try {
				if ((tool.is(ItemTags.PICKAXES) || tool.is(ItemTags.SHOVELS))) {
					if (mineThreeByThree(serverPlayer, origin, originalState, tool) > 0) {
						AmethystParticles.burst((ServerLevel) serverPlayer.level(), origin, 14);
					}
				} else if (tool.is(ItemTags.AXES) && originalState.is(BlockTags.LOGS)) {
					if (fellTree(serverPlayer, origin, tool) > 0) {
						AmethystParticles.burst((ServerLevel) serverPlayer.level(), origin, 24);
					}
				} else if (tool.is(ItemTags.HOES) && isHoeClearingTarget(originalState)) {
					if (clearPlantsThreeByThree(serverPlayer, origin, tool) > 0) {
						AmethystParticles.burst((ServerLevel) serverPlayer.level(), origin, 14);
					}
				}
			} finally {
				ACTIVE.remove(player.getUUID());
			}
		});
	}

	private static int clearPlantsThreeByThree(ServerPlayer player, BlockPos origin, ItemStack hoe) {
		int cleared = 0;
		for (int x = -1; x <= 1 && !hoe.isEmpty(); x++) {
			for (int z = -1; z <= 1 && !hoe.isEmpty(); z++) {
				BlockPos target = origin.offset(x, 0, z);
				if (!target.equals(origin) && isHoeClearingTarget(player.level().getBlockState(target))) {
					if (player.gameMode.destroyBlock(target)) {
						cleared++;
					}
				}
			}
		}
		return cleared;
	}

	private static boolean isHoeClearingTarget(BlockState state) {
		return state.is(BlockTags.CROPS)
				|| state.is(BlockTags.FLOWERS)
				|| state.is(Blocks.SHORT_GRASS)
				|| state.is(Blocks.TALL_GRASS)
				|| state.is(Blocks.FERN)
				|| state.is(Blocks.LARGE_FERN)
				|| state.is(Blocks.SHORT_DRY_GRASS)
				|| state.is(Blocks.TALL_DRY_GRASS)
				|| state.is(Blocks.LEAF_LITTER);
	}

	private static int mineThreeByThree(ServerPlayer player, BlockPos origin,
			BlockState originalState, ItemStack tool) {
		int mined = 0;
		Direction facing = player.getDirection();
		boolean horizontalPlane = Math.abs(player.getXRot()) > 55.0F;

		for (int first = -1; first <= 1; first++) {
			for (int second = -1; second <= 1; second++) {
				BlockPos target;
				if (horizontalPlane) {
					target = origin.offset(first, 0, second);
				} else if (facing.getAxis() == Direction.Axis.X) {
					target = origin.offset(0, first, second);
				} else {
					target = origin.offset(first, second, 0);
				}

				if (target.equals(origin) || tool.isEmpty()) {
					continue;
				}

				BlockState targetState = player.level().getBlockState(target);
				if (!targetState.isAir()
						&& targetState.getDestroySpeed(player.level(), target) >= 0.0F
						&& tool.isCorrectToolForDrops(targetState)
						&& tool.isCorrectToolForDrops(originalState)) {
					if (player.gameMode.destroyBlock(target)) {
						mined++;
					}
				}
			}
		}
		return mined;
	}

	private static int fellTree(ServerPlayer player, BlockPos origin, ItemStack tool) {
		int felled = 0;
		ArrayDeque<BlockPos> pending = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();
		pending.add(origin);

		while (!pending.isEmpty() && visited.size() < MAX_TREE_LOGS && !tool.isEmpty()) {
			BlockPos current = pending.removeFirst();
			if (!visited.add(current) || !insideTreeBounds(current, origin)) {
				continue;
			}

			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						BlockPos next = current.offset(x, y, z);
						if (!visited.contains(next) && player.level().getBlockState(next).is(BlockTags.LOGS)) {
							pending.addLast(next);
						}
					}
				}
			}

			if (!current.equals(origin)) {
				if (player.gameMode.destroyBlock(current)) {
					felled++;
				}
			}
		}
		return felled;
	}

	private static boolean insideTreeBounds(BlockPos pos, BlockPos origin) {
		int xDistance = Math.abs(pos.getX() - origin.getX());
		int zDistance = Math.abs(pos.getZ() - origin.getZ());
		int yDistance = pos.getY() - origin.getY();
		return xDistance <= MAX_TREE_RADIUS
				&& zDistance <= MAX_TREE_RADIUS
				&& yDistance >= -MAX_TREE_DEPTH_BELOW
				&& yDistance <= MAX_TREE_HEIGHT_ABOVE;
	}
}
