package com.tsg0d.lostfragments.infusion;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;

public final class MaceAbilities {
	private static final List<GravityField> FIELDS = new ArrayList<>();

	private MaceAbilities() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tickFields());
	}

	public static void create(ItemStack mace, LivingEntity target, LivingEntity attacker) {
		if (!(attacker.level() instanceof ServerLevel level) || !InfusionService.isInfused(mace)) return;
		FIELDS.add(new GravityField(level, target.position(), attacker, mace.copy(), 0, false));
		level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
				SoundSource.PLAYERS, 1.0F, 0.65F);
	}

	private static void tickFields() {
		Iterator<GravityField> iterator = FIELDS.iterator();
		while (iterator.hasNext()) {
			GravityField field = iterator.next();
			var config = LostFragmentsConfig.get().mace;
			if (field.age >= Math.round(config.durationSeconds * 20.0) || !field.owner.isAlive()) {
				iterator.remove();
				continue;
			}
			AmethystParticles.gravitySpiral(field.level, field.center, field.age, config.radius);
			if (field.age % config.pullIntervalTicks == 0) pull(field);
			field.age++;
		}
	}

	private static void pull(GravityField field) {
		var config = LostFragmentsConfig.get().mace;
		List<LivingEntity> targets = field.level.getEntitiesOfClass(LivingEntity.class,
				new net.minecraft.world.phys.AABB(field.center, field.center).inflate(config.radius),
				entity -> entity != field.owner && entity.isAlive() && !(entity instanceof ArmorStand)
						&& !field.owner.isAlliedTo(entity));
		boolean pulled = false;
		for (LivingEntity target : targets) {
			Vec3 toward = field.center.subtract(target.position());
			double distance = toward.length();
			if (distance < 0.25 || distance > config.radius || !field.owner.hasLineOfSight(target)) continue;
			double strength = config.minimumPull
					+ Math.max(0.0, config.maximumPull - config.minimumPull) * (distance / config.radius);
			Vec3 motion = toward.normalize().scale(strength);
			target.push(motion.x, Math.max(-0.04, motion.y * 0.35) + 0.04, motion.z);
			target.hurtMarked = true;
			pulled = true;
		}
		if (pulled && !field.durabilityCharged) {
			ItemStack held = field.owner.getMainHandItem();
			if (InfusionService.isInfused(held) && held.getItem() == field.maceTemplate.getItem()) {
				held.hurtAndBreak(config.durabilityCost, field.owner, EquipmentSlot.MAINHAND);
			}
			field.durabilityCharged = true;
		}
	}

	private static final class GravityField {
		private final ServerLevel level;
		private final Vec3 center;
		private final LivingEntity owner;
		private final ItemStack maceTemplate;
		private int age;
		private boolean durabilityCharged;

		private GravityField(ServerLevel level, Vec3 center, LivingEntity owner, ItemStack maceTemplate,
				int age, boolean durabilityCharged) {
			this.level = level;
			this.center = center;
			this.owner = owner;
			this.maceTemplate = maceTemplate;
			this.age = age;
			this.durabilityCharged = durabilityCharged;
		}
	}
}
