package com.tsg0d.lostfragments.worldgen;

import com.tsg0d.lostfragments.LostFragments;
import com.tsg0d.lostfragments.infusion.InfusionService;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomes {
	public static final ResourceKey<Biome> AMETHYST_HOLLOWS = ResourceKey.create(
			Registries.BIOME, LostFragments.id("amethyst_hollows"));

	private ModBiomes() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 10 != 0) return;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				applyHollowsWaterEffect(player);
			}
		});
	}

	private static void applyHollowsWaterEffect(ServerPlayer player) {
		if (!player.isInWater() || !player.level().getBiome(player.blockPosition()).is(AMETHYST_HOLLOWS)) {
			return;
		}

		var config = LostFragmentsConfig.get().amethystHollows;
		int level = hasStableInfusedArmor(player)
				? config.infusedWaterResistanceLevel
				: config.waterResistanceLevel;
		int amplifier = Math.max(0, level - 1);
		MobEffectInstance current = player.getEffect(MobEffects.RESISTANCE);
		if (current == null || current.getDuration() < 30 || current.getAmplifier() < amplifier) {
			player.addEffect(new MobEffectInstance(
					MobEffects.RESISTANCE, config.waterEffectDurationTicks, amplifier, true, false, true));
		}
	}

	private static boolean hasStableInfusedArmor(ServerPlayer player) {
		for (EquipmentSlot slot : new EquipmentSlot[] {
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
		}) {
			ItemStack stack = player.getItemBySlot(slot);
			if (InfusionService.isInfused(stack)) return true;
		}
		return false;
	}
}
