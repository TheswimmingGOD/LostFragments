package com.tsg0d.lostfragments.item;

import com.tsg0d.lostfragments.component.ModComponents;
import com.tsg0d.lostfragments.infusion.AmethystParticles;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;
import com.tsg0d.lostfragments.network.TalismanActivationPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.tsg0d.lostfragments.infusion.InfusionService;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;

public final class TalismanAbilities {
	private static final Set<ResourceKey<LootTable>> FRAGMENT_LOOT_TABLES = Set.of(
			BuiltInLootTables.ANCIENT_CITY,
			BuiltInLootTables.ANCIENT_CITY_ICE_BOX,
			BuiltInLootTables.END_CITY_TREASURE,
			BuiltInLootTables.DESERT_PYRAMID,
			BuiltInLootTables.JUNGLE_TEMPLE,
			BuiltInLootTables.BURIED_TREASURE,
			BuiltInLootTables.SHIPWRECK_MAP,
			BuiltInLootTables.SHIPWRECK_SUPPLY,
			BuiltInLootTables.SHIPWRECK_TREASURE,
			BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY,
			BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY,
			BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON,
			BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE,
			BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY,
			BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY
	);

	private TalismanAbilities() {}

	public static void initialize() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			ItemStack talisman = findTalisman(player);
			if (talisman.isEmpty()) return true;
			activate(player, source, talisman);
			return false;
		});

		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			if (!FRAGMENT_LOOT_TABLES.contains(key)) return;
			table.withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.when(LootItemRandomChanceCondition.randomChance(
							(float) (LostFragmentsConfig.get().talisman.fragmentLootChancePercent / 100.0)))
					.add(LootItem.lootTableItem(ModItems.LOST_CORNER_FRAGMENT))
					.add(LootItem.lootTableItem(ModItems.LOST_SIDE_FRAGMENT)));
		});
	}

	private static ItemStack findTalisman(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.is(ModItems.CATMEN_TALISMAN) && InfusionService.isInfused(stack)
					&& stack.getOrDefault(ModComponents.TALISMAN_USES,
							LostFragmentsConfig.get().talisman.uses) > 0) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private static void activate(ServerPlayer player, DamageSource source, ItemStack talisman) {
		var config = LostFragmentsConfig.get().talisman;
		int uses = talisman.getOrDefault(ModComponents.TALISMAN_USES, config.uses) - 1;
		if (uses <= 0) talisman.shrink(1); else talisman.set(ModComponents.TALISMAN_USES, uses);

		if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || player.getY() < player.level().getMinY()) {
			ServerLevel overworld = player.level().getServer().overworld();
			BlockPos spawn = overworld.getRespawnData().pos();
			player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5,
					Set.of(), player.getYRot(), player.getXRot(), true);
		}

		player.setHealth((float) Math.min(player.getMaxHealth(), config.healthAfterSave));
		player.clearFire();
		player.removeAllEffects();
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
				(int) Math.round(config.regenerationSeconds * 20.0), config.regenerationLevel - 1));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
				(int) Math.round(config.absorptionSeconds * 20.0), config.absorptionLevel - 1));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
				(int) Math.round(config.fireResistanceSeconds * 20.0), config.fireResistanceLevel - 1));
		player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
				SoundSource.PLAYERS, 1.0F, 1.0F);
		AmethystParticles.burst(player.level(), player.blockPosition(), 40);
		ServerPlayNetworking.send(player, TalismanActivationPayload.INSTANCE);
	}
}
