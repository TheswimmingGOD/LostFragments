package com.tsg0d.lostfragments.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class InfusionTableBlock extends Block {
	private static final VoxelShape NORTH_WEST_LEG = box(0, 0, 0, 2, 9, 2);
	private static final VoxelShape SOUTH_WEST_LEG = box(0, 0, 14, 2, 9, 16);
	private static final VoxelShape SOUTH_EAST_LEG = box(14, 0, 14, 16, 9, 16);
	private static final VoxelShape NORTH_EAST_LEG = box(14, 0, 0, 16, 9, 2);
	private static final VoxelShape TOP = box(0, 9, 0, 16, 12, 16);
	private static final VoxelShape SHARD = box(7, 12, 7, 9, 16, 9);

	private static final VoxelShape COLLISION_SHAPE = Shapes.or(
			TOP, NORTH_WEST_LEG, SOUTH_WEST_LEG, SOUTH_EAST_LEG, NORTH_EAST_LEG);
	private static final VoxelShape OUTLINE_SHAPE = Shapes.or(COLLISION_SHAPE, SHARD);

	public InfusionTableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return OUTLINE_SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return COLLISION_SHAPE;
	}
}
