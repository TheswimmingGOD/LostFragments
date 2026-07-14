package com.tsg0d.lostfragments.menu;

import com.tsg0d.lostfragments.block.ResonantEnderChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ResonantEnderChestMenu extends AbstractContainerMenu {
	private final Container chest;

	public ResonantEnderChestMenu(int id, Inventory inventory, BlockPos pos) {
		this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof ResonantEnderChestBlockEntity chest
				? chest : new SimpleContainer(28));
	}

	public ResonantEnderChestMenu(int id, Inventory inventory, Container chest) {
		super(ModMenus.RESONANT_ENDER_CHEST, id);
		this.chest = chest;
		checkContainerSize(chest, 28);
		addSlot(new Slot(chest, 0, 8, 18) {
			@Override public boolean mayPlace(ItemStack stack) {
				return chest instanceof ResonantEnderChestBlockEntity resonant && resonant.canBind(stack);
			}
			@Override public boolean mayPickup(Player player) { return false; }
			@Override public int getMaxStackSize() { return 1; }
		});
		for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
			addSlot(new Slot(chest, 1 + col + row * 9, 8 + col * 18, 50 + row * 18));
		addStandardInventorySlots(inventory, 8, 118);
	}

	@Override public boolean stillValid(Player player) { return chest.stillValid(player); }
	@Override public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index < 28) {
			if (!moveItemStackTo(stack, 28, slots.size(), true)) return ItemStack.EMPTY;
		} else if (!slots.get(0).hasItem() && slots.get(0).mayPlace(stack)) {
			if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, 1, 28, false)) return ItemStack.EMPTY;
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
		return copy;
	}
}
