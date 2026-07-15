package com.tsg0d.lostfragments;

import net.fabricmc.api.ModInitializer;
import com.tsg0d.lostfragments.block.ModBlocks;
import com.tsg0d.lostfragments.component.ModComponents;
import com.tsg0d.lostfragments.infusion.InfusionService;
import com.tsg0d.lostfragments.infusion.MiningAbilities;
import com.tsg0d.lostfragments.infusion.HoeAbilities;
import com.tsg0d.lostfragments.infusion.ArmorAbilities;
import com.tsg0d.lostfragments.infusion.SwordAbilities;
import com.tsg0d.lostfragments.infusion.UtilityItemAbilities;
import com.tsg0d.lostfragments.infusion.TeleportRodAbilities;
import com.tsg0d.lostfragments.infusion.AnimalArmorAbilities;
import com.tsg0d.lostfragments.infusion.MaceAbilities;
import com.tsg0d.lostfragments.infusion.SpearAbilities;
import com.tsg0d.lostfragments.menu.ModMenus;
import com.tsg0d.lostfragments.item.ModItems;
import com.tsg0d.lostfragments.item.FirstJoinBookGift;
import com.tsg0d.lostfragments.item.TalismanAbilities;
import com.tsg0d.lostfragments.network.ModNetworking;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LostFragments implements ModInitializer {
	public static final String MOD_ID = "lostfragments";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LostFragmentsConfig.load();
		ModComponents.initialize();
		ModNetworking.initialize();
		ModBlocks.initialize();
		ModItems.initialize();
		FirstJoinBookGift.initialize();
		ModMenus.initialize();
		InfusionService.initialize();
		MiningAbilities.initialize();
		HoeAbilities.initialize();
		ArmorAbilities.initialize();
		SwordAbilities.initialize();
		UtilityItemAbilities.initialize();
		TeleportRodAbilities.initialize();
		AnimalArmorAbilities.initialize();
		MaceAbilities.initialize();
		SpearAbilities.initialize();
		TalismanAbilities.initialize();
		LOGGER.info("Initializing Lost Fragments");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
