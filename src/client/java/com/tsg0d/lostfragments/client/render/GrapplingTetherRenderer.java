package com.tsg0d.lostfragments.client.render;

import com.tsg0d.lostfragments.infusion.InfusionService;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class GrapplingTetherRenderer {
	private GrapplingTetherRenderer() {
	}

	public static void initialize() {
		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.level == null) return;
			Vec3 camera = context.levelState().cameraRenderState.pos;

			for (Entity entity : minecraft.level.entitiesForRendering()) {
				if (!(entity instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof Entity owner)) continue;
				ItemStack weapon = arrow.getWeaponItem();
				if (weapon == null || !(weapon.getItem() instanceof CrossbowItem)
						|| !InfusionService.isInfused(weapon)) continue;

				Vec3 start = arrow.position();
				Vec3 end = owner.getRopeHoldPosition(1.0F);
				EntityRenderState.LeashState leash = new EntityRenderState.LeashState();
				leash.offset = Vec3.ZERO;
				leash.start = start;
				leash.end = end;
				leash.startBlockLight = 15;
				leash.endBlockLight = 15;
				leash.startSkyLight = 15;
				leash.endSkyLight = 15;
				leash.slack = false;

				context.poseStack().pushPose();
				context.poseStack().translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
				context.submitNodeCollector().submitLeash(context.poseStack(), leash);
				context.poseStack().popPose();
			}
		});
	}
}
