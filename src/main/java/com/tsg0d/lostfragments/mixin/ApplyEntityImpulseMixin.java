package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.config.LostFragmentsConfig;
import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ApplyEntityImpulse;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ApplyEntityImpulse.class)
public abstract class ApplyEntityImpulseMixin {
	@Inject(method = "apply", at = @At("RETURN"))
	private void lostfragments$addInfusedSpearLungeLift(ServerLevel level, int enchantmentLevel,
			EnchantedItemInUse item, Entity entity, Vec3 position, CallbackInfo ci) {
		ApplyEntityImpulse effect = (ApplyEntityImpulse) (Object) this;
		if (!isHorizontalLungeEffect(effect)
				|| !item.itemStack().is(ItemTags.SPEARS)
				|| !InfusionService.isInfused(item.itemStack())) {
			return;
		}

		double lift = LostFragmentsConfig.get().spear.lungeUpwardBoostPerLevel * enchantmentLevel;
		if (lift <= 0.0) return;
		entity.addDeltaMovement(new Vec3(0.0, lift, 0.0));
		entity.hurtMarked = true;
		entity.needsSync = true;
	}

	private static boolean isHorizontalLungeEffect(ApplyEntityImpulse effect) {
		Vec3 direction = effect.direction();
		Vec3 scale = effect.coordinateScale();
		return direction.equals(new Vec3(0.0, 0.0, 1.0))
				&& scale.equals(new Vec3(1.0, 0.0, 1.0));
	}
}
