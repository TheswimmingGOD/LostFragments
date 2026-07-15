package com.tsg0d.lostfragments.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
	@ModifyArg(method = "shoot", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"), index = 0)
	private int lostfragments$oneBowDurabilityPerVolley(int amount, @Local(index = 14) int projectileIndex) {
		return (Object) this instanceof BowItem && projectileIndex > 0 ? 0 : amount;
	}
}
