package com.guitweaks.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Ranuras de vista: seis copias de los ajustes de apariencia y las posiciones de los paneles, con nombre
 * propio, para cambiar entre ellas. No incluyen los comandos rápidos (esos van en sus propios grupos).
 */
public class ViewSlotsScreen extends Screen {
	private static final int ROW = 26;

	private final Screen parent;

	public ViewSlotsScreen(Screen parent) {
		super(Component.translatable("guitweaks.slots.title"));
		this.parent = parent;
	}

	private static Component t(String key) {
		return Component.translatable("guitweaks.slots." + key);
	}

	@Override
	protected void init() {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		int x = width / 2 - 170;
		int y = 38;

		for (int i = 0; i < cfg.viewSlots.size(); i++) {
			final int idx = i;
			GuiTweaksConfig.ViewSlot slot = cfg.viewSlots.get(i);
			boolean used = cfg.slotUsed(i);
			int ry = y + i * ROW;

			EditBox name = new EditBox(font, x, ry, 104, 20, t("name"));
			name.setMaxLength(20);
			name.setHint(Component.translatable("guitweaks.slots.slot", i + 1));
			name.setValue(slot.name);
			name.setResponder(v -> slot.name = v);
			addRenderableWidget(name);

			Button load = Button.builder(t("load"), b -> askLoad(idx)).bounds(x + 108, ry, 52, 20).build();
			load.active = used;
			addRenderableWidget(load);

			addRenderableWidget(Button.builder(t("save"), b -> askSave(idx)).bounds(x + 164, ry, 66, 20).build());

			Button dup = Button.builder(t("duplicate"), b -> {
				cfg.duplicateSlot(idx);
				cfg.save();
				rebuildWidgets();
			}).bounds(x + 234, ry, 58, 20).build();
			dup.active = used && cfg.hasEmptySlot();
			addRenderableWidget(dup);

			Button delete = Button.builder(Component.literal("✕"), b -> askDelete(idx)).bounds(x + 296, ry, 20, 20).build();
			delete.active = used;
			addRenderableWidget(delete);
		}

		int by = height - 28;
		Button undo = Button.builder(t("undo_load"), b -> {
			cfg.undoLoad();
			cfg.save();
			rebuildWidgets();
		}).bounds(width / 2 - 154, by, 150, 20).build();
		undo.active = cfg.canUndoLoad();
		addRenderableWidget(undo);
		addRenderableWidget(Button.builder(t("done"), b -> onClose()).bounds(width / 2 + 4, by, 150, 20).build());
	}

	private void confirm(String key, Runnable action) {
		minecraft.setScreenAndShow(new ConfirmScreen(yes -> {
			if (yes) {
				action.run();
				GuiTweaksConfig.get().save();
			}
			minecraft.setScreenAndShow(this);
		}, t(key + ".title"), t(key + ".message")));
	}

	private void askLoad(int i) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		if (cfg.viewDirty()) confirm("load", () -> cfg.loadSlot(i));
		else {
			cfg.loadSlot(i);
			cfg.save();
			rebuildWidgets();
		}
	}

	private void askSave(int i) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		if (cfg.slotUsed(i)) confirm("overwrite", () -> cfg.saveSlot(i));
		else {
			cfg.saveSlot(i);
			cfg.save();
			rebuildWidgets();
		}
	}

	private void askDelete(int i) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		confirm("delete", () -> cfg.clearSlot(i));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		int accent = cfg.accentColor();
		g.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);

		// Marca de la ranura activa
		if (cfg.slotUsed(cfg.activeSlot)) g.text(font, ">", width / 2 - 170 - 9, 38 + cfg.activeSlot * ROW + 6, accent);

		// Estado de la vista actual
		Component status;
		if (cfg.slotUsed(cfg.activeSlot)) {
			String n = cfg.viewSlots.get(cfg.activeSlot).name;
			if (n == null || n.isBlank()) n = Component.translatable("guitweaks.slots.slot", cfg.activeSlot + 1).getString();
			status = Component.translatable(cfg.viewDirty() ? "guitweaks.slots.status.dirty" : "guitweaks.slots.status.clean", n);
		} else {
			status = t(cfg.viewDirty() ? "status.none_dirty" : "status.none");
		}
		g.centeredText(font, status, width / 2, 38 + cfg.viewSlots.size() * ROW + 2, cfg.viewDirty() ? 0xFFFFC060 : 0xFF9A9A9A);
	}

	@Override
	public void onClose() {
		GuiTweaksConfig.get().save();
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void removed() {
		GuiTweaksConfig.get().save();
	}
}
