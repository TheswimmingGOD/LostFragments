package com.tsg0d.lostfragments.client.screen;

import com.tsg0d.lostfragments.config.ConfigRange;
import com.tsg0d.lostfragments.config.LostFragmentsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LostFragmentsConfigScreen extends Screen {
	private final Screen parent;
	private LostFragmentsConfig working;
	private final List<NumericOption> options = new ArrayList<>();
	private final Map<String, String> enteredValues = new LinkedHashMap<>();
	private final List<VisibleRow> visibleRows = new ArrayList<>();
	private int page;
	private int pageSize;
	private String error = "";

	public LostFragmentsConfigScreen(Screen parent) {
		super(Component.translatable("config.lostfragments.title"));
		this.parent = parent;
		this.working = LostFragmentsConfig.copy();
		collectOptions(working);
	}

	@Override
	protected void init() {
		visibleRows.clear();
		pageSize = Math.max(4, Math.min(9, (height - 112) / 24));
		page = Math.max(0, Math.min(page, pageCount() - 1));
		int start = page * pageSize;
		int end = Math.min(options.size(), start + pageSize);
		for (int i = start; i < end; i++) {
			NumericOption option = options.get(i);
			int y = 47 + (i - start) * 24;
			EditBox box = new EditBox(font, width / 2 + 35, y, 120, 20, Component.literal(option.label));
			box.setMaxLength(24);
			String value = enteredValues.computeIfAbsent(option.path, ignored -> option.text());
			box.setValue(value);
			box.setResponder(text -> {
				enteredValues.put(option.path, text);
				box.setTextColor(option.valid(text) ? 0xFFE8DDF0 : 0xFFFF5555);
			});
			box.setTextColor(option.valid(value) ? 0xFFE8DDF0 : 0xFFFF5555);
			addRenderableWidget(box);
			visibleRows.add(new VisibleRow(option, y));
		}

		int bottom = height - 28;
		addRenderableWidget(Button.builder(Component.literal("<"), button -> {
			if (page > 0) { page--; rebuildWidgets(); }
		}).bounds(width / 2 - 155, bottom, 35, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("config.lostfragments.reset"), button -> {
			working = LostFragmentsConfig.defaults(); options.clear(); enteredValues.clear(); collectOptions(working);
			error = ""; page = 0; rebuildWidgets();
		}).bounds(width / 2 - 115, bottom, 70, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
				.bounds(width / 2 - 40, bottom, 70, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("config.lostfragments.save"), button -> save())
				.bounds(width / 2 + 35, bottom, 70, 20).build());
		addRenderableWidget(Button.builder(Component.literal(">"), button -> {
			if (page + 1 < pageCount()) { page++; rebuildWidgets(); }
		}).bounds(width / 2 + 110, bottom, 35, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		graphics.centeredText(font, title, width / 2, 15, 0xFFD884FF);
		graphics.centeredText(font, Component.translatable("config.lostfragments.page", page + 1, pageCount()), width / 2, 29, 0xFFAAAAAA);
		for (VisibleRow row : visibleRows) {
			graphics.text(font, Component.literal(row.option.label), width / 2 - 155, row.y + 6, 0xFFFFFFFF);
			graphics.text(font, Component.literal(row.option.rangeText()), width / 2 - 10, row.y + 6, 0xFF888888);
		}
		if (!error.isEmpty()) graphics.centeredText(font, Component.literal(error), width / 2, height - 42, 0xFFFF5555);
	}

	private void save() {
		for (NumericOption option : options) {
			String text = enteredValues.getOrDefault(option.path, option.text());
			if (!option.valid(text)) {
				error = Component.translatable("config.lostfragments.invalid", option.label).getString();
				page = options.indexOf(option) / pageSize; rebuildWidgets(); return;
			}
			option.apply(text);
		}
		LostFragmentsConfig.replaceAndSave(working);
		Minecraft.getInstance().setScreen(parent);
	}

	@Override
	public void onClose() { Minecraft.getInstance().setScreen(parent); }

	private int pageCount() { return Math.max(1, (options.size() + Math.max(1, pageSize) - 1) / Math.max(1, pageSize)); }

	private void collectOptions(LostFragmentsConfig config) {
		for (Field sectionField : LostFragmentsConfig.class.getFields()) {
			try {
				Object section = sectionField.get(config);
				if (section == null) continue;
				for (Field valueField : section.getClass().getFields()) {
					if (valueField.getType().isPrimitive()) {
						options.add(new NumericOption(sectionField.getName() + "." + valueField.getName(),
								humanize(sectionField.getName()) + " / " + humanize(valueField.getName()), section, valueField));
					}
				}
			} catch (IllegalAccessException ignored) { }
		}
	}

	private static String humanize(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1).replaceAll("([a-z])([A-Z])", "$1 $2");
	}

	private record VisibleRow(NumericOption option, int y) { }

	private static final class NumericOption {
		private final String path, label; private final Object owner; private final Field field; private final ConfigRange range;
		private NumericOption(String path, String label, Object owner, Field field) {
			this.path=path; this.label=label; this.owner=owner; this.field=field; this.range=field.getAnnotation(ConfigRange.class);
		}
		private String text() { try { return String.valueOf(field.get(owner)); } catch (IllegalAccessException exception) { return "0"; } }
		private boolean valid(String text) {
			try {
				double value=Double.parseDouble(text);
				return Double.isFinite(value) && range != null && value >= range.min() && value <= range.max()
						&& (!(field.getType()==int.class || field.getType()==long.class) || value==Math.rint(value));
			} catch (NumberFormatException exception) { return false; }
		}
		private void apply(String text) {
			try {
				double value=Double.parseDouble(text);
				if(field.getType()==int.class) field.setInt(owner,(int)value); else if(field.getType()==long.class) field.setLong(owner,(long)value);
				else if(field.getType()==float.class) field.setFloat(owner,(float)value); else field.setDouble(owner,value);
			} catch(IllegalAccessException ignored) { }
		}
		private String rangeText(){return "["+compact(range.min())+".."+compact(range.max())+"]";}
		private static String compact(double value){return value==Math.rint(value)?Long.toString((long)value):Double.toString(value);}
	}
}
