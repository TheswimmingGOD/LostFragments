package com.tsg0d.lostfragments.infusion;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class HoeAbilities {
	private HoeAbilities() {
	}

	public static void initialize() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack hoe = player.getItemInHand(hand);
			if (level.isClientSide()
					|| !player.isShiftKeyDown()
					|| hit.getDirection() != Direction.UP
					|| !hoe.is(ItemTags.HOES)
					|| !InfusionService.isInfused(hoe)) {
				return InteractionResult.PASS;
			}

			BlockPos origin = hit.getBlockPos();
			int tilled = 0;
			for (int x = -1; x <= 1 && !hoe.isEmpty(); x++) {
				for (int z = -1; z <= 1 && !hoe.isEmpty(); z++) {
					if (x == 0 && z == 0) {
						continue;
					}

					BlockPos target = origin.offset(x, 0, z);
					BlockHitResult targetHit = new BlockHitResult(
							Vec3.atCenterOf(target).add(0.0, 0.5, 0.0),
							Direction.UP,
							target,
							false
					);
					InteractionResult result = hoe.useOn(new UseOnContext(player, hand, targetHit));
					if (result.consumesAction()) {
						tilled++;
					}
				}
			}
			if (tilled > 0) {
				AmethystParticles.burst((ServerLevel) level, origin, 14);
			}

			// Let vanilla process the center block normally after the surrounding eight.
			return InteractionResult.PASS;
		});
	}
}
