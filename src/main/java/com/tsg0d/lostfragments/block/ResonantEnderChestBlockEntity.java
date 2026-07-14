package com.tsg0d.lostfragments.block;

import com.tsg0d.lostfragments.menu.ResonantEnderChestMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ResonantEnderChestBlockEntity extends BlockEntity implements WorldlyContainer,
		ExtendedMenuProvider<BlockPos>, LidBlockEntity {
	private static final int[] AUTOMATION_SLOTS = java.util.stream.IntStream.range(1, 28).toArray();
	private NonNullList<ItemStack> migrationItems = NonNullList.withSize(27, ItemStack.EMPTY);
	private NonNullList<ItemStack> clientItems = NonNullList.withSize(27, ItemStack.EMPTY);
	private ItemStack frequencyItem = ItemStack.EMPTY;
	private String frequency = "";

	public ResonantEnderChestBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.RESONANT_ENDER_CHEST_ENTITY, pos, state);
	}

	@Override public BlockPos getScreenOpeningData(ServerPlayer player) { return worldPosition; }
	@Override public Component getDisplayName() { return Component.translatable("container.lostfragments.resonant_ender_chest"); }
	@Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new ResonantEnderChestMenu(id, inventory, this);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (!level.isClientSide() && !frequency.isEmpty()) {
			data().register(frequency, endpointId(), migrationItems);
			migrationItems = NonNullList.withSize(27, ItemStack.EMPTY);
		}
	}

	private ResonantChestSavedData data() {
		return ResonantChestSavedData.get(level.getServer());
	}

	private String endpointId() {
		return level.dimension().identifier() + "@" + worldPosition.asLong();
	}

	private ResonantChestSavedData.Network network() {
		if (level == null || level.isClientSide() || frequency.isEmpty()) return null;
		ResonantChestSavedData.Network network = data().getNetwork(frequency);
		return network != null ? network : data().register(frequency, endpointId(), migrationItems);
	}

	public boolean canBind(ItemStack stack) {
		if (!frequencyItem.isEmpty() || stack.isEmpty()) return false;
		if (level == null || level.isClientSide()) return true;
		String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		if (!data().canRegister(key, endpointId())) return false;
		ResonantChestSavedData.Network target = data().getNetwork(key);
		return target == null || canMerge(clientItems, target.items);
	}

	private void bind(ItemStack stack) {
		if (!frequencyItem.isEmpty() || stack.isEmpty()) return;
		frequencyItem = stack.copyWithCount(1);
		frequency = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		if (level != null && !level.isClientSide()) {
			ResonantChestSavedData.Network target = data().getNetwork(frequency);
			if (target == null) {
				data().register(frequency, endpointId(), clientItems);
			} else {
				data().register(frequency, endpointId(), migrationItems);
				merge(clientItems, target.items);
				data().setDirty();
			}
			clientItems = NonNullList.withSize(27, ItemStack.EMPTY);
		}
		setChanged();
	}

	private static boolean canMerge(NonNullList<ItemStack> source, NonNullList<ItemStack> target) {
		NonNullList<ItemStack> sourceCopy = NonNullList.withSize(27, ItemStack.EMPTY);
		NonNullList<ItemStack> targetCopy = NonNullList.withSize(27, ItemStack.EMPTY);
		for (int i = 0; i < 27; i++) {
			sourceCopy.set(i, source.get(i).copy());
			targetCopy.set(i, target.get(i).copy());
		}
		return merge(sourceCopy, targetCopy);
	}

	private static boolean merge(NonNullList<ItemStack> source, NonNullList<ItemStack> target) {
		for (ItemStack sourceStack : source) {
			if (sourceStack.isEmpty()) continue;
			for (ItemStack targetStack : target) {
				if (sourceStack.isEmpty()) break;
				if (ItemStack.isSameItemSameComponents(sourceStack, targetStack)) {
					int move = Math.min(sourceStack.getCount(), targetStack.getMaxStackSize() - targetStack.getCount());
					if (move > 0) { targetStack.grow(move); sourceStack.shrink(move); }
				}
			}
			while (!sourceStack.isEmpty()) {
				int empty = -1;
				for (int i = 0; i < target.size(); i++) if (target.get(i).isEmpty()) { empty = i; break; }
				if (empty < 0) return false;
				int move = Math.min(sourceStack.getCount(), sourceStack.getMaxStackSize());
				target.set(empty, sourceStack.copyWithCount(move));
				sourceStack.shrink(move);
			}
		}
		return true;
	}

	private NonNullList<ItemStack> storage() {
		ResonantChestSavedData.Network network = network();
		return network == null ? clientItems : network.items;
	}

	private void storageChanged() {
		setChanged();
		if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (level != null && !level.isClientSide() && !frequency.isEmpty()) data().setDirty();
	}

	public void dropForBreak() {
		if (level == null) return;
		if (!frequencyItem.isEmpty()) {
			Containers.dropContents(level, worldPosition,
					new net.minecraft.world.SimpleContainer(frequencyItem.copy()));
		}
		if (!level.isClientSide() && !frequency.isEmpty()) {
			NonNullList<ItemStack> finalContents = data().removeEndpoint(frequency, endpointId());
			if (finalContents.stream().anyMatch(stack -> !stack.isEmpty())) {
				Containers.dropContents(level, worldPosition, finalContents);
			}
		}
		frequencyItem = ItemStack.EMPTY;
		frequency = "";
		clientItems = NonNullList.withSize(27, ItemStack.EMPTY);
	}

	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		// Shared contents live only in ResonantChestSavedData. Never save a second copy here.
		if (frequency.isEmpty()) ContainerHelper.saveAllItems(output, clientItems);
		if (!frequencyItem.isEmpty()) output.store("frequency_item", ItemStack.CODEC, frequencyItem);
		if (!frequency.isEmpty()) output.putString("frequency", frequency);
	}

	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		// Read the former per-chest format once so existing worlds migrate without losing items.
		migrationItems = NonNullList.withSize(27, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, migrationItems);
		frequencyItem = input.read("frequency_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		frequency = input.getStringOr("frequency", "");
		clientItems = frequency.isEmpty() ? migrationItems : NonNullList.withSize(27, ItemStack.EMPTY);
	}

	@Override public int getContainerSize() { return 28; }
	@Override public boolean isEmpty() { return frequencyItem.isEmpty() && storage().stream().allMatch(ItemStack::isEmpty); }
	@Override public ItemStack getItem(int slot) { return slot == 0 ? frequencyItem : storage().get(slot - 1); }
	@Override public ItemStack removeItem(int slot, int amount) {
		if (slot == 0) return ItemStack.EMPTY;
		ItemStack result = ContainerHelper.removeItem(storage(), slot - 1, amount);
		if (!result.isEmpty()) storageChanged();
		return result;
	}
	@Override public ItemStack removeItemNoUpdate(int slot) {
		if (slot == 0) return ItemStack.EMPTY;
		ItemStack result = ContainerHelper.takeItem(storage(), slot - 1);
		if (!result.isEmpty()) storageChanged();
		return result;
	}
	@Override public void setItem(int slot, ItemStack stack) {
		if (slot == 0) { if (canBind(stack)) bind(stack); return; }
		storage().set(slot - 1, stack);
		storageChanged();
	}
	@Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
	@Override public void clearContent() {
		for (int i = 0; i < 27; i++) storage().set(i, ItemStack.EMPTY);
		storageChanged();
	}
	@Override public int[] getSlotsForFace(Direction side) { return AUTOMATION_SLOTS; }
	@Override public float getOpenNess(float partialTick) { return 0.0F; }
	@Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return slot > 0; }
	@Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return slot > 0; }
}
