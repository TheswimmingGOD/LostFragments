package com.tsg0d.lostfragments.client.screen;

import com.tsg0d.lostfragments.menu.InfusionTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class InfusionTableScreen extends AbstractContainerScreen<InfusionTableMenu> {
	public InfusionTableScreen(InfusionTableMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 166);
		this.titleLabelY += 2;
		this.inventoryLabelY = 72;
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Amethyst-themed panel rendered without requiring a large GUI texture.
		graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF24152F);
		graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF4D2E62);
		graphics.fill(leftPos + 7, topPos + 7, leftPos + imageWidth - 7, topPos + imageHeight - 7, 0xFFC9B1D5);

		// Slot wells for equipment, shard, result, and the player inventory.
		drawSlot(graphics, leftPos + 43, topPos + 34);
		drawSlot(graphics, leftPos + 79, topPos + 34);
		drawSlot(graphics, leftPos + 115, topPos + 34);
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				drawSlot(graphics, leftPos + 7 + column * 18, topPos + 83 + row * 18);
			}
		}
		for (int column = 0; column < 9; column++) {
			drawSlot(graphics, leftPos + 7 + column * 18, topPos + 141);
		}

		// Direction arrows between the three table slots.
		graphics.fill(leftPos + 65, topPos + 42, leftPos + 73, topPos + 45, 0xFF7B2CBF);
		graphics.fill(leftPos + 101, topPos + 42, leftPos + 109, topPos + 45, 0xFF7B2CBF);
		super.extractContents(graphics, mouseX, mouseY, delta);
	}

	private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(x, y, x + 18, y + 18, 0xFF2B1738);
		graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFE8DDF0);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		int required = menu.getRequiredShards();
		if (required > 0) {
			Component status = Component.translatable(
					"container.lostfragments.infusion_status",
					Math.min(menu.getSuppliedShards(), required),
					required,
					menu.getFractureRiskPercent()
			);
			graphics.centeredText(font, status, imageWidth / 2, 20, 0xFF3B174D);
		}
	}
}
