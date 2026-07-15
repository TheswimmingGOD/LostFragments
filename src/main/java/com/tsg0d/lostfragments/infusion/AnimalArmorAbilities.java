package com.tsg0d.lostfragments.infusion;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

public final class AnimalArmorAbilities {
	private static final double ACTIVATION_CHANCE = 0.40;
	private static final long COOLDOWN_TICKS = 20L;
	private static final Map<LivingEntity, Long> LAST_ACTIVATION = new WeakHashMap<>();

	private AnimalArmorAbilities() {
	}

	public static void initialize() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((wearer, source, baseDamage, damageTaken, blocked) -> {
			if (wearer instanceof Player || !(wearer.level() instanceof ServerLevel level)
					|| damageTaken <= 0.0F || source.is(DamageTypes.THORNS)) {
				return;
			}
			ItemStack armor = wearer.getItemBySlot(EquipmentSlot.BODY);
			if (!InfusionService.isAnimalArmor(armor) || !InfusionService.isInfused(armor)) {
				return;
			}
			if (!(source.getEntity() instanceof LivingEntity attacker)
					|| source.getDirectEntity() != attacker || attacker == wearer || !attacker.isAlive()) {
				return;
			}

			long now = level.getGameTime();
			if (now - LAST_ACTIVATION.getOrDefault(wearer, Long.MIN_VALUE / 2) < COOLDOWN_TICKS
					|| level.getRandom().nextDouble() >= ACTIVATION_CHANCE) {
				return;
			}
			LAST_ACTIVATION.put(wearer, now);

			attacker.hurtServer(level, level.damageSources().thorns(wearer), 2.0F);
			Vec3 away = attacker.position().subtract(wearer.position());
			double horizontal = Math.max(0.001, Math.sqrt(away.x * away.x + away.z * away.z));
			attacker.push(away.x / horizontal * 1.15, 0.3, away.z / horizontal * 1.15);
			attacker.hurtMarked = true;
			AmethystParticles.burst(level, wearer.getX(), wearer.getY() + wearer.getBbHeight() * 0.6,
					wearer.getZ(), 18);
			level.playSound(null, wearer.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
					SoundSource.NEUTRAL, 0.8F, 1.15F);
		});
	}
}
