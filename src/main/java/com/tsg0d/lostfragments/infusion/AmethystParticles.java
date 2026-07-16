package com.tsg0d.lostfragments.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class AmethystParticles {
	private AmethystParticles() {
	}

	public static void burst(ServerLevel level, BlockPos center, int count) {
		burst(level, center.getX() + 0.5, center.getY() + 0.65, center.getZ() + 0.5, count);
	}

	public static void burst(ServerLevel level, double x, double y, double z, int count) {
		level.sendParticles(ParticleTypes.WITCH, x, y, z, count,
				0.65, 0.45, 0.65, 0.02);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, Math.max(4, count / 3),
				0.45, 0.35, 0.45, 0.015);
	}

	public static void fullSetAura(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		level.sendParticles(ParticleTypes.WITCH,
				player.getX(), player.getY() + 1.0, player.getZ(), 5,
				0.55, 0.8, 0.55, 0.005);
	}

	public static void gravitySpiral(ServerLevel level, Vec3 center, int age, double fieldRadius) {
		for (int i = 0; i < 7; i++) {
			double progress = ((age * 0.16) + i / 7.0) % 1.0;
			double radius = fieldRadius * 0.96 * (1.0 - progress);
			double angle = age * 0.42 + i * (Math.PI * 2.0 / 7.0);
			double x = center.x + Math.cos(angle) * radius;
			double z = center.z + Math.sin(angle) * radius;
			double y = center.y + 0.15 + progress * 0.7;
			level.sendParticles(ParticleTypes.WITCH, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

}
