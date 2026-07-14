package com.tsg0d.lostfragments.client.render;

import com.tsg0d.lostfragments.block.ResonantEnderChestBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;

public final class ResonantEnderChestRenderer extends ChestRenderer<ResonantEnderChestBlockEntity> {
	public ResonantEnderChestRenderer(BlockEntityRendererProvider.Context context) { super(context); }

	@Override
	public void extractRenderState(ResonantEnderChestBlockEntity chest, ChestRenderState state,
			float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay overlay) {
		super.extractRenderState(chest, state, partialTick, cameraPosition, overlay);
		state.material = ChestRenderState.ChestMaterialType.ENDER_CHEST;
	}
}
