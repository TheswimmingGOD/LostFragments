package com.tsg0d.lostfragments.component;

import com.mojang.serialization.Codec;
import com.tsg0d.lostfragments.LostFragments;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModComponents {
	public static final DataComponentType<Boolean> AMETHYST_INFUSED = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			LostFragments.id("amethyst_infused"),
			DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
	);
	public static final DataComponentType<Boolean> FRACTURED_INFUSION = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			LostFragments.id("fractured_infusion"),
			DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
	);
	public static final DataComponentType<Integer> FRACTURE_LEVEL = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			LostFragments.id("fracture_level"),
			DataComponentType.<Integer>builder().persistent(Codec.INT).build()
	);
	public static final DataComponentType<String> TRACKED_PLAYER = stringComponent("tracked_player");
	public static final DataComponentType<String> TRACKED_PLAYER_NAME = stringComponent("tracked_player_name");
	public static final DataComponentType<String> TELEPORT_ROD_ID = stringComponent("teleport_rod_id");
	public static final DataComponentType<String> TELEPORT_DIMENSION = stringComponent("teleport_dimension");
	public static final DataComponentType<Boolean> ENDER_PEARL_LOADED = booleanComponent("ender_pearl_loaded");
	public static final DataComponentType<Boolean> DRY_RIPTIDE_READY = booleanComponent("dry_riptide_ready");
	public static final DataComponentType<Long> TELEPORT_POSITION = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, LostFragments.id("teleport_position"),
			DataComponentType.<Long>builder().persistent(Codec.LONG).build());
	public static final DataComponentType<Integer> BUNDLE_CAPACITY = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, LostFragments.id("bundle_capacity"),
			DataComponentType.<Integer>builder().persistent(Codec.INT).build());
	public static final DataComponentType<Integer> TALISMAN_USES = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, LostFragments.id("talisman_uses"),
			DataComponentType.<Integer>builder().persistent(Codec.INT).build());

	private static DataComponentType<String> stringComponent(String name) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, LostFragments.id(name),
				DataComponentType.<String>builder().persistent(Codec.STRING).build());
	}

	private static DataComponentType<Boolean> booleanComponent(String name) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, LostFragments.id(name),
				DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
	}

	private ModComponents() {
	}

	public static void initialize() {
		LostFragments.LOGGER.info("Registering Lost Fragments data components");
	}
}
