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
import com.tsg0d.lostfragments.config.LostFragmentsConfig;

public final class AnimalArmorAbilities {
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
			var config = LostFragmentsConfig.get().animalArmor;
			if (now - LAST_ACTIVATION.getOrDefault(wearer, Long.MIN_VALUE / 2) < Math.round(config.cooldownSeconds * 20.0)
					|| level.getRandom().nextDouble() >= config.activationChancePercent / 100.0) {
				return;
			}
			LAST_ACTIVATION.put(wearer, now);

			attacker.hurtServer(level, level.damageSources().thorns(wearer), (float) config.damage);
			Vec3 away = attacker.position().subtract(wearer.position());
			double horizontal = Math.max(0.001, Math.sqrt(away.x * away.x + away.z * away.z));
			attacker.push(away.x / horizontal * config.knockback, config.upwardKnockback,
					away.z / horizontal * config.knockback);
			attacker.hurtMarked = true;
			AmethystParticles.burst(level, wearer.getX(), wearer.getY() + wearer.getBbHeight() * 0.6,
					wearer.getZ(), 18);
			level.playSound(null, wearer.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
					SoundSource.NEUTRAL, 0.8F, 1.15F);
		});
	}
}
