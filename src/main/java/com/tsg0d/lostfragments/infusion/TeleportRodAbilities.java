package com.tsg0d.lostfragments.infusion;

import com.tsg0d.lostfragments.LostFragments;
import com.tsg0d.lostfragments.component.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TeleportRodAbilities {
	public static final ResourceKey<Enchantment> ENDER_REACH = ResourceKey.create(
			Registries.ENCHANTMENT, LostFragments.id("ender_reach"));
	private static final Map<UUID, String> ACTIVE_RODS = new HashMap<>();

	private TeleportRodAbilities() {}

	public static void initialize() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack rod = player.getItemInHand(hand);
			if (!rod.is(Items.FISHING_ROD) || !InfusionService.isInfused(rod)) return InteractionResult.PASS;
			if (player.isShiftKeyDown()) {
				InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
				ItemStack pearls = player.getItemInHand(other);
				if (!rod.getOrDefault(ModComponents.ENDER_PEARL_LOADED, false) && pearls.is(Items.ENDER_PEARL)) {
					if (!level.isClientSide()) {
						pearls.shrink(1);
						rod.set(ModComponents.ENDER_PEARL_LOADED, true);
						player.sendOverlayMessage(Component.translatable("message.lostfragments.rod_loaded"));
					}
					return InteractionResult.SUCCESS;
				}
			}
			Long packed = rod.get(ModComponents.TELEPORT_POSITION);
			if (packed != null) {
				if (player instanceof ServerPlayer serverPlayer) teleport(serverPlayer, rod, packed);
				return InteractionResult.SUCCESS;
			}
			if (!rod.getOrDefault(ModComponents.ENDER_PEARL_LOADED, false)) {
				if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.lostfragments.rod_needs_pearl"));
				return InteractionResult.FAIL;
			}
			if (!level.isClientSide()) {
				String id = rod.get(ModComponents.TELEPORT_ROD_ID);
				if (id == null) {
					id = UUID.randomUUID().toString();
					rod.set(ModComponents.TELEPORT_ROD_ID, id);
				}
				ACTIVE_RODS.put(player.getUUID(), id);
			}
			return InteractionResult.PASS;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
					sanitizeEnchantments(player.getInventory().getItem(slot));
				}
				String id = ACTIVE_RODS.get(player.getUUID());
				if (id == null || player.fishing == null) continue;
				if (!player.fishing.onGround() && player.fishing.getDeltaMovement().lengthSqr() > 0.01) continue;
				ItemStack rod = findRod(player, id);
				if (rod.isEmpty()) continue;
				BlockPos pos = player.fishing.blockPosition();
				rod.set(ModComponents.TELEPORT_POSITION, pos.asLong());
				rod.set(ModComponents.TELEPORT_DIMENSION, player.level().dimension().identifier().toString());
				player.fishing.discard();
				player.fishing = null;
				ACTIVE_RODS.remove(player.getUUID());
				player.sendOverlayMessage(Component.translatable("message.lostfragments.rod_anchor", pos.getX(), pos.getY(), pos.getZ()));
			}
		});
	}

	private static void sanitizeEnchantments(ItemStack rod) {
		if (!rod.is(Items.FISHING_ROD) || !InfusionService.isInfused(rod)) return;
		rod.set(DataComponents.ITEM_MODEL, LostFragments.id("infused_fishing_rod"));
		if (rod.has(ModComponents.ENDER_PEARL_LOADED)
				&& !rod.getOrDefault(ModComponents.ENDER_PEARL_LOADED, false)) {
			rod.remove(ModComponents.ENDER_PEARL_LOADED);
		}
		EnchantmentHelper.updateEnchantments(rod, enchantments -> enchantments.removeIf(holder ->
				!holder.is(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING)
						&& !holder.is(net.minecraft.world.item.enchantment.Enchantments.MENDING)
						&& !holder.is(ENDER_REACH)));
	}

	private static ItemStack findRod(ServerPlayer player, String id) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (id.equals(stack.get(ModComponents.TELEPORT_ROD_ID))) return stack;
		}
		return ItemStack.EMPTY;
	}

	private static void teleport(ServerPlayer player, ItemStack rod, long packed) {
		String dimension = rod.get(ModComponents.TELEPORT_DIMENSION);
		if (dimension == null || !dimension.equals(player.level().dimension().identifier().toString())) {
			player.sendOverlayMessage(Component.translatable("message.lostfragments.rod_wrong_dimension"));
			return;
		}
		BlockPos pos = BlockPos.of(packed);
		int enchantmentLevel = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.get(ENDER_REACH).map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, rod)).orElse(0);
		double maximum = 1500.0 * (enchantmentLevel + 1);
		if (player.position().distanceToSqr(Vec3.atCenterOf(pos)) > maximum * maximum) {
			player.sendOverlayMessage(Component.translatable("message.lostfragments.rod_too_far", (int) maximum));
			return;
		}
		ServerLevel level = player.level();
		Vec3 destination = Vec3.atBottomCenterOf(pos.above());
		if (!level.noCollision(player, player.getBoundingBox().move(destination.subtract(player.position())))) {
			player.sendOverlayMessage(Component.translatable("message.lostfragments.rod_blocked"));
			return;
		}
		player.teleportTo(level, destination.x, destination.y, destination.z, java.util.Set.of(), player.getYRot(), player.getXRot(), true);
		rod.remove(ModComponents.TELEPORT_POSITION);
		rod.remove(ModComponents.TELEPORT_DIMENSION);
		rod.remove(ModComponents.ENDER_PEARL_LOADED);
		AmethystParticles.burst(level, pos.above(), 22);
	}
}
