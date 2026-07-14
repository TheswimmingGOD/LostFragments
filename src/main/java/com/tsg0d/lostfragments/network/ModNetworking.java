package com.tsg0d.lostfragments.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
	private ModNetworking() {}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(
				TalismanActivationPayload.TYPE, TalismanActivationPayload.CODEC);
	}
}
