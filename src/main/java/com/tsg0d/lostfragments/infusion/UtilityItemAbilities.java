package com.tsg0d.lostfragments.infusion;

import com.tsg0d.lostfragments.component.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.WorldClocks;

import java.util.Optional;
import java.util.UUID;

public final class UtilityItemAbilities {
	private UtilityItemAbilities() {
	}

	public static void initialize() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			ItemStack compass = player.getItemInHand(hand);
			if (!compass.is(Items.COMPASS) || !InfusionService.isInfused(compass)
					|| !(entity instanceof Player target)) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				compass.set(ModComponents.TRACKED_PLAYER, target.getUUID().toString());
				compass.set(ModComponents.TRACKED_PLAYER_NAME, target.getName().getString());
				player.sendOverlayMessage(Component.translatable(
						"message.lostfragments.compass_bound", target.getName()));
			}
			return InteractionResult.SUCCESS;
		});

		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!InfusionService.isInfused(stack)) {
				return InteractionResult.PASS;
			}
			if (stack.is(Items.COMPASS) && player.isShiftKeyDown()) {
				if (!level.isClientSide()) {
					stack.remove(ModComponents.TRACKED_PLAYER);
					stack.remove(ModComponents.TRACKED_PLAYER_NAME);
					stack.remove(DataComponents.LODESTONE_TRACKER);
					player.sendOverlayMessage(Component.translatable("message.lostfragments.compass_cleared"));
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(Items.CLOCK)) {
				if (player instanceof ServerPlayer serverPlayer) {
					long time = overworldTime(serverPlayer.level().registryAccess(),
							serverPlayer.level().getServer().overworld().clockManager());
					serverPlayer.sendOverlayMessage(timeText(time));
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 10 != 0) return;
			for (ServerPlayer owner : server.getPlayerList().getPlayers()) {
				for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++) {
					updateCompass(server.getPlayerList(), owner.getInventory().getItem(slot));
				}
			}
		});
	}

	private static void updateCompass(net.minecraft.server.players.PlayerList players, ItemStack compass) {
		if (!compass.is(Items.COMPASS) || !InfusionService.isInfused(compass)) return;
		String uuid = compass.get(ModComponents.TRACKED_PLAYER);
		if (uuid == null) return;
		try {
			ServerPlayer target = players.getPlayer(UUID.fromString(uuid));
			if (target == null) {
				compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.empty(), false));
			} else {
				compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(
						GlobalPos.of(target.level().dimension(), target.blockPosition())), false));
			}
		} catch (IllegalArgumentException ignored) {
			compass.remove(ModComponents.TRACKED_PLAYER);
		}
	}

	public static Component timeText(long dayTime) {
		long day = dayTime / 24_000L + 1L;
		long timeOfDay = dayTime % 24_000L;
		int totalMinutes = (int) ((timeOfDay * 60L / 1_000L + 360L) % 1_440L);
		return Component.translatable("message.lostfragments.clock_time", day,
				String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60));
	}

	public static long overworldTime(RegistryAccess registries, ClockManager clocks) {
		return clocks.getTotalTicks(registries.lookupOrThrow(Registries.WORLD_CLOCK)
				.getOrThrow(WorldClocks.OVERWORLD));
	}
}
