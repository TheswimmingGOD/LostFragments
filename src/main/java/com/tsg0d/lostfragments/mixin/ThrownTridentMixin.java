package com.tsg0d.lostfragments.mixin;

import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {
	@Inject(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostAttackEffectsWithItemSourceOnBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)V",
			shift = At.Shift.AFTER))
	private void lostfragments$clearWeatherChanneling(EntityHitResult hit, CallbackInfo ci) {
		ThrownTrident trident = (ThrownTrident) (Object) this;
		if (!(trident.level() instanceof ServerLevel level) || level.isThundering()) {
			return;
		}
		ItemStack weapon = trident.getWeaponItem();
		if (!InfusionService.isInfused(weapon) || !level.canSeeSky(hit.getEntity().blockPosition())) {
			return;
		}
		int channeling = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
				.get(Enchantments.CHANNELING)
				.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, weapon)).orElse(0);
		if (channeling == 0) {
			return;
		}

		LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (lightning == null) {
			return;
		}
		lightning.setPos(Vec3.atBottomCenterOf(hit.getEntity().blockPosition()));
		if (trident.getOwner() instanceof ServerPlayer player) {
			lightning.setCause(player);
		}
		level.addFreshEntity(lightning);
	}
}
