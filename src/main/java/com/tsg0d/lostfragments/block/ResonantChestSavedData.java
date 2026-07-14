package com.tsg0d.lostfragments.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tsg0d.lostfragments.LostFragments;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ResonantChestSavedData extends SavedData {
	public static final class Network {
		public static final Codec<Network> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(network -> network.items),
				Codec.STRING.listOf().optionalFieldOf("endpoints", List.of())
						.forGetter(network -> new ArrayList<>(network.endpoints))
		).apply(instance, Network::new));

		final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
		final Set<String> endpoints;

		Network(List<ItemStack> loadedItems, List<String> loadedEndpoints) {
			for (int i = 0; i < Math.min(27, loadedItems.size()); i++) items.set(i, loadedItems.get(i));
			endpoints = new HashSet<>(loadedEndpoints);
		}

		Network(NonNullList<ItemStack> migratedItems) {
			this(migratedItems, List.of());
		}
	}

	private static final Codec<ResonantChestSavedData> CODEC = Codec.unboundedMap(Codec.STRING, Network.CODEC)
			.xmap(ResonantChestSavedData::new, data -> data.networks);
	private static final SavedDataType<ResonantChestSavedData> TYPE = new SavedDataType<>(
			LostFragments.id("resonant_chests"), ResonantChestSavedData::new, CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final Map<String, Network> networks;

	public ResonantChestSavedData() { this.networks = new HashMap<>(); }
	private ResonantChestSavedData(Map<String, Network> networks) { this.networks = new HashMap<>(networks); }

	public static ResonantChestSavedData get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public Network getNetwork(String frequency) { return networks.get(frequency); }

	public Network register(String frequency, String endpoint, NonNullList<ItemStack> migrationItems) {
		Network network = networks.computeIfAbsent(frequency, ignored -> new Network(migrationItems));
		if (network.endpoints.add(endpoint)) setDirty();
		return network;
	}

	public boolean canRegister(String frequency, String endpoint) {
		Network network = networks.get(frequency);
		return network == null || network.endpoints.contains(endpoint) || network.endpoints.size() < 4;
	}

	public NonNullList<ItemStack> removeEndpoint(String frequency, String endpoint) {
		Network network = networks.get(frequency);
		if (network == null) return NonNullList.withSize(27, ItemStack.EMPTY);
		network.endpoints.remove(endpoint);
		setDirty();
		if (!network.endpoints.isEmpty()) return NonNullList.withSize(27, ItemStack.EMPTY);
		networks.remove(frequency);
		return network.items;
	}
}
