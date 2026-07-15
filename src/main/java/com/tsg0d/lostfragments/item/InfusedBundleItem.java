package com.tsg0d.lostfragments.item;

import com.tsg0d.lostfragments.component.ModComponents;
import com.tsg0d.lostfragments.infusion.InfusionService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;
import java.util.List;

public final class InfusedBundleItem extends Item {
	public InfusedBundleItem(Properties properties) {
		super(properties.component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY));
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack bundle, ItemStack carried, Slot slot,
			ClickAction action, Player player, SlotAccess carriedAccess) {
		if (action == ClickAction.PRIMARY && !carried.isEmpty()) return insert(bundle, carried);
		if (action == ClickAction.SECONDARY && carried.isEmpty()) return remove(bundle, carriedAccess);
		return false;
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack bundle, Slot slot, ClickAction action, Player player) {
		if (action == ClickAction.PRIMARY && slot.hasItem()) return insert(bundle, slot.getItem());
		if (action == ClickAction.SECONDARY && !slot.hasItem()) {
			return remove(bundle, SlotAccess.of(slot::getItem, slot::set));
		}
		return false;
	}

	private static boolean insert(ItemStack bundle, ItemStack source) {
		if (!source.getItem().canFitInsideContainerItems() || source.getItem() instanceof InfusedBundleItem) return false;
		boolean fractured = InfusionService.isFractured(bundle);
		if (fractured ? source.getMaxStackSize() <= 1 : source.getMaxStackSize() > 1) return false;
		List<ItemStack> items = contents(bundle);
		int used = items.stream().mapToInt(ItemStack::getCount).sum();
		int capacity = fractured ? bundle.getOrDefault(ModComponents.BUNDLE_CAPACITY,
				LostFragmentsConfig.get().bundle.fracturedMinimumCapacity)
				: LostFragmentsConfig.get().bundle.stableCapacity;
		int move = Math.min(source.getCount(), capacity - used);
		if (move <= 0) return false;
		for (ItemStack stored : items) {
			if (ItemStack.isSameItemSameComponents(stored, source) && stored.getCount() + move <= stored.getMaxStackSize()) {
				stored.grow(move);
				source.shrink(move);
				setContents(bundle, items);
				return true;
			}
		}
		items.add(source.copyWithCount(move));
		source.shrink(move);
		setContents(bundle, items);
		return true;
	}

	private static boolean remove(ItemStack bundle, SlotAccess destination) {
		List<ItemStack> items = contents(bundle);
		if (items.isEmpty()) return false;
		ItemStack removed = items.remove(items.size() - 1);
		if (!destination.set(removed)) return false;
		setContents(bundle, items);
		return true;
	}

	private static List<ItemStack> contents(ItemStack bundle) {
		List<ItemStack> result = new ArrayList<>();
		bundle.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)
				.items().forEach(template -> result.add(template.create()));
		return result;
	}

	private static void setContents(ItemStack bundle, List<ItemStack> items) {
		bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(items.stream()
				.filter(stack -> !stack.isEmpty()).map(ItemStackTemplate::fromNonEmptyStack).toList()));
	}
}
