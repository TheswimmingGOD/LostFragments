package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.infusion.AmethystParticles;
import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
	@Inject(method = "releaseUsing", at = @At("RETURN"))
	private void lostfragments$infusedBowParticles(ItemStack bow, Level level, LivingEntity user,
			int remainingUseTicks, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && level instanceof ServerLevel serverLevel
				&& InfusionService.isInfused(bow)) {
			AmethystParticles.burst(serverLevel, user.getX(), user.getEyeY() - 0.2, user.getZ(), 12);
		}
	}
}
