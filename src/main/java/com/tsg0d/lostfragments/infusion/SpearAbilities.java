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

public final class SpearAbilities {
	private static final double RANGE = 4.0;
	private static final long TARGET_COOLDOWN = 20L;
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

		Vec3 movement = player.getDeltaMovement();
		player.setDeltaMovement(movement.x * 0.25, movement.y, movement.z * 0.25);
		ServerLevel level = player.level();
		Vec3 facing = player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
		long now = level.getGameTime();
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(RANGE), target -> target != player && target.isAlive()
						&& !player.isAlliedTo(target) && player.hasLineOfSight(target))) {
			Vec3 offset = target.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
			double distance = offset.length();
			if (distance < 0.2 || distance > RANGE || facing.dot(offset.normalize()) < 0.78) continue;
			Vec3 approach = target.getDeltaMovement().multiply(1.0, 0.0, 1.0);
			if (approach.dot(offset.normalize()) > -0.045) continue;
			if (now - LAST_COUNTERED.getOrDefault(target, Long.MIN_VALUE / 2) < TARGET_COOLDOWN) continue;

			LAST_COUNTERED.put(target, now);
			double speed = approach.length();
			float damage = (float) (4.0 + Math.min(4.0, speed * 12.0));
			target.hurtServer(level, level.damageSources().playerAttack(player), damage);
			Vec3 away = offset.normalize();
			target.setDeltaMovement(away.x * 0.9, Math.max(0.25, target.getDeltaMovement().y), away.z * 0.9);
			target.hurtMarked = true;
			spear.hurtAndBreak(1, player, player.getUsedItemHand());
			AmethystParticles.burst(level, target.getX(), target.getY() + target.getBbHeight() * 0.55,
					target.getZ(), 12);
			level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.8F, 0.9F);
		}
	}
}
