package com.tsg0d.lostfragments.client;

import com.tsg0d.lostfragments.component.ModComponents;
import com.tsg0d.lostfragments.client.screen.InfusionTableScreen;
import com.tsg0d.lostfragments.client.screen.ResonantEnderChestScreen;
import com.tsg0d.lostfragments.menu.ModMenus;
import com.tsg0d.lostfragments.infusion.UtilityItemAbilities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ExtractItemDecorationsCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import com.tsg0d.lostfragments.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import com.tsg0d.lostfragments.block.ModBlocks;
import com.tsg0d.lostfragments.client.render.ResonantEnderChestRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import com.tsg0d.lostfragments.network.TalismanActivationPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;

public final class LostFragmentsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.INFUSION_TABLE, InfusionTableScreen::new);
		MenuScreens.register(ModMenus.RESONANT_ENDER_CHEST, ResonantEnderChestScreen::new);
		BlockEntityRenderers.register(ModBlocks.RESONANT_ENDER_CHEST_ENTITY, ResonantEnderChestRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(TalismanActivationPayload.TYPE, (payload, context) ->
				context.client().execute(() -> context.client().gameRenderer
						.displayItemActivation(new ItemStack(ModItems.CATMEN_TALISMAN))));
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			if (stack.has(ModComponents.AMETHYST_INFUSED) && !stack.has(ModComponents.FRACTURED_INFUSION)) {
				lines.add(Component.translatable("tooltip.lostfragments.amethyst_infused")
						.withStyle(ChatFormatting.LIGHT_PURPLE));
			}
			if (stack.has(ModComponents.FRACTURED_INFUSION)) {
				lines.add(Component.translatable("tooltip.lostfragments.infusion_failed")
						.withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
				lines.add(Component.translatable("tooltip.lostfragments.retry_infusion")
						.withStyle(ChatFormatting.YELLOW));
				boolean armor = stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR)
						|| stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR);
				if (!stack.is(ModItems.INFUSED_BUNDLE)) {
					lines.add(Component.translatable(armor
							? "tooltip.lostfragments.fractured_armor"
							: "tooltip.lostfragments.fractured_infusion")
							.withStyle(ChatFormatting.RED));
				}
			}
			String trackedName = stack.get(ModComponents.TRACKED_PLAYER_NAME);
			if (stack.is(Items.COMPASS) && stack.has(ModComponents.AMETHYST_INFUSED)
					&& !stack.has(ModComponents.FRACTURED_INFUSION)) {
				lines.add(Component.translatable("tooltip.lostfragments.tracking",
						trackedName == null ? Component.translatable("tooltip.lostfragments.no_target") : trackedName)
						.withStyle(ChatFormatting.AQUA));
			}
			if (stack.is(Items.CLOCK) && stack.has(ModComponents.AMETHYST_INFUSED)
					&& !stack.has(ModComponents.FRACTURED_INFUSION)
					&& Minecraft.getInstance().level != null) {
				lines.add(UtilityItemAbilities.timeText(UtilityItemAbilities.overworldTime(
						Minecraft.getInstance().level.registryAccess(),
						Minecraft.getInstance().level.clockManager()))
						.copy().withStyle(ChatFormatting.GOLD));
			}
			if (stack.is(Items.FISHING_ROD) && stack.has(ModComponents.AMETHYST_INFUSED)
					&& !stack.has(ModComponents.FRACTURED_INFUSION)) {
				if (stack.getOrDefault(ModComponents.ENDER_PEARL_LOADED, false))
					lines.add(Component.translatable("tooltip.lostfragments.rod_loaded").withStyle(ChatFormatting.DARK_PURPLE));
				Long packed = stack.get(ModComponents.TELEPORT_POSITION);
				if (packed != null) {
					BlockPos pos = BlockPos.of(packed);
					lines.add(Component.translatable("tooltip.lostfragments.rod_point", pos.getX(), pos.getY(), pos.getZ())
							.withStyle(ChatFormatting.AQUA));
				}
			}
			if (stack.is(ModItems.INFUSED_BUNDLE)) {
				int used = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).items().stream()
						.mapToInt(template -> template.count()).sum();
				int capacity = stack.getOrDefault(ModComponents.BUNDLE_CAPACITY, 8);
				lines.add(Component.translatable("tooltip.lostfragments.bundle_capacity", used, capacity)
						.withStyle(ChatFormatting.LIGHT_PURPLE));
				lines.add(Component.translatable(stack.has(ModComponents.FRACTURED_INFUSION)
						? "tooltip.lostfragments.bundle_fractured" : "tooltip.lostfragments.bundle_stable")
						.withStyle(ChatFormatting.GRAY));
			}
			if (stack.is(ModItems.CATMEN_TALISMAN) && !stack.has(ModComponents.FRACTURED_INFUSION)) {
				lines.add(Component.translatable("tooltip.lostfragments.talisman_uses",
						stack.getOrDefault(ModComponents.TALISMAN_USES, 8))
						.withStyle(ChatFormatting.GOLD));
			}
			if (stack.is(ModItems.CRACKED_CATMEN_TALISMAN)) {
				lines.add(Component.translatable("tooltip.lostfragments.cracked_talisman")
						.withStyle(ChatFormatting.DARK_GRAY));
			}
		});

		ExtractItemDecorationsCallback.EVENT.register((graphics, font, stack, x, y) -> {
			if (!stack.has(ModComponents.AMETHYST_INFUSED)) {
				return;
			}

			// Small crystal corners keep the underlying item recognizable.
			boolean failed = stack.has(ModComponents.FRACTURED_INFUSION);
			int light = failed ? 0xFFFF5A5A : 0xFFD884FF;
			int dark = failed ? 0xFFB00020 : 0xFF9B4DCA;
			graphics.fill(x, y, x + 2, y + 5, light);
			graphics.fill(x + 2, y, x + 5, y + 2, dark);
			graphics.fill(x + 14, y + 11, x + 16, y + 16, dark);
			graphics.fill(x + 11, y + 14, x + 14, y + 16, light);
		});
	}
}
