package com.guitweaks.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Utilidades de dibujo para el estilo del mod. */
public final class Draw {
	private Draw() {}

	public static int withAlpha(int argb, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (argb & 0xFFFFFF);
	}

	public static int shade(int argb, float f) {
		int r = Math.min(255, Math.round(((argb >> 16) & 0xFF) * f));
		int g = Math.min(255, Math.round(((argb >> 8) & 0xFF) * f));
		int b = Math.min(255, Math.round((argb & 0xFF) * f));
		return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
	}

	/** Rectángulo con esquinas recortadas (1px). */
	public static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		g.fill(x + 1, y, x + w - 1, y + h, color);
		g.fill(x, y + 1, x + w, y + h - 1, color);
	}

	/** Tarjeta: borde fino, fondo translúcido y línea de acento arriba. */
	public static void card(GuiGraphicsExtractor g, int x, int y, int w, int h, int accent, int opacityPercent) {
		if (opacityPercent <= 0) return;   // totalmente transparente: los iconos "flotan"
		int bgAlpha = Math.round(255 * opacityPercent / 100f);
		rounded(g, x, y, w, h, withAlpha(0x000000, Math.min(255, bgAlpha + 40)));
		rounded(g, x + 1, y + 1, w - 2, h - 2, withAlpha(0x14161C, bgAlpha));
		g.fill(x + 3, y + 1, x + w - 3, y + 2, withAlpha(accent, 200));
	}

	/** Interpola dos colores ARGB. */
	public static int lerp(int a, int b, float t) {
		int aa = Math.round(((a >>> 24) & 0xFF) * (1 - t) + ((b >>> 24) & 0xFF) * t);
		int r = Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
		int g = Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
		int bl = Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
		return (aa << 24) | (r << 16) | (g << 8) | bl;
	}

	/** Píldora (extremos semicirculares) con degradado vertical de {@code top} a {@code bottom}. */
	public static void pill(GuiGraphicsExtractor g, int x, int y, int w, int h, int top, int bottom) {
		float r = h / 2f;
		for (int row = 0; row < h; row++) {
			float dy = row + 0.5f - r;
			int inset = Math.round(r - (float) Math.sqrt(Math.max(0f, r * r - dy * dy)));
			int x1 = x + inset, x2 = x + w - inset;
			if (x2 > x1) g.fill(x1, y + row, x2, y + row + 1, lerp(top, bottom, h <= 1 ? 0f : row / (float) (h - 1)));
		}
	}

	public static void pill(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
		pill(g, x, y, w, h, color, color);
	}
}
