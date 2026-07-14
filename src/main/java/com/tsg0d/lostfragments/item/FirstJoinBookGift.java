package com.tsg0d.lostfragments.item;

import com.mojang.serialization.Codec;
import com.tsg0d.lostfragments.LostFragments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FirstJoinBookGift extends SavedData {
	private static final Codec<FirstJoinBookGift> CODEC = Codec.STRING.listOf().xmap(
			FirstJoinBookGift::new,
			data -> data.giftedPlayers.stream().map(UUID::toString).toList());
	private static final SavedDataType<FirstJoinBookGift> TYPE = new SavedDataType<>(
			LostFragments.id("first_join_book_gifts"), FirstJoinBookGift::new, CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final Set<UUID> giftedPlayers;

	public FirstJoinBookGift() {
		giftedPlayers = new HashSet<>();
	}

	private FirstJoinBookGift(List<String> savedPlayers) {
		this();
		for (String savedPlayer : savedPlayers) {
			try {
				giftedPlayers.add(UUID.fromString(savedPlayer));
			} catch (IllegalArgumentException ignored) {
				LostFragments.LOGGER.warn("Ignoring invalid player UUID in first-join book data: {}", savedPlayer);
			}
		}
	}

	private static FirstJoinBookGift get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	private boolean claim(UUID playerId) {
		if (!giftedPlayers.add(playerId)) {
			return false;
		}
		setDirty();
		return true;
	}

	public static void initialize() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!get(server).claim(handler.player.getUUID())) {
				return;
			}

			ItemStack book = new ItemStack(ModItems.BOOK_OF_INFUSION);
			if (!handler.player.getInventory().add(book)) {
				handler.player.drop(book, false);
			}
		});
	}
}
