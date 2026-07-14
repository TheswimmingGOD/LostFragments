package com.tsg0d.lostfragments.menu;

import com.tsg0d.lostfragments.LostFragments;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
	public static final MenuType<InfusionTableMenu> INFUSION_TABLE = Registry.register(
			BuiltInRegistries.MENU,
			LostFragments.id("infusion_table"),
			new ExtendedMenuType<>(InfusionTableMenu::new, BlockPos.STREAM_CODEC)
	);
	public static final MenuType<ResonantEnderChestMenu> RESONANT_ENDER_CHEST = Registry.register(
			BuiltInRegistries.MENU, LostFragments.id("resonant_ender_chest"),
			new ExtendedMenuType<>(ResonantEnderChestMenu::new, BlockPos.STREAM_CODEC));

	private ModMenus() {
	}

	public static void initialize() {
		LostFragments.LOGGER.info("Registering Lost Fragments menus");
	}
}
