package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.infusion.InfusionService;
import com.tsg0d.lostfragments.infusion.MaceAbilities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {
	@Inject(method = "hurtEnemy", at = @At("HEAD"))
	private void lostfragments$createGravityField(ItemStack mace, LivingEntity target,
			LivingEntity attacker, CallbackInfo ci) {
		if (MaceItem.canSmashAttack(attacker) && InfusionService.isInfused(mace)) {
			MaceAbilities.create(mace, target, attacker);
		}
	}
}
