package com.guitweaks.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

/** Botón de configuración del mod en la esquina superior derecha de la pantalla de pausa (Esc). */
public final class PauseConfigButton {
	private static final int W = 74;
	private static final int H = 15;
	private static final int MARGIN = 4;
	private static final String LABEL = "GUI Tweaks";

	private PauseConfigButton() {}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			GuiTweaksConfig cfg = GuiTweaksConfig.get();
			if (!(screen instanceof PauseScreen) || !cfg.showPauseButton) return;

			int x = scaledWidth - MARGIN - W, y = MARGIN;
			Button button = Button.builder(Component.literal(LABEL), b -> client.setScreenAndShow(new GuiTweaksConfigScreen(screen)))
				.bounds(x, y, W, H)
				.tooltip(Tooltip.create(Component.translatable("guitweaks.config.title")))
				.build();
			button.setAlpha(0f);                       // el aspecto lo dibuja el mod
			Screens.getWidgets(screen).add(button);

			Font font = client.font;
			ScreenEvents.afterExtract(screen).register((s, g, mouseX, mouseY, tickDelta) -> draw(g, font, button, cfg));
		});
	}

	private static void draw(GuiGraphicsExtractor g, Font font, Button b, GuiTweaksConfig cfg) {
		int accent = cfg.accentColor();
		boolean hover = b.isHoveredOrFocused();
		int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
		// En la pausa el fondo del juego ya está oscurecido: se usa una opacidad mínima para que se vea
		Draw.buttonBox(g, x, y, w, h, hover, accent, Math.max(cfg.opacityPercent, 60));

		// Icono de controles deslizantes
		int ix = x + 5, iy = y + (h - 7) / 2;
		int c = hover ? 0xFFFFFFFF : Draw.withAlpha(accent, 255);
		g.fill(ix, iy, ix + 9, iy + 1, c);
		g.fill(ix, iy + 3, ix + 9, iy + 4, c);
		g.fill(ix, iy + 6, ix + 9, iy + 7, c);
		g.fill(ix + 5, iy - 1, ix + 7, iy + 2, c);
		g.fill(ix + 1, iy + 2, ix + 3, iy + 5, c);
		g.fill(ix + 6, iy + 5, ix + 8, iy + 8, c);

		float scale = 0.8f;
		g.pose().pushMatrix();
		g.pose().translate(x + 18, y + (h - 9 * scale) / 2f + 0.5f);
		g.pose().scale(scale, scale);
		g.text(font, LABEL, 0, 0, hover ? 0xFFFFFFFF : 0xFFE6E6E6, true);
		g.pose().popMatrix();
	}
}
