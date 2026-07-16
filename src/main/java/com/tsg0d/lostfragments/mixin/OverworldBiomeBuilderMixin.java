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
				Climate.Parameter.span(0.10F, 0.35F),
				Climate.Parameter.span(0.10F, 0.25F),
				Climate.Parameter.span(0.15F, 0.35F),
				Climate.Parameter.span(-0.30F, -0.15F),
				Climate.Parameter.span(0.70F, 0.90F),
				Climate.Parameter.span(0.72F, 0.78F),
				(float) LostFragmentsConfig.get().amethystHollows.climateRarityOffset
		), ModBiomes.AMETHYST_HOLLOWS));
	}
}
