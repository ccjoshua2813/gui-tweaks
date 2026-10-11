package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/** Una sola línea con FPS, coordenadas y dirección, debajo del panel de estado. */
public final class InfoPanel {
	private static final float TEXT_SCALE = 0.75f;
	private static final int PAD_X = 5;
	private static final int PAD_Y = 3;
	private static final int SEP = 8;
	private static final int MARGIN = 4;
	private static final int SPACING = 3;

	private InfoPanel() {}

	/** Alto (en píxeles de pantalla) que ocupa el panel de info más el espacio de abajo; 0 si no se ve. */
	public static int occupiedHeight(GuiTweaksConfig cfg) {
		if (cfg.infoStyle >= 1) return 0;   // la brújula va arriba al centro, no bajo el panel de estado
		if (!cfg.showInfoPanel || !(cfg.showFps || cfg.showCoords || cfg.showDirection)) return 0;
		return Math.round((9 + PAD_Y * 2 + 3) * cfg.scalePercent / 100f * TEXT_SCALE);
	}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !cfg.showInfoPanel) return;
		if (cfg.infoStyle >= 1) {
			CompassBar.render(g);
			return;
		}

		// pares {etiqueta, valor}
		List<String[]> items = new ArrayList<>();
		if (cfg.showFps) items.add(new String[] {"FPS", String.valueOf(mc.getFps())});
		if (cfg.showCoords) items.add(new String[] {"XYZ",
			(int) Math.floor(mc.player.getX()) + " " + (int) Math.floor(mc.player.getY()) + " " + (int) Math.floor(mc.player.getZ())});
		if (cfg.showDirection) items.add(new String[] {"DIR", dir(mc.player.getDirection())});
		if (items.isEmpty()) return;

		Font font = mc.font;
		int textW = 0;
		for (String[] it : items) textW += font.width(it[0]) + 3 + font.width(it[1]);
		textW += SEP * (items.size() - 1);

		float s = cfg.scalePercent / 100f * TEXT_SCALE;
		int w = textW + PAD_X * 2;
		s = Math.min(s, (g.guiWidth() - 8f) / w);
		int h = 9 + PAD_Y * 2;

		boolean statusShown = StatusPanel.isVisible(mc);
		float fullScale = cfg.scalePercent / 100f;
		int px = cfg.isRight() ? Math.round(g.guiWidth() - MARGIN - w * s) : MARGIN;
		int py = MARGIN + (statusShown ? Math.round((StatusPanel.height() + SPACING) * fullScale) : 0);
		px += PanelLayout.dx("info");
		py += PanelLayout.dy("info");
		PanelLayout.record("info", px, py, Math.round(w * s), Math.round(h * s));

		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(s, s);

		Draw.card(g, 0, 0, w, h, cfg.accentColor(), cfg.opacityPercent);
		int x = PAD_X;
		for (String[] it : items) {
			g.text(font, it[0], x, PAD_Y + 1, cfg.accentColor());
			x += font.width(it[0]) + 3;
			g.text(font, it[1], x, PAD_Y + 1, 0xFFFFFFFF);
			x += font.width(it[1]) + SEP;
		}

		g.pose().popMatrix();
	}

	private static String dir(Direction d) {
		return d.getName().substring(0, 1).toUpperCase();
	}
}
