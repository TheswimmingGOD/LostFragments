package com.tsg0d.lostfragments.infusion;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class SwordAbilities {
	private static final double RADIUS = 3.0;
	private static final int COOLDOWN_TICKS = 120;

	private SwordAbilities() {
	}

	public static void initialize() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack sword = player.getItemInHand(hand);
			if (!sword.is(ItemTags.SWORDS) || !InfusionService.isInfused(sword)) {
				return InteractionResult.PASS;
			}
			if (player.getCooldowns().isOnCooldown(sword)) {
				return InteractionResult.FAIL;
			}
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			List<LivingEntity> targets = level.getEntitiesOfClass(
					LivingEntity.class,
					player.getBoundingBox().inflate(RADIUS),
					target -> target != player
							&& target.isAlive()
							&& !player.isAlliedTo(target)
							&& player.hasLineOfSight(target)
			);
			int moved = 0;
			for (LivingEntity target : targets) {
				Vec3 away = target.position().subtract(player.position());
				double horizontal = Math.sqrt(away.x * away.x + away.z * away.z);
				if (horizontal < 0.001) {
					away = new Vec3(0.0, 0.0, 1.0);
					horizontal = 1.0;
				}
				target.push(away.x / horizontal * 1.35, 0.35, away.z / horizontal * 1.35);
				target.hurtMarked = true;
				moved++;
			}
			sword.hurtAndBreak(4, player, hand);
			player.getCooldowns().addCooldown(sword, COOLDOWN_TICKS);
			if (moved > 0) {
				AmethystParticles.burst((ServerLevel) level, player.blockPosition(), 22);
			}
			return InteractionResult.SUCCESS;
		});
	}
}
