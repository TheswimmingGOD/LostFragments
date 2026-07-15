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
	private enum View { CATEGORIES, SECTIONS, SETTINGS }

	private final Screen parent;
	private LostFragmentsConfig working;
	private final List<ConfigGroup> groups = new ArrayList<>();
	private final Map<String, String> enteredValues = new LinkedHashMap<>();
	private final List<VisibleRow> visibleRows = new ArrayList<>();
	private View view = View.CATEGORIES;
	private ConfigGroup selectedGroup;
	private ConfigSection selectedSection;
	private int page;
	private int pageSize = 1;
	private String error = "";

	public LostFragmentsConfigScreen(Screen parent) {
		super(Component.translatable("config.lostfragments.title"));
		this.parent = parent;
		working = LostFragmentsConfig.copy();
		buildStructure();
	}

	@Override
	protected void init() {
		visibleRows.clear();
		switch (view) {
			case CATEGORIES -> initCategories();
			case SECTIONS -> initSections();
			case SETTINGS -> initSettings();
		}
	}

	private void initCategories() {
		int panelWidth = Math.min(430, width - 30);
		int buttonWidth = (panelWidth - 10) / 2;
		int left = (width - panelWidth) / 2;
		int top = 52;
		for (int i = 0; i < groups.size(); i++) {
			ConfigGroup group = groups.get(i);
			int x = left + (i % 2) * (buttonWidth + 10);
			int y = top + (i / 2) * 30;
			addRenderableWidget(Button.builder(Component.literal(group.label), button -> openGroup(group))
					.bounds(x, y, buttonWidth, 24).build());
		}
		int bottom = height - 28;
		addRenderableWidget(Button.builder(Component.translatable("config.lostfragments.reset_all"), button -> resetAll())
				.bounds(width / 2 - 150, bottom, 95, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
				.bounds(width / 2 - 50, bottom, 95, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("config.lostfragments.save"), button -> save())
				.bounds(width / 2 + 50, bottom, 95, 20).build());
	}

	private void initSections() {
		if (selectedGroup == null) { view = View.CATEGORIES; rebuildWidgets(); return; }
		int panelWidth = Math.min(360, width - 30);
		int left = (width - panelWidth) / 2;
		int top = 52;
		for (int i = 0; i < selectedGroup.sections.size(); i++) {
			ConfigSection section = selectedGroup.sections.get(i);
			addRenderableWidget(Button.builder(Component.literal(section.label), button -> openSection(section))
					.bounds(left, top + i * 30, panelWidth, 24).build());
		}
		addBottomNavigation(false);
	}

	private void initSettings() {
		if (selectedSection == null) { view = View.SECTIONS; rebuildWidgets(); return; }
		pageSize = Math.max(3, Math.min(8, (height - 126) / 34));
		page = Math.max(0, Math.min(page, pageCount() - 1));
		int panelWidth = Math.min(520, width - 24);
		int left = (width - panelWidth) / 2;
		int inputWidth = Math.min(160, Math.max(95, panelWidth / 3));
		int inputX = left + panelWidth - inputWidth;
		int start = page * pageSize;
		int end = Math.min(selectedSection.options.size(), start + pageSize);
		for (int i = start; i < end; i++) {
			NumericOption option = selectedSection.options.get(i);
			int y = 53 + (i - start) * 34;
			EditBox box = new EditBox(font, inputX, y, inputWidth, 22, Component.literal(option.label));
			box.setMaxLength(24);
			String value = enteredValues.computeIfAbsent(option.path, ignored -> option.text());
			box.setValue(value);
			box.setResponder(text -> {
				enteredValues.put(option.path, text);
				box.setTextColor(option.valid(text) ? 0xFFE8DDF0 : 0xFFFF5555);
			});
			box.setTextColor(option.valid(value) ? 0xFFE8DDF0 : 0xFFFF5555);
			addRenderableWidget(box);
			visibleRows.add(new VisibleRow(option, left, y));
		}

		int bottom = height - 28;
		addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
			view = View.SECTIONS; page = 0; error = ""; rebuildWidgets();
		}).bounds(width / 2 - 190, bottom, 75, 20).build());
		addRenderableWidget(Button.builder(Component.literal("<"), button -> {
			if (page > 0) { page--; rebuildWidgets(); }
		}).bounds(width / 2 - 110, bottom, 35, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("config.lostfragments.defaults"), button -> resetSection())
				.bounds(width / 2 - 70, bottom, 95, 20).build());
		addRenderableWidget(Button.builder(Component.literal(">"), button -> {
			if (page + 1 < pageCount()) { page++; rebuildWidgets(); }
		}).bounds(width / 2 + 30, bottom, 35, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("config.lostfragments.save"), button -> save())
				.bounds(width / 2 + 70, bottom, 115, 20).build());
	}

	private void addBottomNavigation(boolean unused) {
		int bottom = height - 28;
		addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
			view = View.CATEGORIES; selectedGroup = null; error = ""; rebuildWidgets();
		}).bounds(width / 2 - 150, bottom, 95, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
				.bounds(width / 2 - 50, bottom, 95, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("config.lostfragments.save"), button -> save())
				.bounds(width / 2 + 50, bottom, 95, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		graphics.centeredText(font, title, width / 2, 13, 0xFFD884FF);
		graphics.centeredText(font, Component.literal(breadcrumb()), width / 2, 29, 0xFFCCCCCC);
		if (view == View.SETTINGS) {
			graphics.centeredText(font, Component.translatable("config.lostfragments.page", page + 1, pageCount()),
					width / 2, 41, 0xFF999999);
			for (VisibleRow row : visibleRows) {
				graphics.text(font, Component.literal(row.option.label), row.x, row.y + 1, 0xFFFFFFFF);
				graphics.text(font, Component.literal(row.option.rangeText()), row.x, row.y + 13, 0xFFAAAAAA);
			}
		}
		if (!error.isEmpty()) graphics.centeredText(font, Component.literal(error), width / 2, height - 42, 0xFFFF5555);
	}

	private String breadcrumb() {
		return switch (view) {
			case CATEGORIES -> Component.translatable("config.lostfragments.choose_category").getString();
			case SECTIONS -> selectedGroup == null ? "" : selectedGroup.label;
			case SETTINGS -> selectedGroup.label + "  >  " + selectedSection.label;
		};
	}

	private void openGroup(ConfigGroup group) {
		selectedGroup = group; selectedSection = null; view = View.SECTIONS; page = 0; error = ""; rebuildWidgets();
	}

	private void openSection(ConfigSection section) {
		selectedSection = section; view = View.SETTINGS; page = 0; error = ""; rebuildWidgets();
	}

	private void resetAll() {
		working = LostFragmentsConfig.defaults(); enteredValues.clear(); groups.clear(); buildStructure(); error = ""; rebuildWidgets();
	}

	private void resetSection() {
		LostFragmentsConfig defaults = LostFragmentsConfig.defaults();
		try {
			Field sectionField = LostFragmentsConfig.class.getField(selectedSection.fieldName);
			Object defaultsSection = sectionField.get(defaults);
			Object workingSection = sectionField.get(working);
			for (NumericOption option : selectedSection.options) {
				Field valueField = workingSection.getClass().getField(option.field.getName());
				valueField.set(workingSection, valueField.get(defaultsSection));
				enteredValues.remove(option.path);
			}
		} catch (ReflectiveOperationException ignored) { }
		error = ""; page = 0; rebuildWidgets();
	}

	private void save() {
		for (ConfigGroup group : groups) for (ConfigSection section : group.sections) for (NumericOption option : section.options) {
			String value = enteredValues.getOrDefault(option.path, option.text());
			if (!option.valid(value)) {
				selectedGroup = group; selectedSection = section; view = View.SETTINGS;
				page = section.options.indexOf(option) / pageSize;
				error = Component.translatable("config.lostfragments.invalid", option.label).getString();
				rebuildWidgets(); return;
			}
			option.apply(value);
		}
		LostFragmentsConfig.replaceAndSave(working);
		Minecraft.getInstance().setScreen(parent);
	}

	@Override
	public void onClose() { Minecraft.getInstance().setScreen(parent); }

	private int pageCount() {
		return selectedSection == null ? 1 : Math.max(1,
				(selectedSection.options.size() + pageSize - 1) / pageSize);
	}

	private void buildStructure() {
		Map<String, ConfigSection> sections = new LinkedHashMap<>();
		for (Field sectionField : LostFragmentsConfig.class.getFields()) {
			try {
				Object owner = sectionField.get(working);
				if (owner == null) continue;
				ConfigSection section = new ConfigSection(sectionField.getName(), sectionLabel(sectionField.getName()));
				for (Field valueField : owner.getClass().getFields()) {
					if (valueField.getType().isPrimitive()) section.options.add(new NumericOption(
							sectionField.getName() + "." + valueField.getName(), humanize(valueField.getName()), owner, valueField));
				}
				sections.put(sectionField.getName(), section);
			} catch (IllegalAccessException ignored) { }
		}
		groups.add(group("Infusion", sections, "infusion"));
		groups.add(group("Tools", sections, "mining"));
		groups.add(group("Weapons", sections, "sword", "bow", "trident", "spear", "mace"));
		groups.add(group("Armor & Companions", sections, "armor", "animalArmor"));
		groups.add(group("Utility Items", sections, "fishingRod", "bundle"));
		groups.add(group("Talisman & Storage", sections, "talisman", "resonantChest"));
	}

	private static ConfigGroup group(String label, Map<String, ConfigSection> sections, String... names) {
		ConfigGroup group = new ConfigGroup(label);
		for (String name : names) if (sections.containsKey(name)) group.sections.add(sections.get(name));
		return group;
	}

	private static String sectionLabel(String name) {
		return switch (name) {
			case "animalArmor" -> "Animal Armor";
			case "fishingRod" -> "Fishing Rod";
			case "resonantChest" -> "Resonant Chest";
			default -> humanize(name);
		};
	}

	private static String humanize(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1).replaceAll("([a-z])([A-Z])", "$1 $2");
	}

	private record VisibleRow(NumericOption option, int x, int y) { }
	private static final class ConfigGroup {
		private final String label; private final List<ConfigSection> sections = new ArrayList<>();
		private ConfigGroup(String label) { this.label = label; }
	}
	private static final class ConfigSection {
		private final String fieldName, label; private final List<NumericOption> options = new ArrayList<>();
		private ConfigSection(String fieldName, String label) { this.fieldName = fieldName; this.label = label; }
	}
	private static final class NumericOption {
		private final String path, label; private final Object owner; private final Field field; private final ConfigRange range;
		private NumericOption(String path, String label, Object owner, Field field) {
			this.path=path; this.label=label; this.owner=owner; this.field=field; this.range=field.getAnnotation(ConfigRange.class);
		}
		private String text(){try{return String.valueOf(field.get(owner));}catch(IllegalAccessException exception){return "0";}}
		private boolean valid(String text){try{double value=Double.parseDouble(text);return Double.isFinite(value)&&range!=null
				&&value>=range.min()&&value<=range.max()&&(!(field.getType()==int.class||field.getType()==long.class)||value==Math.rint(value));}
			catch(NumberFormatException exception){return false;}}
		private void apply(String text){try{double value=Double.parseDouble(text);if(field.getType()==int.class)field.setInt(owner,(int)value);
			else if(field.getType()==long.class)field.setLong(owner,(long)value);else if(field.getType()==float.class)field.setFloat(owner,(float)value);
			else field.setDouble(owner,value);}catch(IllegalAccessException ignored){}}
		private String rangeText(){return "Allowed: "+compact(range.min())+" - "+compact(range.max());}
		private static String compact(double value){return value==Math.rint(value)?Long.toString((long)value):Double.toString(value);}
	}
}
