package com.tsg0d.lostfragments.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public final class AmethystHollowsSurfaceFeature extends Feature<NoneFeatureConfiguration> {
	private static final int RADIUS = 6;

	public AmethystHollowsSurfaceFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		boolean placed = false;

		for (BlockPos pos : BlockPos.betweenClosed(
				origin.offset(-RADIUS, -RADIUS, -RADIUS),
				origin.offset(RADIUS, RADIUS, RADIUS))) {
			if (pos.getY() > 20 || pos.distSqr(origin) > RADIUS * RADIUS) continue;

			BlockState existing = level.getBlockState(pos);
			if (!existing.is(BlockTags.BASE_STONE_OVERWORLD)) continue;

			Direction exposedFace = randomExposedFace(level, pos, random);
			if (exposedFace == null) continue;

			double roll = random.nextDouble();
			BlockState replacement;
			if (roll < 0.72) replacement = Blocks.AMETHYST_BLOCK.defaultBlockState();
			else if (roll < 0.84) replacement = Blocks.CALCITE.defaultBlockState();
			else if (roll < 0.96) replacement = Blocks.SMOOTH_BASALT.defaultBlockState();
			else replacement = Blocks.BUDDING_AMETHYST.defaultBlockState();

			level.setBlock(pos, replacement, 2);
			placed = true;

			if (random.nextFloat() < 0.24F) {
				placeCluster(level, pos.relative(exposedFace), exposedFace, random);
			}
		}
		return placed;
	}

	private static Direction randomExposedFace(WorldGenLevel level, BlockPos pos, RandomSource random) {
		Direction selected = null;
		int matches = 0;
		for (Direction direction : Direction.values()) {
			BlockState neighbor = level.getBlockState(pos.relative(direction));
			if (!neighbor.isAir() && neighbor.getFluidState().getType() != Fluids.WATER) continue;
			if (random.nextInt(++matches) == 0) selected = direction;
		}
		return selected;
	}

	private static void placeCluster(WorldGenLevel level, BlockPos pos, Direction facing, RandomSource random) {
		BlockState existing = level.getBlockState(pos);
		boolean waterlogged = existing.getFluidState().getType() == Fluids.WATER;
		if (!existing.isAir() && !waterlogged) return;

		BlockState cluster = switch (random.nextInt(4)) {
			case 0 -> Blocks.SMALL_AMETHYST_BUD.defaultBlockState();
			case 1 -> Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState();
			case 2 -> Blocks.LARGE_AMETHYST_BUD.defaultBlockState();
			default -> Blocks.AMETHYST_CLUSTER.defaultBlockState();
		};
		cluster = cluster.setValue(AmethystClusterBlock.FACING, facing)
				.setValue(AmethystClusterBlock.WATERLOGGED, waterlogged);
		if (cluster.canSurvive(level, pos)) {
			level.setBlock(pos, cluster, 2);
		}
	}
}
