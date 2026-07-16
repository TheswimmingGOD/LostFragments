package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
	@Inject(method = "processProjectileCount", at = @At("RETURN"), cancellable = true)
	private static void lostfragments$infusedBowProjectileCount(ServerLevel level, ItemStack weapon,
			Entity shooter, int baseCount, CallbackInfoReturnable<Integer> cir) {
		if (weapon.getItem() instanceof CrossbowItem && InfusionService.isInfused(weapon)) {
			cir.setReturnValue(1);
		} else if (weapon.getItem() instanceof BowItem && InfusionService.isInfused(weapon)) {
			cir.setReturnValue(Math.max(LostFragmentsConfig.get().bow.arrowCount, cir.getReturnValue()));
		}
	}

	@Inject(method = "processProjectileSpread", at = @At("RETURN"), cancellable = true)
	private static void lostfragments$infusedBowSpread(ServerLevel level, ItemStack weapon,
			Entity shooter, float baseSpread, CallbackInfoReturnable<Float> cir) {
		if (weapon.getItem() instanceof BowItem && InfusionService.isInfused(weapon)) {
			cir.setReturnValue(Math.max((float) LostFragmentsConfig.get().bow.spreadDegrees, cir.getReturnValue()));
		}
	}

	@Inject(method = "modifyDamage", at = @At("RETURN"), cancellable = true)
	private static void lostfragments$wetImpaling(ServerLevel level, ItemStack weapon, Entity target,
			DamageSource source, float baseDamage, CallbackInfoReturnable<Float> cir) {
		if (!(weapon.getItem() instanceof TridentItem) || !InfusionService.isInfused(weapon)
				|| !target.isInWaterOrRain()
				|| target.getType().builtInRegistryHolder().is(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
			return;
		}
		int impaling = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.get(Enchantments.IMPALING)
				.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, weapon)).orElse(0);
		if (impaling > 0) {
			cir.setReturnValue(cir.getReturnValue()
					+ (float) LostFragmentsConfig.get().trident.impalingDamagePerLevel * impaling);
		}
	}
}
