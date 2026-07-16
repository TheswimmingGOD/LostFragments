package com.tsg0d.lostfragments.mixin;

import com.mojang.datafixers.util.Pair;
import com.tsg0d.lostfragments.worldgen.ModBiomes;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
	@Inject(method = "addUndergroundBiomes", at = @At("TAIL"))
	private void lostfragments$addAmethystHollows(
			Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
			CallbackInfo ci) {
		consumer.accept(Pair.of(Climate.parameters(
				Climate.Parameter.span(0.22F, 0.28F),
				Climate.Parameter.span(0.16F, 0.20F),
				Climate.Parameter.span(0.22F, 0.26F),
				Climate.Parameter.span(-0.24F, -0.21F),
				Climate.Parameter.span(0.76F, 0.86F),
				Climate.Parameter.span(0.745F, 0.755F),
				(float) LostFragmentsConfig.get().amethystHollows.rarityOffset
		), ModBiomes.AMETHYST_HOLLOWS));
	}
}
