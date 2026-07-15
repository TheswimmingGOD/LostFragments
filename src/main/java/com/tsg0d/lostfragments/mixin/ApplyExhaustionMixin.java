package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ApplyExhaustion;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;

@Mixin(ApplyExhaustion.class)
public abstract class ApplyExhaustionMixin {
	@ModifyArg(method = "apply", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"), index = 0)
	private float lostfragments$reduceInfusedSpearLungeCost(float amount,
			@Local(argsOnly = true) EnchantedItemInUse item) {
		return item.itemStack().is(ItemTags.SPEARS) && InfusionService.isInfused(item.itemStack())
				? amount * (float) (LostFragmentsConfig.get().spear.lungeHungerPercent / 100.0) : amount;
	}
}
