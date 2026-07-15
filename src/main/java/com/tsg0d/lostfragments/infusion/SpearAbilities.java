package com.tsg0d.lostfragments.infusion;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;

public final class SpearAbilities {
	private static final Map<LivingEntity, Long> LAST_COUNTERED = new WeakHashMap<>();

	private SpearAbilities() {
	}

	public static void initialize() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack spear = player.getItemInHand(hand);
			if (!player.isShiftKeyDown() || !spear.is(ItemTags.SPEARS) || !InfusionService.isInfused(spear)) {
				return InteractionResult.PASS;
			}
			player.startUsingItem(hand);
			return InteractionResult.CONSUME;
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) tickBrace(player);
		});
	}

	private static void tickBrace(ServerPlayer player) {
		ItemStack spear = player.getUseItem();
		if (!player.isUsingItem() || !player.isShiftKeyDown() || !spear.is(ItemTags.SPEARS)
				|| !InfusionService.isInfused(spear)) return;

		var config = LostFragmentsConfig.get().spear;
		Vec3 movement = player.getDeltaMovement();
		double movementFactor = config.bracingMovementPercent / 100.0;
		player.setDeltaMovement(movement.x * movementFactor, movement.y, movement.z * movementFactor);
		ServerLevel level = player.level();
		Vec3 facing = player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
		long now = level.getGameTime();
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(config.braceRange), target -> target != player && target.isAlive()
						&& !player.isAlliedTo(target) && player.hasLineOfSight(target))) {
			Vec3 offset = target.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
			double distance = offset.length();
			double minimumDot = Math.cos(Math.toRadians(config.braceAngleDegrees / 2.0));
			if (distance < 0.2 || distance > config.braceRange || facing.dot(offset.normalize()) < minimumDot) continue;
			Vec3 approach = target.getDeltaMovement().multiply(1.0, 0.0, 1.0);
			if (approach.dot(offset.normalize()) > -config.minimumApproachSpeed) continue;
			if (now - LAST_COUNTERED.getOrDefault(target, Long.MIN_VALUE / 2)
					< Math.round(config.targetCooldownSeconds * 20.0)) continue;

			LAST_COUNTERED.put(target, now);
			double speed = approach.length();
			float damage = (float) Math.min(config.maximumDamage,
					config.minimumDamage + speed * config.speedDamageMultiplier);
			target.hurtServer(level, level.damageSources().playerAttack(player), damage);
			Vec3 away = offset.normalize();
			target.setDeltaMovement(away.x * config.knockback,
					Math.max(config.upwardKnockback, target.getDeltaMovement().y), away.z * config.knockback);
			target.hurtMarked = true;
			spear.hurtAndBreak(config.durabilityCost, player, player.getUsedItemHand());
			AmethystParticles.burst(level, target.getX(), target.getY() + target.getBbHeight() * 0.55,
					target.getZ(), 12);
			level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.8F, 0.9F);
		}
	}
}
