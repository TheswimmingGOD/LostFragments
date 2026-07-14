package com.tsg0d.lostfragments.item;

import com.tsg0d.lostfragments.LostFragments;
import com.tsg0d.lostfragments.block.ModBlocks;
import com.tsg0d.lostfragments.component.ModComponents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;
import java.util.List;

public final class ModItems {
	private static final ResourceKey<Item> INFUSED_BUNDLE_KEY = ResourceKey.create(Registries.ITEM, LostFragments.id("infused_bundle"));
	public static final Item INFUSED_BUNDLE = Registry.register(BuiltInRegistries.ITEM, INFUSED_BUNDLE_KEY,
			new InfusedBundleItem(infusedProperties().setId(INFUSED_BUNDLE_KEY).stacksTo(1)));
	public static final Item CATMEN_TALISMAN = register("catmen_talisman",
			infusedProperties().stacksTo(1).component(ModComponents.TALISMAN_USES, 8));
	public static final Item CRACKED_CATMEN_TALISMAN = register("cracked_catmen_talisman",
			new Item.Properties().stacksTo(1));
	public static final Item LOST_CORNER_FRAGMENT = register("lost_corner_fragment", new Item.Properties());
	public static final Item LOST_SIDE_FRAGMENT = register("lost_side_fragment", new Item.Properties());
	public static final Item BOOK_OF_INFUSION = registerWrittenBook("book_of_infusion");
	public static final CreativeModeTab LOST_FRAGMENTS_TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			LostFragments.id("lost_fragments"),
			FabricCreativeModeTab.builder()
					.title(Component.translatable("itemGroup.lostfragments"))
					.icon(() -> new ItemStack(ModBlocks.INFUSION_TABLE))
					.displayItems((parameters, output) -> {
						output.accept(ModBlocks.INFUSION_TABLE);
						output.accept(CRACKED_CATMEN_TALISMAN);
						output.accept(LOST_CORNER_FRAGMENT);
						output.accept(LOST_SIDE_FRAGMENT);
					})
					.build());

	private ModItems() {}

	private static Item register(String name, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, LostFragments.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties.setId(key)));
	}

	private static Item.Properties infusedProperties() {
		return new Item.Properties()
				.component(ModComponents.AMETHYST_INFUSED, true)
				.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
	}

	private static Item registerWrittenBook(String name) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, LostFragments.id(name));
		Item.Properties properties = infusedProperties().setId(key).stacksTo(1)
				.component(DataComponents.WRITTEN_BOOK_CONTENT, guideContent());
		return Registry.register(BuiltInRegistries.ITEM, key, new WrittenBookItem(properties));
	}

	private static WrittenBookContent guideContent() {
		List<String> text = List.of(
				"LOST FRAGMENTS\n\nWelcome! Every player receives this book once when first joining a world. It explains amethyst infusion and every current item.",
				"INFUSION TABLE\n\nRecipe:\nA E A\nO C O\nO O O\n\nA: Amethyst Block\nE: Echo Shard\nO: Obsidian\nC: Crafting Table",
				"HOW TO INFUSE\n\nPlace an eligible item left, amethyst shards in the middle, then take the result. Purple means stable. Red and 'INFUSION FAILED' means fractured.",
				"FAILURE RISK\n\nFull shard cost is safe. With fewer shards, risk = missing / required. Failed items have no infused ability. Re-infuse them for twice the normal cost.",
				"SHARD COSTS I\n\n2: Shovel, Hoe, Clock\n3: Pickaxe, Axe, Sword, Compass\n4: Helmet, Boots, Fishing Rod, Bundle",
				"SHARD COSTS II\n\n5: Leggings\n6: Chestplate\n8: Ender Chest, Cracked Catmen Talisman\n1: Book\n\nModded tagged tools and armor are supported.",
				"MINING TOOLS\n\nSneak while breaking. Pickaxes and shovels mine 3x3. Axes fell connected logs, including built structures. Extra blocks use normal durability.",
				"INFUSED HOE\n\nSneak-right-click the top of soil to till 3x3. Sneak-break crops, flowers, grass, dry grass, ferns, large ferns, or leaf litter to clear 3x3.",
				"INFUSED SWORD\n\nRight-click for a harmless knockback pulse in a 3-block radius. It costs 4 durability and has a 6-second cooldown, even if it misses.",
				"INFUSED ARMOR I\n\nHelmet: Night Vision\nChestplate: Resistance I\nLeggings: +10% walking and crouching speed, plus improved swimming.",
				"INFUSED ARMOR II\n\nBoots: 25% less fall damage and no farmland trampling. A stable same-material full set adds health by tier and creates a purple aura.",
				"TELEPORT ROD\n\nHold a pearl in the other hand and sneak-right-click to load. Cast to save a point. Right-click later to teleport. Same dimension; base range 1500.",
				"ENDER REACH\n\nOnly Mending, Unbreaking, and Ender Reach remain on infused rods. Reach I/II/III raises range to 3000/4500/6000 blocks.",
				"COMPASS & CLOCK\n\nUse an infused compass on a player to track them; sneak-right-click air to clear. The clock shows Overworld day and time in every dimension.",
				"RESONANT CHEST\n\nInfuse an Ender Chest. Put an item in its key slot to choose a frequency. Up to 4 linked chests share 27 slots across dimensions. Hoppers work.",
				"CHEST SAFETY\n\nThe key slot is not automated. Breaking the last chest on a frequency drops all shared contents. Pre-key items merge safely when a key is chosen.",
				"INFUSED BUNDLE\n\nAny colour works. Stable: 8 non-stackable items. Failed: stackable items only, with random 16-48 capacity. Primary-click inserts; secondary removes.",
				"CATMEN TALISMAN\n\nCraft the cracked form with Corner Fragments in the corners and Side Fragments on the sides. Infuse with 8 shards. It prevents 8 deaths, even void.",
				"FRAGMENTS\n\nOne random fragment has an 8% chance in Ancient/End Cities, temples, shipwrecks, buried treasure, and listed suspicious sand or gravel archaeology.",
				"FAILED ITEMS\n\nRed items are inactive. Put one back in the Infusion Table. Its retry cost is double the listed normal cost; paying the full retry cost is guaranteed safe."
		);
		return new WrittenBookContent(Filterable.passThrough("Book of Infusion"), "tsg0d", 0,
				text.stream().map(page -> Filterable.passThrough((Component) Component.literal(page))).toList(), true);
	}

	public static void initialize() {
		LostFragments.LOGGER.info("Registering Lost Fragments items and creative tab");
	}
}
