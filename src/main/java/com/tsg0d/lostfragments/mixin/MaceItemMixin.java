package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.infusion.InfusionService;
import com.tsg0d.lostfragments.infusion.MaceAbilities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {
	@Shadow
	private static void knockback(Level level, Entity attacker, Entity target) {
		throw new AssertionError();
	}

	@Inject(method = "hurtEnemy", at = @At("HEAD"))
	private void lostfragments$createGravityField(ItemStack mace, LivingEntity target,
			LivingEntity attacker, CallbackInfo ci) {
		if (MaceItem.canSmashAttack(attacker) && InfusionService.isInfused(mace)) {
			MaceAbilities.create(mace, target, attacker);
		}
	}

	@Redirect(method = "hurtEnemy", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/MaceItem;knockback(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V"))
	private void lostfragments$disableInfusedMaceKnockback(Level level, Entity attacker, Entity target,
			ItemStack mace, LivingEntity hitTarget, LivingEntity wielder) {
		if (!InfusionService.isInfused(mace)) {
			knockback(level, attacker, target);
		}
	}
}
