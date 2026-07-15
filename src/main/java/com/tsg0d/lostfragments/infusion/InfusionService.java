package com.tsg0d.lostfragments.infusion;

import com.tsg0d.lostfragments.block.ModBlocks;
import com.tsg0d.lostfragments.component.ModComponents;
import com.tsg0d.lostfragments.menu.InfusionTableMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.TridentItem;
import com.tsg0d.lostfragments.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.enchantment.Enchantments;

public final class InfusionService {
	private InfusionService() {
	}

	public static boolean isEligible(ItemStack stack) {
		if (stack.is(Items.FISHING_ROD) && hasForbiddenRodEnchantment(stack)) return false;
		return stack.is(ItemTags.PICKAXES)
				|| stack.is(ItemTags.SHOVELS)
				|| stack.is(ItemTags.AXES)
				|| stack.is(ItemTags.HOES)
				|| stack.is(ItemTags.SWORDS)
				|| stack.is(ItemTags.HEAD_ARMOR)
				|| stack.is(ItemTags.CHEST_ARMOR)
				|| stack.is(ItemTags.LEG_ARMOR)
				|| stack.is(ItemTags.FOOT_ARMOR)
				|| stack.is(Items.FISHING_ROD)
				|| stack.is(Items.COMPASS)
				|| stack.is(Items.CLOCK)
				|| stack.is(ItemTags.BUNDLES)
				|| stack.is(ModItems.INFUSED_BUNDLE)
				|| stack.is(Items.ENDER_CHEST)
				|| stack.is(ModBlocks.RESONANT_ENDER_CHEST.asItem())
				|| stack.is(ModItems.CRACKED_CATMEN_TALISMAN)
				|| stack.is(ModItems.CATMEN_TALISMAN)
				|| stack.is(Items.BOOK)
				|| stack.is(ModItems.BOOK_OF_INFUSION)
				|| stack.getItem() instanceof BowItem
				|| stack.getItem() instanceof TridentItem
				|| isAnimalArmor(stack);
				
				
				
	}

	private static boolean hasForbiddenRodEnchantment(ItemStack stack) {
		return stack.getOrDefault(DataComponents.ENCHANTMENTS,
				net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).keySet().stream()
				.anyMatch(enchantment -> !enchantment.is(Enchantments.UNBREAKING)
						&& !enchantment.is(Enchantments.MENDING)
						&& !enchantment.is(TeleportRodAbilities.ENDER_REACH));
	}

	public static boolean isInfused(ItemStack stack) {
		return stack.getOrDefault(ModComponents.AMETHYST_INFUSED, false) && !isFractured(stack);
	}

	public static boolean hasInfusion(ItemStack stack) {
		return stack.getOrDefault(ModComponents.AMETHYST_INFUSED, false);
	}

	public static boolean isFractured(ItemStack stack) {
		return fractureLevel(stack) > 0;
	}

	public static int fractureLevel(ItemStack stack) {
		int level = stack.getOrDefault(ModComponents.FRACTURE_LEVEL, 0);
		if (level == 0 && stack.getOrDefault(ModComponents.FRACTURED_INFUSION, false)) {
			return 1;
		}
		return Math.max(0, level);
	}

	public static boolean isAnimalArmor(ItemStack stack) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		return equippable != null && equippable.slot() == EquipmentSlot.BODY;
	}

	public static int requiredShards(ItemStack stack) {
		int base = baseShardCost(stack);
		return base * (fractureLevel(stack) + 1);
	}

	private static int baseShardCost(ItemStack stack) {
		if (stack.is(ItemTags.CHEST_ARMOR)) return 6;
		if (stack.is(ItemTags.LEG_ARMOR)) return 5;
		if (stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)) return 4;
		if (stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SWORDS)) return 3;
		if (stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)) return 2;
		if (stack.is(Items.FISHING_ROD)) return 4;
		if (stack.is(Items.COMPASS)) return 3;
		if (stack.is(Items.CLOCK)) return 2;
		if (stack.is(ItemTags.BUNDLES) || stack.is(ModItems.INFUSED_BUNDLE)) return 4;
		if (stack.is(Items.ENDER_CHEST) || stack.is(ModBlocks.RESONANT_ENDER_CHEST.asItem())) return 8;
		if (stack.is(ModItems.CRACKED_CATMEN_TALISMAN) || stack.is(ModItems.CATMEN_TALISMAN)) return 8;
		if (stack.is(Items.BOOK) || stack.is(ModItems.BOOK_OF_INFUSION)) return 1;
		if (stack.getItem() instanceof BowItem || isAnimalArmor(stack)) return 4;
		if (stack.getItem() instanceof TridentItem) return 5;
		return 1;
	}

	public static void initialize() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack held = player.getItemInHand(hand);
			if (held.is(ModBlocks.RESONANT_ENDER_CHEST.asItem()) && isFractured(held)) {
				return InteractionResult.FAIL;
			}
			if (hand != InteractionHand.MAIN_HAND
					|| !level.getBlockState(hit.getBlockPos()).is(ModBlocks.INFUSION_TABLE)) {
				return InteractionResult.PASS;
			}

			if (player instanceof ServerPlayer serverPlayer) {
				BlockPos pos = hit.getBlockPos();
				serverPlayer.openMenu(new ExtendedMenuProvider<BlockPos>() {
					@Override
					public BlockPos getScreenOpeningData(ServerPlayer player) {
						return pos;
					}

					@Override
					public Component getDisplayName() {
						return Component.translatable("container.lostfragments.infusion_table");
					}

					@Override
					public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
						return new InfusionTableMenu(id, inventory, new net.minecraft.world.SimpleContainer(3),
								ContainerLevelAccess.create(level, pos));
					}
				});
			}

			return InteractionResult.SUCCESS;
		});

		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			return stack.is(ModItems.BOOK_OF_INFUSION) && isFractured(stack)
					? InteractionResult.FAIL : InteractionResult.PASS;
		});
	}
}
