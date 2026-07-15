package com.tsg0d.lostfragments.menu;

import com.tsg0d.lostfragments.block.ModBlocks;
import com.tsg0d.lostfragments.component.ModComponents;
import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.ChatFormatting;
import com.tsg0d.lostfragments.LostFragments;
import com.tsg0d.lostfragments.item.ModItems;

public final class InfusionTableMenu extends AbstractContainerMenu {
	private static final int EQUIPMENT_SLOT = 0;
	private static final int CATALYST_SLOT = 1;
	private static final int RESULT_SLOT = 2;
	private static final int PLAYER_SLOT_START = 3;
	private static final int PLAYER_SLOT_END = 39;

	private final Container container;
	private final ContainerLevelAccess access;
	private final Player owner;
	private boolean updating;

	public InfusionTableMenu(int containerId, Inventory playerInventory, BlockPos pos) {
		this(containerId, playerInventory, new SimpleContainer(3),
				ContainerLevelAccess.create(playerInventory.player.level(), pos));
	}

	public InfusionTableMenu(int containerId, Inventory playerInventory, Container container,
			ContainerLevelAccess access) {
		super(ModMenus.INFUSION_TABLE, containerId);
		checkContainerSize(container, 3);
		this.container = container;
		this.access = access;
		this.owner = playerInventory.player;

		addSlot(new Slot(container, EQUIPMENT_SLOT, 44, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return InfusionService.isEligible(stack) && !InfusionService.isInfused(stack);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}

			@Override
			public void setChanged() {
				super.setChanged();
				InfusionTableMenu.this.slotsChanged(container);
			}
		});
		addSlot(new Slot(container, CATALYST_SLOT, 80, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(Items.AMETHYST_SHARD);
			}

			@Override
			public void setChanged() {
				super.setChanged();
				InfusionTableMenu.this.slotsChanged(container);
			}
		});
		addSlot(new Slot(container, RESULT_SLOT, 116, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				int required = InfusionService.requiredShards(container.getItem(EQUIPMENT_SLOT));
				int supplied = container.getItem(CATALYST_SLOT).getCount();
				container.removeItemNoUpdate(EQUIPMENT_SLOT);
				container.removeItem(CATALYST_SLOT, Math.min(required, supplied));
				InfusionTableMenu.this.slotsChanged(container);
				super.onTake(player, stack);
			}
		});

		addStandardInventorySlots(playerInventory, 8, 84);
	}

	@Override
	public void slotsChanged(Container changed) {
		super.slotsChanged(changed);
		if (updating) {
			return;
		}

		updating = true;
		ItemStack equipment = container.getItem(EQUIPMENT_SLOT);
		ItemStack catalyst = container.getItem(CATALYST_SLOT);
		ItemStack result = ItemStack.EMPTY;
		if (InfusionService.isEligible(equipment)
				&& !InfusionService.isInfused(equipment)
				&& catalyst.is(Items.AMETHYST_SHARD)) {
			int required = InfusionService.requiredShards(equipment);
			int supplied = Math.min(required, catalyst.getCount());
			boolean failed = false;
			if (!owner.level().isClientSide() && supplied < required) {
				double fractureChance = (required - supplied) / (double) required;
				failed = owner.getRandom().nextDouble() < fractureChance;
			}
			result = createInfusionResult(equipment, failed, supplied, required);
		}
		container.setItem(RESULT_SLOT, result);
		updating = false;
		broadcastChanges();
	}

	private ItemStack createInfusionResult(ItemStack equipment, boolean failed, int supplied, int required) {
		int previousFractureLevel = InfusionService.fractureLevel(equipment);
		boolean keepOriginal = failed && (equipment.is(Items.ENDER_CHEST)
				|| equipment.is(ModItems.CRACKED_CATMEN_TALISMAN) || equipment.is(Items.BOOK));
		ItemStack result;
		if (keepOriginal) {
			result = equipment.copyWithCount(1);
		} else if (equipment.is(ItemTags.BUNDLES)) {
			result = new ItemStack(ModItems.INFUSED_BUNDLE);
			if (equipment.has(DataComponents.BUNDLE_CONTENTS)) {
				result.set(DataComponents.BUNDLE_CONTENTS, equipment.get(DataComponents.BUNDLE_CONTENTS));
			}
			result.set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(equipment.getItem()));
			result.set(DataComponents.ITEM_NAME, Component.translatable(
					"item.lostfragments.infused_colored_bundle", equipment.getHoverName()));
		} else if (equipment.is(Items.ENDER_CHEST)) {
			result = new ItemStack(ModBlocks.RESONANT_ENDER_CHEST);
		} else if (equipment.is(ModItems.CRACKED_CATMEN_TALISMAN)) {
			result = new ItemStack(ModItems.CATMEN_TALISMAN);
		} else if (equipment.is(Items.BOOK)) {
			result = new ItemStack(ModItems.BOOK_OF_INFUSION);
		} else {
			result = equipment.copyWithCount(1);
		}

		result.remove(ModComponents.FRACTURED_INFUSION);
		result.remove(ModComponents.FRACTURE_LEVEL);
		result.remove(DataComponents.CUSTOM_NAME);
		result.remove(ModComponents.BUNDLE_CAPACITY);
		result.set(ModComponents.AMETHYST_INFUSED, true);
		if (equipment.has(DataComponents.ENCHANTMENTS)) {
			result.set(DataComponents.ENCHANTMENTS, equipment.get(DataComponents.ENCHANTMENTS));
		}
		if (equipment.isEnchanted()) {
			result.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
		} else {
			result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
		}
		if (equipment.is(Items.FISHING_ROD)) {
			result.set(DataComponents.ITEM_MODEL, LostFragments.id("infused_fishing_rod"));
		}
		if (failed) {
			result.set(ModComponents.FRACTURED_INFUSION, true);
			result.set(ModComponents.FRACTURE_LEVEL, previousFractureLevel + 1);
			applyFailureDamage(result, supplied, required);
			result.set(DataComponents.CUSTOM_NAME, Component.translatable(
					"item.lostfragments.failed_infusion", result.getHoverName())
					.withStyle(ChatFormatting.RED));
			if (result.is(ModItems.INFUSED_BUNDLE)) {
				int oldCapacity = equipment.getOrDefault(ModComponents.BUNDLE_CAPACITY, 0);
				result.set(ModComponents.BUNDLE_CAPACITY,
						oldCapacity > 0 ? oldCapacity : 16 + owner.getRandom().nextInt(33));
			}
		} else {
			repairSuccessfulInfusion(result);
		}
		return result;
	}

	private static void applyFailureDamage(ItemStack stack, int supplied, int required) {
		if (!stack.isDamageableItem()) {
			return;
		}
		double missingRatio = Math.max(0.0, (required - supplied) / (double) required);
		double damageRatio = 0.10 + 0.25 * missingRatio;
		int durabilityDamage = Math.max(1, (int) Math.ceil(stack.getMaxDamage() * damageRatio));
		stack.setDamageValue(Math.min(stack.getMaxDamage() - 1,
				stack.getDamageValue() + durabilityDamage));
	}

	private static void repairSuccessfulInfusion(ItemStack stack) {
		if (!stack.isDamageableItem() || stack.getDamageValue() == 0) {
			return;
		}
		int repaired = Math.max(1, (int) Math.ceil(stack.getMaxDamage() * 0.15));
		stack.setDamageValue(Math.max(0, stack.getDamageValue() - repaired));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index == RESULT_SLOT) {
			if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stack);
		} else if (index < PLAYER_SLOT_START) {
			if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) {
				return ItemStack.EMPTY;
			}
		} else if (InfusionService.isEligible(stack) && !InfusionService.isInfused(stack)) {
			if (!moveItemStackTo(stack, EQUIPMENT_SLOT, EQUIPMENT_SLOT + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.is(Items.AMETHYST_SHARD)) {
			if (!moveItemStackTo(stack, CATALYST_SLOT, CATALYST_SLOT + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return original;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, ModBlocks.INFUSION_TABLE);
	}

	public int getRequiredShards() {
		ItemStack equipment = container.getItem(EQUIPMENT_SLOT);
		return InfusionService.isEligible(equipment) ? InfusionService.requiredShards(equipment) : 0;
	}

	public int getSuppliedShards() {
		return container.getItem(CATALYST_SLOT).getCount();
	}

	public int getFractureRiskPercent() {
		int required = getRequiredShards();
		if (required == 0) {
			return 0;
		}
		int supplied = Math.min(required, getSuppliedShards());
		return (int) Math.round((required - supplied) * 100.0 / required);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		container.setItem(RESULT_SLOT, ItemStack.EMPTY);
		clearContainer(player, container);
	}
}
