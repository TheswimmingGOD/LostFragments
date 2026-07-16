package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.worldgen.ModBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
	@Inject(
			method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
			at = @At("RETURN"),
			cancellable = true)
	private void lostfragments$limitAmethystHollowsHeight(int quartX, int quartY, int quartZ,
			Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
		if (QuartPos.toBlock(quartY) <= 20 || !cir.getReturnValue().is(ModBiomes.AMETHYST_HOLLOWS)) {
			return;
		}

		Climate.TargetPoint sampled = sampler.sample(quartX, quartY, quartZ);
		Climate.TargetPoint surfacePoint = new Climate.TargetPoint(
				sampled.temperature(),
				sampled.humidity(),
				sampled.continentalness(),
				sampled.erosion(),
				Climate.quantizeCoord(0.0F),
				sampled.weirdness());
		cir.setReturnValue(((MultiNoiseBiomeSource) (Object) this).getNoiseBiome(surfacePoint));
	}
}
