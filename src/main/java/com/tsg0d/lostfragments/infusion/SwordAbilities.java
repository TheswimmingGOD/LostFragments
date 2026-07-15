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
import com.tsg0d.lostfragments.config.LostFragmentsConfig;

public final class SwordAbilities {
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
					player.getBoundingBox().inflate(LostFragmentsConfig.get().sword.radius),
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
				var config = LostFragmentsConfig.get().sword;
				target.push(away.x / horizontal * config.knockback, config.upwardKnockback,
						away.z / horizontal * config.knockback);
				target.hurtMarked = true;
				moved++;
			}
			var config = LostFragmentsConfig.get().sword;
			sword.hurtAndBreak(config.durabilityCost, player, hand);
			player.getCooldowns().addCooldown(sword, (int) Math.round(config.cooldownSeconds * 20.0));
			if (moved > 0) {
				AmethystParticles.burst((ServerLevel) level, player.blockPosition(), 22);
			}
			return InteractionResult.SUCCESS;
		});
	}
}
