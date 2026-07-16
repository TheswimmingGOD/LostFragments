package com.tsg0d.lostfragments.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tsg0d.lostfragments.LostFragments;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LostFragmentsConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("lostfragments.json");
	private static LostFragmentsConfig instance = new LostFragmentsConfig();

	public Infusion infusion = new Infusion();
	public Mining mining = new Mining();
	public Sword sword = new Sword();
	public Bow bow = new Bow();
	public Crossbow crossbow = new Crossbow();
	public AnimalArmor animalArmor = new AnimalArmor();
	public Armor armor = new Armor();
	public Trident trident = new Trident();
	public Spear spear = new Spear();
	public Mace mace = new Mace();
	public FishingRod fishingRod = new FishingRod();
	public Bundle bundle = new Bundle();
	public Talisman talisman = new Talisman();
	public ResonantChest resonantChest = new ResonantChest();

	public static LostFragmentsConfig get() { return instance; }

	public static void load() {
		if (Files.isRegularFile(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				LostFragmentsConfig loaded = GSON.fromJson(reader, LostFragmentsConfig.class);
				if (loaded != null) instance = loaded;
			} catch (Exception exception) {
				LostFragments.LOGGER.error("Could not read {}; using defaults", PATH, exception);
			}
		}
		sanitize(instance);
		save();
	}

	public static void replaceAndSave(LostFragmentsConfig replacement) {
		sanitize(replacement);
		instance = replacement;
		save();
	}

	public static LostFragmentsConfig copy() {
		return GSON.fromJson(GSON.toJson(instance), LostFragmentsConfig.class);
	}

	public static LostFragmentsConfig defaults() { return new LostFragmentsConfig(); }

	public static Path path() { return PATH; }

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) { GSON.toJson(instance, writer); }
		} catch (IOException exception) {
			LostFragments.LOGGER.error("Could not save {}", PATH, exception);
		}
	}

	private static void sanitize(Object object) {
		if (object == null) return;
		for (java.lang.reflect.Field field : object.getClass().getFields()) {
			try {
				Object value = field.get(object);
				ConfigRange range = field.getAnnotation(ConfigRange.class);
				if (range != null && value instanceof Number number) {
					double clamped = Math.max(range.min(), Math.min(range.max(), number.doubleValue()));
					if (field.getType() == int.class) field.setInt(object, (int) Math.round(clamped));
					else if (field.getType() == long.class) field.setLong(object, Math.round(clamped));
					else if (field.getType() == float.class) field.setFloat(object, (float) clamped);
					else if (field.getType() == double.class) field.setDouble(object, clamped);
				} else if (value != null && !field.getType().isPrimitive()) sanitize(value);
			} catch (IllegalAccessException ignored) { }
		}
	}

	public static final class Infusion {
		@ConfigRange(min=1,max=64) public int bookCost=1, shovelCost=2, hoeCost=2, clockCost=2;
		@ConfigRange(min=1,max=64) public int pickaxeCost=3, axeCost=3, swordCost=3, compassCost=3;
		@ConfigRange(min=1,max=64) public int helmetCost=4, bootsCost=4, fishingRodCost=4, bundleCost=4;
		@ConfigRange(min=1,max=64) public int bowCost=4, animalArmorCost=4, spearCost=4, crossbowCost=5, leggingsCost=5;
		@ConfigRange(min=1,max=64) public int tridentCost=5, chestplateCost=6, maceCost=6;
		@ConfigRange(min=1,max=64) public int enderChestCost=8, talismanCost=8;
		@ConfigRange(min=0,max=100) public double successRepairPercent=15;
		@ConfigRange(min=0,max=100) public double failureDamageMinPercent=10, failureDamageMaxPercent=35;
	}
	public static final class Mining {
		@ConfigRange(min=0,max=5) public int areaRadius=1;
		@ConfigRange(min=1,max=8192) public int treeMaxLogs=1024;
		@ConfigRange(min=1,max=64) public int treeRadius=16;
		@ConfigRange(min=1,max=256) public int treeHeightAbove=64;
		@ConfigRange(min=0,max=64) public int treeDepthBelow=6;
	}
	public static final class Sword {
		@ConfigRange(min=0.5,max=32) public double radius=3;
		@ConfigRange(min=0,max=300) public double cooldownSeconds=6;
		@ConfigRange(min=0,max=100) public int durabilityCost=4;
		@ConfigRange(min=0,max=5) public double knockback=1.35, upwardKnockback=0.35;
	}
	public static final class Bow {
		@ConfigRange(min=1,max=15) public int arrowCount=3;
		@ConfigRange(min=0,max=90) public double spreadDegrees=10;
	}
	public static final class Crossbow {
		@ConfigRange(min=4,max=256) public double maximumLineDistance=64;
		@ConfigRange(min=0.05,max=3) public double playerPullStrength=0.42;
		@ConfigRange(min=0.05,max=3) public double entityPullStrength=0.34;
		@ConfigRange(min=0.5,max=8) public double stoppingDistance=2;
	}
	public static final class AnimalArmor {
		@ConfigRange(min=0,max=100) public double activationChancePercent=40;
		@ConfigRange(min=0,max=300) public double cooldownSeconds=1;
		@ConfigRange(min=0,max=100) public double damage=2;
		@ConfigRange(min=0,max=5) public double knockback=1.15, upwardKnockback=0.3;
	}
	public static final class Armor {
		@ConfigRange(min=-100,max=500) public double leggingsWalkPercent=10, leggingsCrouchPercent=10;
		@ConfigRange(min=-5,max=5) public double leggingsSwimBonus=0.1;
		@ConfigRange(min=0,max=100) public double bootsFallReductionPercent=25;
		@ConfigRange(min=1,max=10) public int chestplateResistanceLevel=1;
		@ConfigRange(min=0,max=100) public double leatherHealth=1, goldChainHealth=2, copperIronHealth=3, diamondHealth=4, netheriteHealth=5;
		@ConfigRange(min=0,max=20) public double fracturedHealthLossPerLevel=1, fracturedHealthLossCap=10;
		@ConfigRange(min=1,max=1200) public int fullSetParticleIntervalTicks=20;
	}
	public static final class Trident {
		@ConfigRange(min=0,max=100) public double dryRiptideChancePercent=35;
		@ConfigRange(min=0,max=100) public int failedRiptideDurabilityCost=1;
		@ConfigRange(min=0,max=100) public double impalingDamagePerLevel=2.5;
	}
	public static final class Spear {
		@ConfigRange(min=0,max=100) public double lungeHungerPercent=50;
		@ConfigRange(min=0,max=5) public double lungeUpwardBoostPerLevel=0.25;
	}
	public static final class Mace {
		@ConfigRange(min=0.1,max=60) public double durationSeconds=2;
		@ConfigRange(min=0.5,max=32) public double radius=5;
		@ConfigRange(min=1,max=100) public int pullIntervalTicks=4;
		@ConfigRange(min=0,max=5) public double minimumPull=0.18, maximumPull=0.34;
		@ConfigRange(min=0,max=100) public int durabilityCost=2;
	}
	public static final class FishingRod {
		@ConfigRange(min=1,max=1000000) public double baseTeleportRange=1500;
	}
	public static final class Bundle {
		@ConfigRange(min=1,max=256) public int stableCapacity=8, fracturedMinimumCapacity=16, fracturedMaximumCapacity=48;
	}
	public static final class Talisman {
		@ConfigRange(min=1,max=1000) public int uses=8;
		@ConfigRange(min=0,max=100) public double fragmentLootChancePercent=8;
		@ConfigRange(min=0.5,max=100) public double healthAfterSave=1;
		@ConfigRange(min=0,max=3600) public double regenerationSeconds=45, absorptionSeconds=5, fireResistanceSeconds=40;
		@ConfigRange(min=1,max=10) public int regenerationLevel=2, absorptionLevel=2, fireResistanceLevel=1;
	}
	public static final class ResonantChest {
		@ConfigRange(min=1,max=64) public int maximumLinkedChests=4;
	}
}
