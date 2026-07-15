package com.tsg0d.lostfragments.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.tsg0d.lostfragments.client.screen.LostFragmentsConfigScreen;

public final class LostFragmentsModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return LostFragmentsConfigScreen::new;
	}
}
