package com.tsg0d.lostfragments.infusion;

import com.tsg0d.lostfragments.config.LostFragmentsConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

public final class GrapplingCrossbowAbilities {
	private static final Map<AbstractArrow, Hook> BY_ARROW = new IdentityHashMap<>();
	private static final Map<ServerPlayer, Hook> BY_OWNER = new IdentityHashMap<>();
	private static final Set<AbstractArrow> SEEN_ARROWS = Collections.newSetFromMap(new WeakHashMap<>());
	private static final List<Hook> HOOKS = new ArrayList<>();

	private GrapplingCrossbowAbilities() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tickHooks());
	}

	public static void track(AbstractArrow arrow) {
		if (!(arrow.level() instanceof ServerLevel level) || SEEN_ARROWS.contains(arrow)
				|| !(arrow.getOwner() instanceof ServerPlayer owner)) return;

		ItemStack weapon = arrow.getWeaponItem();
		if (!(weapon.getItem() instanceof CrossbowItem) || !InfusionService.isInfused(weapon)) return;

		SEEN_ARROWS.add(arrow);
		Hook current = BY_OWNER.get(owner);
		if (current != null && current.createdAt == level.getGameTime()) return;
		clearOwnerHook(owner);
		Hook hook = new Hook(owner, arrow);
		HOOKS.add(hook);
		BY_ARROW.put(arrow, hook);
		BY_OWNER.put(owner, hook);
	}

	public static void latchBlock(AbstractArrow arrow, BlockHitResult hit) {
		Hook hook = BY_ARROW.get(arrow);
		if (hook == null) return;
		hook.anchor = hit.getLocation().add(hit.getDirection().getUnitVec3().scale(-0.06));
		hook.attached = true;
		arrow.setNoGravity(true);
		arrow.setDeltaMovement(Vec3.ZERO);
		activate(hook, hook.anchor);
	}

	public static void latchEntity(AbstractArrow arrow, EntityHitResult hit) {
		Hook hook = BY_ARROW.get(arrow);
		if (hook == null || hit.getEntity() == hook.owner) return;
		hook.target = hit.getEntity();
		hook.attached = true;
		activate(hook, hook.target.position());
	}

	private static void activate(Hook hook, Vec3 position) {
		AmethystParticles.burst((ServerLevel) hook.owner.level(), position.x, position.y, position.z, 10);
		hook.owner.level().playSound(null, hook.owner.blockPosition(), SoundEvents.LEAD_TIED,
				SoundSource.PLAYERS, 0.75F, 1.25F);
	}

	private static void tickHooks() {
		Iterator<Hook> iterator = HOOKS.iterator();
		while (iterator.hasNext()) {
			Hook hook = iterator.next();
			if (!valid(hook)) {
				remove(iterator, hook);
				continue;
			}

			Vec3 endpoint;
			if (hook.target != null) {
				if (!hook.target.isAlive()) {
					remove(iterator, hook);
					continue;
				}
				endpoint = hook.target.position().add(0, hook.target.getBbHeight() * 0.5, 0);
			} else if (hook.anchor != null) {
				endpoint = hook.anchor;
			} else {
				if (hook.arrow.isRemoved()) {
					remove(iterator, hook);
					continue;
				}
				endpoint = hook.arrow.position();
			}

			double distance = hook.owner.getEyePosition().distanceTo(endpoint);
			if (distance > LostFragmentsConfig.get().crossbow.maximumLineDistance) {
				remove(iterator, hook);
				continue;
			}

			if (hook.owner.level().getGameTime() % 2 == 0) {
				AmethystParticles.tether((ServerLevel) hook.owner.level(), hook.owner.getEyePosition(), endpoint);
			}

			if (!hook.attached) continue;
			if (distance <= LostFragmentsConfig.get().crossbow.stoppingDistance) {
				remove(iterator, hook);
			} else if (hook.target != null) {
				pullEntity(hook, endpoint);
			} else {
				pullPlayer(hook, endpoint);
			}
		}
	}

	private static boolean valid(Hook hook) {
		if (!hook.owner.isAlive() || hook.owner.hasDisconnected()) return false;
		return isHeldInfusedCrossbow(hook.owner.getMainHandItem())
				|| isHeldInfusedCrossbow(hook.owner.getOffhandItem());
	}

	private static boolean isHeldInfusedCrossbow(ItemStack stack) {
		return stack.getItem() instanceof CrossbowItem && InfusionService.isInfused(stack);
	}

	private static void pullPlayer(Hook hook, Vec3 endpoint) {
		Vec3 toward = endpoint.subtract(hook.owner.getEyePosition()).normalize();
		double strength = LostFragmentsConfig.get().crossbow.playerPullStrength;
		hook.owner.setDeltaMovement(hook.owner.getDeltaMovement().scale(0.72).add(toward.scale(strength)));
		hook.owner.hurtMarked = true;
		hook.owner.resetFallDistance();
	}

	private static void pullEntity(Hook hook, Vec3 endpoint) {
		Vec3 destination = hook.owner.getEyePosition().subtract(0, 0.35, 0);
		Vec3 toward = destination.subtract(endpoint).normalize();
		double strength = LostFragmentsConfig.get().crossbow.entityPullStrength;
		hook.target.setDeltaMovement(hook.target.getDeltaMovement().scale(0.72).add(toward.scale(strength)));
		hook.target.hurtMarked = true;
		hook.target.resetFallDistance();
	}

	private static void clearOwnerHook(ServerPlayer owner) {
		Iterator<Hook> iterator = HOOKS.iterator();
		while (iterator.hasNext()) {
			Hook hook = iterator.next();
			if (hook.owner == owner) remove(iterator, hook);
		}
	}

	private static void remove(Iterator<Hook> iterator, Hook hook) {
		BY_ARROW.remove(hook.arrow);
		BY_OWNER.remove(hook.owner, hook);
		if (hook.anchor != null && !hook.arrow.isRemoved()) hook.arrow.discard();
		iterator.remove();
	}

	private static final class Hook {
		private final ServerPlayer owner;
		private final AbstractArrow arrow;
		private Entity target;
		private Vec3 anchor;
		private boolean attached;
		private final long createdAt;

		private Hook(ServerPlayer owner, AbstractArrow arrow) {
			this.owner = owner;
			this.arrow = arrow;
			this.createdAt = owner.level().getGameTime();
		}
	}
}
