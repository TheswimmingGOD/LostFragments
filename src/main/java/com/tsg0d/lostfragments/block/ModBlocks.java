package com.tsg0d.lostfragments.block;

import com.tsg0d.lostfragments.LostFragments;
import com.tsg0d.lostfragments.component.ModComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import java.util.function.Function;

public final class ModBlocks {
	public static final Block INFUSION_TABLE = register(
			"infusion_table",
			InfusionTableBlock::new,
			BlockBehaviour.Properties.of()
					.strength(5.0F, 1_200.0F)
					.sound(SoundType.AMETHYST)
					.noOcclusion()
					.requiresCorrectToolForDrops(),
			true, false
	);
	public static final Block RESONANT_ENDER_CHEST = register(
			"resonant_ender_chest", ResonantEnderChestBlock::new,
			BlockBehaviour.Properties.of().strength(22.5F, 1_200.0F).sound(SoundType.STONE).noOcclusion(), true, true);
	public static final BlockEntityType<ResonantEnderChestBlockEntity> RESONANT_ENDER_CHEST_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, LostFragments.id("resonant_ender_chest"),
			FabricBlockEntityTypeBuilder.create(ResonantEnderChestBlockEntity::new, RESONANT_ENDER_CHEST).build());

	private ModBlocks() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
			BlockBehaviour.Properties properties, boolean registerItem, boolean infusedItem) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, LostFragments.id(name));
		Block block = factory.apply(properties.setId(blockKey));

		if (registerItem) {
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, LostFragments.id(name));
			Item.Properties itemProperties = new Item.Properties();
			if (infusedItem) {
				itemProperties.component(ModComponents.AMETHYST_INFUSED, true)
						.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
			}
			Registry.register(BuiltInRegistries.ITEM, itemKey,
					new BlockItem(block, itemProperties.setId(itemKey).useBlockDescriptionPrefix()));
		}

		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	public static void initialize() {
		LostFragments.LOGGER.info("Registering Lost Fragments blocks");
	}
}
