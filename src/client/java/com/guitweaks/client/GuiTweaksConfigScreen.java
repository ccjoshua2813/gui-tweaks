package com.guitweaks.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Pantalla de configuración abierta desde Mod Menu, con pestañas. */
public class GuiTweaksConfigScreen extends Screen {
	private static final int COL_W = 150;
	private static final int ROW = 22;
	private static final int TABS = 4;
	private static int currentTab = 0;

	private final Screen parent;

	public GuiTweaksConfigScreen(Screen parent) {
		super(Component.translatable("guitweaks.config.title"));
		this.parent = parent;
	}

	private static Component t(String key) {
		return Component.translatable("guitweaks.config." + key);
	}

	private static Component onOff(boolean v) {
		return t(v ? "on" : "off");
	}

	@Override
	protected void init() {
		GuiTweaksConfig c = GuiTweaksConfig.get();

		// Pestañas
		int tabW = 88;
		for (int i = 0; i < TABS; i++) {
			final int tab = i;
			Button b = Button.builder(t("tab." + i), x -> { currentTab = tab; rebuildWidgets(); })
				.bounds(width / 2 - (TABS * tabW + (TABS - 1) * 4) / 2 + i * (tabW + 4), 28, tabW, 20).build();
			b.active = tab != currentTab;
			addRenderableWidget(b);
		}

		// Contenido de la pestaña: dos columnas, de izquierda a derecha y hacia abajo
		List<AbstractWidget> items = new ArrayList<>();
		switch (currentTab) {
			case 0 -> {
				items.add(toggle("side", () -> t(c.isRight() ? "side.right" : "side.left"), () -> c.side = c.isRight() ? "LEFT" : "RIGHT"));
				items.add(toggle("head", () -> onOff(c.showHead), () -> c.showHead = !c.showHead));
				items.add(toggle("head3d", () -> onOff(c.head3d), () -> c.head3d = !c.head3d));
				items.add(slider("head3d_zoom", 50, 150, 5, "%", () -> c.head3dZoom, v -> c.head3dZoom = v));
				items.add(slider("head3d_height", -30, 30, 2, "", () -> c.head3dHeight, v -> c.head3dHeight = v));
				items.add(toggle("head_effects", () -> onOff(c.headEffects), () -> c.headEffects = !c.headEffects));
				items.add(toggle("numbers", () -> onOff(c.showNumbers), () -> c.showNumbers = !c.showNumbers));
				items.add(toggle("accent", () -> t("accent." + GuiTweaksConfig.ACCENT_KEYS[c.accent % GuiTweaksConfig.ACCENTS.length]),
					() -> c.accent = (c.accent + 1) % GuiTweaksConfig.ACCENTS.length));
				items.add(toggle("hide_vanilla", () -> onOff(c.hideVanillaBars), () -> c.hideVanillaBars = !c.hideVanillaBars));
				items.add(slider("scale", 50, 150, 5, "%", () -> c.scalePercent, v -> c.scalePercent = v));
				items.add(slider("opacity", 0, 100, 5, "%", () -> c.opacityPercent, v -> c.opacityPercent = v));
				items.add(slider("bar_width", 60, 140, 5, " px", () -> c.barWidth, v -> c.barWidth = v));
			}
			case 1 -> {
				items.add(toggle("info", () -> onOff(c.showInfoPanel), () -> c.showInfoPanel = !c.showInfoPanel));
				items.add(toggle("info_style", () -> t("info_style." + c.infoStyle), () -> c.infoStyle = (c.infoStyle + 1) % 3));
				items.add(toggle("fps", () -> onOff(c.showFps), () -> c.showFps = !c.showFps));
				items.add(toggle("coords", () -> onOff(c.showCoords), () -> c.showCoords = !c.showCoords));
				items.add(toggle("direction", () -> onOff(c.showDirection), () -> c.showDirection = !c.showDirection));
				items.add(toggle("compass_players", () -> onOff(c.compassPlayers), () -> c.compassPlayers = !c.compassPlayers));
				items.add(toggle("compass_heads", () -> onOff(c.compassHeads), () -> c.compassHeads = !c.compassHeads));
				items.add(slider("compass_range", 50, 1000, 50, " m", () -> c.compassRange, v -> c.compassRange = v));
				items.add(slider("compass_offset", 0, 100, 5, " %", () -> c.compassDropPercent, v -> c.compassDropPercent = v));
				items.add(toggle("effects", () -> onOff(c.showEffectsPanel), () -> c.showEffectsPanel = !c.showEffectsPanel));
				items.add(toggle("hotbar", () -> t("hotbar." + c.hotbarStyle), () -> c.hotbarStyle = (c.hotbarStyle + 1) % 2));
				items.add(toggle("equipment", () -> t("equipment." + c.equipmentMode), () -> c.equipmentMode = (c.equipmentMode + 1) % 3));
			}
			case 3 -> {
				items.add(toggle("mob_health", () -> onOff(c.showMobHealth), () -> c.showMobHealth = !c.showMobHealth));
				items.add(slider("mob_distance", 4, 64, 2, " m", () -> c.mobDistance, v -> c.mobDistance = v));
				items.add(toggle("mob_hostile", () -> onOff(c.mobHostile), () -> c.mobHostile = !c.mobHostile));
				items.add(toggle("mob_passive", () -> onOff(c.mobPassive), () -> c.mobPassive = !c.mobPassive));
				items.add(toggle("mob_players", () -> onOff(c.mobPlayers), () -> c.mobPlayers = !c.mobPlayers));
			}
			default -> {
				items.add(Button.builder(t("edit_layout"), b -> minecraft.setScreenAndShow(new HudEditorScreen(this))).size(COL_W, 20).build());
				items.add(toggle("quick", () -> onOff(c.showQuickCommands), () -> c.showQuickCommands = !c.showQuickCommands));
				items.add(toggle("chat_position", () -> t(c.chatPanelCenter ? "chat_position.center" : "chat_position.top"), () -> c.chatPanelCenter = !c.chatPanelCenter));
				items.add(Button.builder(t("edit_commands"), b -> minecraft.setScreenAndShow(new QuickCommandsScreen(this))).size(COL_W, 20).build());
				items.add(toggle("arrow_trail", () -> onOff(c.arrowTrail), () -> c.arrowTrail = !c.arrowTrail));
				items.add(toggle("pause_button", () -> onOff(c.showPauseButton), () -> c.showPauseButton = !c.showPauseButton));
				items.add(toggle("item_popup", () -> onOff(c.showItemPopup), () -> c.showItemPopup = !c.showItemPopup));
				items.add(slider("popup_seconds", 1, 6, 1, " s", () -> c.popupSeconds, v -> c.popupSeconds = v));
			}
		}
		int left = width / 2 - COL_W - 5, right = width / 2 + 5;
		for (int i = 0; i < items.size(); i++) {
			AbstractWidget w = items.get(i);
			w.setX(i % 2 == 0 ? left : right);
			w.setY(60 + (i / 2) * ROW);
			addRenderableWidget(w);
		}

		// El reinicio de todo el mod vive solo en Extras (que tiene sitio libre); la advertencia va en el botón y debajo
		if (currentTab == 2) {
			int warnY = 148;
			addRenderableWidget(Button.builder(t("reset").copy().withStyle(ChatFormatting.RED), b ->
				minecraft.setScreenAndShow(new ConfirmScreen(yes -> {
					if (yes) {
						c.copyFrom(new GuiTweaksConfig());
						c.save();
					}
					minecraft.setScreenAndShow(this);
				}, t("reset.title").copy().withStyle(ChatFormatting.RED), t("reset.message"))))
				.bounds(width / 2 - COL_W - 5, warnY + 20, COL_W * 2 + 10, 20).build());
		}

		int bw = 112, bw2 = 170, gap = 4;
		int bx = width / 2 - (bw + bw2 + gap) / 2;
		addRenderableWidget(Button.builder(t("done"), b -> onClose()).bounds(bx, height - 28, bw, 20).build());
		addRenderableWidget(Button.builder(t("reset_tab"), b -> {
			c.resetTab(currentTab);
			rebuildWidgets();
		}).bounds(bx + bw + gap, height - 28, bw2, 20).build());
	}

