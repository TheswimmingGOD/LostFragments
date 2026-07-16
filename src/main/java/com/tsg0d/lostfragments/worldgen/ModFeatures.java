package com.tsg0d.lostfragments.worldgen;

import com.tsg0d.lostfragments.LostFragments;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ModFeatures {
	public static final Feature<?> AMETHYST_HOLLOWS_SURFACE = Registry.register(
			BuiltInRegistries.FEATURE,
			LostFragments.id("amethyst_hollows_surface"),
			new AmethystHollowsSurfaceFeature());

	private ModFeatures() {
	}

	public static void initialize() {
		LostFragments.LOGGER.info("Registering Lost Fragments world-generation features");
	}
}
