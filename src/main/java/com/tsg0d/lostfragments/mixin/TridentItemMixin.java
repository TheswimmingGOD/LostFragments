package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.component.ModComponents;
import com.tsg0d.lostfragments.infusion.AmethystParticles;
import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
	private static final double DRY_RIPTIDE_CHANCE = 0.35;

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void lostfragments$tryDryRiptide(Level level, Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack trident = player.getItemInHand(hand);
		if (player.isInWaterOrRain() || !InfusionService.isInfused(trident)
				|| EnchantmentHelper.getTridentSpinAttackStrength(trident, player) <= 0.0F) {
			return;
		}

		trident.remove(ModComponents.DRY_RIPTIDE_READY);
		if (trident.nextDamageWillBreak()) {
			cir.setReturnValue(InteractionResult.FAIL);
			return;
		}
		if (level.isClientSide()) {
			trident.set(ModComponents.DRY_RIPTIDE_READY, true);
			player.startUsingItem(hand);
			cir.setReturnValue(InteractionResult.CONSUME);
			return;
		}

		if (level.getRandom().nextDouble() < DRY_RIPTIDE_CHANCE) {
			trident.set(ModComponents.DRY_RIPTIDE_READY, true);
			player.startUsingItem(hand);
			cir.setReturnValue(InteractionResult.CONSUME);
		} else {
			trident.hurtWithoutBreaking(1, player);
			ServerLevel serverLevel = (ServerLevel) level;
			AmethystParticles.burst(serverLevel, player.getX(), player.getY() + 1.0, player.getZ(), 9);
			level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.65F, 0.7F);
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Redirect(method = "releaseUsing", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
	private boolean lostfragments$allowPreparedDryRiptide(Player player) {
		return player.isInWaterOrRain()
				|| player.getMainHandItem().getOrDefault(ModComponents.DRY_RIPTIDE_READY, false)
				|| player.getOffhandItem().getOrDefault(ModComponents.DRY_RIPTIDE_READY, false);
	}

	@Inject(method = "releaseUsing", at = @At("RETURN"))
	private void lostfragments$clearDryRiptide(ItemStack trident, Level level, LivingEntity user,
			int remainingUseTicks, CallbackInfoReturnable<Boolean> cir) {
		trident.remove(ModComponents.DRY_RIPTIDE_READY);
	}
}
