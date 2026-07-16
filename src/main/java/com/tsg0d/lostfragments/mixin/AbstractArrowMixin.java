package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.infusion.GrapplingCrossbowAbilities;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void lostfragments$trackInfusedCrossbowArrow(CallbackInfo ci) {
		GrapplingCrossbowAbilities.track((AbstractArrow) (Object) this);
	}

	@Inject(method = "onHitBlock", at = @At("HEAD"))
	private void lostfragments$latchBlock(BlockHitResult hit, CallbackInfo ci) {
		GrapplingCrossbowAbilities.latchBlock((AbstractArrow) (Object) this, hit);
	}

	@Inject(method = "onHitEntity", at = @At("HEAD"))
	private void lostfragments$latchEntity(EntityHitResult hit, CallbackInfo ci) {
		GrapplingCrossbowAbilities.latchEntity((AbstractArrow) (Object) this, hit);
	}
}
