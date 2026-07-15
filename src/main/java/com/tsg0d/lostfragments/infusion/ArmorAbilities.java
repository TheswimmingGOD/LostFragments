package com.tsg0d.lostfragments.infusion;

import com.tsg0d.lostfragments.LostFragments;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import com.tsg0d.lostfragments.item.ModItems;

import java.util.List;
import java.util.Optional;

public final class ArmorAbilities {
	private static final Identifier WALK_SPEED_ID = LostFragments.id("infused_leggings_walk_speed");
	private static final Identifier SNEAK_SPEED_ID = LostFragments.id("infused_leggings_sneak_speed");
	private static final Identifier SWIM_SPEED_ID = LostFragments.id("infused_leggings_swim_speed");
	private static final Identifier FALL_REDUCTION_ID = LostFragments.id("infused_boots_fall_reduction");
	private static final Identifier FULL_SET_HEALTH_ID = LostFragments.id("infused_full_set_health");
	private static final Identifier FRACTURED_HEALTH_ID = LostFragments.id("fractured_armor_health");

	private static final AttributeModifier WALK_SPEED = new AttributeModifier(
			WALK_SPEED_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	private static final AttributeModifier SNEAK_SPEED = new AttributeModifier(
			SNEAK_SPEED_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	private static final AttributeModifier SWIM_SPEED = new AttributeModifier(
			SWIM_SPEED_ID, 0.10, AttributeModifier.Operation.ADD_VALUE);
	private static final AttributeModifier FALL_REDUCTION = new AttributeModifier(
			FALL_REDUCTION_ID, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

	private ArmorAbilities() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
		});
	}

	private static void tickPlayer(ServerPlayer player) {
		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
		ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
		ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
		int fracturedArmor = countFractured(helmet, chestplate, leggings, boots);
		int fracturedHeld = Math.max(fracturedToolLevel(player.getMainHandItem()),
				fracturedToolLevel(player.getOffhandItem()));
		if (fracturedHeld > 0) {
			applyHiddenEffect(player, MobEffects.WEAKNESS, 50, 30, Math.min(4, fracturedHeld - 1));
		}
		if (fracturedArmor > 0) {
			applyHiddenEffect(player, MobEffects.SLOWNESS, 50, 30, Math.min(4, fracturedArmor - 1));
			if (player.tickCount % 400 < 100) {
				applyHiddenEffect(player, MobEffects.DARKNESS, 30, 20, Math.min(4, fracturedArmor - 1));
			}
		}

		if (InfusionService.isInfused(helmet)) {
			applyHiddenEffect(player, MobEffects.NIGHT_VISION, 320, 240, 0);
		}
		if (InfusionService.isInfused(chestplate)) {
			applyHiddenEffect(player, MobEffects.RESISTANCE, 50, 30, 0);
		}

		setModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), WALK_SPEED,
				InfusionService.isInfused(leggings));
		setModifier(player.getAttribute(Attributes.SNEAKING_SPEED), SNEAK_SPEED,
				InfusionService.isInfused(leggings));
		setModifier(player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY), SWIM_SPEED,
				InfusionService.isInfused(leggings));
		setModifier(player.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER), FALL_REDUCTION,
				InfusionService.isInfused(boots));

		List<ItemStack> armor = List.of(helmet, chestplate, leggings, boots);
		Optional<String> matchingMaterial = matchingInfusedMaterial(armor);
		AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
		setModifier(health, new AttributeModifier(FRACTURED_HEALTH_ID,
				-Math.min(10.0, fracturedArmor), AttributeModifier.Operation.ADD_VALUE), fracturedArmor > 0);
		if (matchingMaterial.isPresent()) {
			double healthPoints = healthBonus(matchingMaterial.get(), armor);
			setModifier(health, new AttributeModifier(FULL_SET_HEALTH_ID, healthPoints,
					AttributeModifier.Operation.ADD_VALUE), true);
			if (player.tickCount % 20 == 0) {
				AmethystParticles.fullSetAura(player);
			}
		} else {
			removeModifier(health, FULL_SET_HEALTH_ID);
		}
	}

	private static int fracturedToolLevel(ItemStack stack) {
		return !stack.is(ModItems.INFUSED_BUNDLE) ? InfusionService.fractureLevel(stack) : 0;
	}

	private static int countFractured(ItemStack... stacks) {
		int count = 0;
		for (ItemStack stack : stacks) {
			count += InfusionService.fractureLevel(stack);
		}
		return count;
	}

	private static void applyHiddenEffect(ServerPlayer player,
			net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
			int duration, int refreshAt, int amplifier) {
		MobEffectInstance current = player.getEffect(effect);
		if (current == null || current.getDuration() < refreshAt || current.getAmplifier() != amplifier) {
			player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
		}
	}

	private static void setModifier(AttributeInstance attribute, AttributeModifier modifier, boolean enabled) {
		if (attribute == null) {
			return;
		}
		if (enabled) {
			attribute.addOrUpdateTransientModifier(modifier);
		} else {
			attribute.removeModifier(modifier.id());
		}
	}

	private static void removeModifier(AttributeInstance attribute, Identifier id) {
		if (attribute != null) {
			attribute.removeModifier(id);
		}
	}

	private static Optional<String> matchingInfusedMaterial(List<ItemStack> armor) {
		String material = null;
		for (ItemStack stack : armor) {
			if (!InfusionService.isInfused(stack)) {
				return Optional.empty();
			}
			Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
			if (equippable == null || equippable.assetId().isEmpty()) {
				return Optional.empty();
			}
			String current = equippable.assetId().get().identifier().toString();
			if (material == null) {
				material = current;
			} else if (!material.equals(current)) {
				return Optional.empty();
			}
		}
		return Optional.ofNullable(material);
	}

	private static double healthBonus(String material, List<ItemStack> armor) {
		String path = material.substring(material.indexOf(':') + 1);
		if (path.contains("leather")) return 1.0;
		if (path.contains("gold") || path.contains("chain")) return 2.0;
		if (path.contains("copper") || path.contains("iron")) return 3.0;
		if (path.contains("diamond")) return 4.0;
		if (path.contains("netherite")) return 5.0;

		double armorPoints = 0.0;
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < armor.size(); i++) {
			ItemAttributeModifiers modifiers = armor.get(i).getOrDefault(
					DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
			armorPoints += modifiers.compute(Attributes.ARMOR, 0.0, slots[i]);
		}
		if (armorPoints <= 7.0) return 1.0;
		if (armorPoints <= 12.0) return 2.0;
		if (armorPoints <= 17.0) return 3.0;
		if (armorPoints <= 20.0) return 4.0;
		return 5.0;
	}
}