	private static Button toggle(String key, Supplier<Component> value, Runnable action) {
		return Button.builder(label(key, value.get()), b -> {
			action.run();
			b.setMessage(label(key, value.get()));
		}).size(COL_W, 20).build();
	}

	private static ConfigSlider slider(String key, int min, int max, int step, String unit, IntSupplier getter, IntConsumer setter) {
		return new ConfigSlider(COL_W, key, min, max, step, unit, getter, setter);
	}

	private static Component label(String key, Component value) {
		return t(key).copy().append(": ").append(value);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		g.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
		if (currentTab == 2) {      // explicación bajo el botón de reinicio total
			g.centeredText(font, t("reset.warn1"), width / 2, 192, 0xFFB8B8B8);
			g.centeredText(font, t("reset.warn2"), width / 2, 202, 0xFFB8B8B8);
		}
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

	/** Slider entero con etiqueta. */
	private static final class ConfigSlider extends AbstractSliderButton {
		private final String key, unit;
		private final int min, max, step;
		private final IntConsumer setter;

		ConfigSlider(int w, String key, int min, int max, int step, String unit, IntSupplier getter, IntConsumer setter) {
			super(0, 0, w, 20, Component.empty(), (getter.getAsInt() - min) / (double) (max - min));
			this.key = key; this.unit = unit; this.min = min; this.max = max; this.step = step; this.setter = setter;
			updateMessage();
		}

		private int current() {
			int v = min + (int) Math.round(value * (max - min));
			return Math.max(min, Math.min(max, Math.round(v / (float) step) * step));
		}

		@Override
		protected void updateMessage() {
			setMessage(t(key).copy().append(": " + current() + unit));
		}

		@Override
		protected void applyValue() {
			setter.accept(current());
		}
	}
}
