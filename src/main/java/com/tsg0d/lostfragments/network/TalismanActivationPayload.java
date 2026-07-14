package com.tsg0d.lostfragments.network;

import com.tsg0d.lostfragments.LostFragments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TalismanActivationPayload() implements CustomPacketPayload {
	public static final TalismanActivationPayload INSTANCE = new TalismanActivationPayload();
	public static final Type<TalismanActivationPayload> TYPE = new Type<>(LostFragments.id("talisman_activation"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TalismanActivationPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override public Type<TalismanActivationPayload> type() { return TYPE; }
}
