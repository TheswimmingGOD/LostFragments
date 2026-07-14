package com.tsg0d.lostfragments.client.screen;

import com.tsg0d.lostfragments.menu.ResonantEnderChestMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ResonantEnderChestScreen extends AbstractContainerScreen<ResonantEnderChestMenu> {
	public ResonantEnderChestScreen(ResonantEnderChestMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 200);
		inventoryLabelY = 106;
	}

	@Override public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF160C22);
		graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF4B176B);
		graphics.fill(leftPos + 7, topPos + 7, leftPos + imageWidth - 7, topPos + imageHeight - 7, 0xFFD0B7DA);
		drawSlot(graphics, leftPos + 7, topPos + 17, 0xFF7B2CBF);
		for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
			drawSlot(graphics, leftPos + 7 + col * 18, topPos + 49 + row * 18, 0xFF271331);
		for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
			drawSlot(graphics, leftPos + 7 + col * 18, topPos + 117 + row * 18, 0xFF271331);
		for (int col = 0; col < 9; col++) drawSlot(graphics, leftPos + 7 + col * 18, topPos + 175, 0xFF271331);
		super.extractContents(graphics, mouseX, mouseY, delta);
	}

	private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y, int color) {
		graphics.fill(x, y, x + 18, y + 18, color);
		graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFE7DCEC);
	}
}
