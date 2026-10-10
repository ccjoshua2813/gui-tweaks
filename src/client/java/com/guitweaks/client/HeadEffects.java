package com.guitweaks.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;

/** Efectos animados dibujados sobre la cabeza del jugador. */
public final class HeadEffects {
	/** Flechas clavadas: {x, y} relativos a la cabeza (0-32); cada una sale hacia arriba a la derecha. */
	private static final int[][] ARROW_SPOTS = {{6, 24}, {22, 26}, {12, 14}, {26, 18}, {4, 12}, {18, 9}};

	private HeadEffects() {}

	private static double time() {
		return System.currentTimeMillis();
	}

	/** Temblor horizontal al recibir daño (píxeles). */
	public static int shake(LocalPlayer p) {
		if (p.hurtTime <= 0) return 0;
		return ((int) (time() / 45) % 2 == 0) ? 1 : -1;
	}

	/** Dibuja los efectos encima de la cara. {@code x, y}: esquina de la cabeza; {@code size}: lado. */
	public static void render(GuiGraphicsExtractor g, LocalPlayer p, int x, int y, int size) {
		double t = time();

		// Golpe: destello rojo que se apaga
		if (p.hurtTime > 0 && p.hurtDuration > 0) {
			int a = Math.round(150f * p.hurtTime / p.hurtDuration);
			g.fill(x, y, x + size, y + size, Draw.withAlpha(0xFF2020, a));
		}

		// Veneno / wither / congelación: tintes
		if (p.hasEffect(MobEffects.POISON)) {
			int a = 55 + (int) (25 * Math.sin(t / 300));
			g.fill(x, y, x + size, y + size, Draw.withAlpha(0x7CB518, a));
		}
		if (p.hasEffect(MobEffects.WITHER)) {
			g.fill(x, y, x + size, y + size, Draw.withAlpha(0x101010, 120));
		}
		if (p.isFullyFrozen()) {
			g.fill(x, y, x + size, y + size, Draw.withAlpha(0x9BE0FF, 70));
			frost(g, x, y, size);
		}

		// Poca vida: borde rojo que late
		if (p.getHealth() > 0f && p.getHealth() <= 6f) {
			int a = 90 + (int) (90 * (0.5 + 0.5 * Math.sin(t / 220)));
			int c = Draw.withAlpha(0xFF1010, a);
			g.fill(x, y, x + size, y + 2, c);
			g.fill(x, y + size - 2, x + size, y + size, c);
			g.fill(x, y + 2, x + 2, y + size - 2, c);
			g.fill(x + size - 2, y + 2, x + size, y + size - 2, c);
		}

		if (p.isOnFire()) fire(g, x, y, size, t);
		bubbles(g, p, x, y, size);
		arrows(g, p, x, y);
	}

	/** Llamas: columnas que parpadean desde la base de la cabeza. */
	private static void fire(GuiGraphicsExtractor g, int x, int y, int size, double t) {
		g.fill(x, y, x + size, y + size, Draw.withAlpha(0xFF6A00, 35));
		int cols = size / 4;
		for (int i = 0; i < cols; i++) {
			double flick = 0.5 + 0.5 * Math.sin(t / 90 + i * 1.9) * Math.sin(t / 140 + i * 0.7 + 1.3);
			int hOuter = (int) (size * (0.45 + 0.4 * flick));
			int cx = x + i * 4;
			g.fill(cx, y + size - hOuter, cx + 4, y + size, Draw.withAlpha(0xFF3C00, 190));
			g.fill(cx + 1, y + size - (int) (hOuter * 0.7), cx + 3, y + size, Draw.withAlpha(0xFF9A1A, 210));
			g.fill(cx + 1, y + size - (int) (hOuter * 0.38), cx + 3, y + size, Draw.withAlpha(0xFFE46B, 230));
		}
	}

	/** Escarcha en los bordes. */
	private static void frost(GuiGraphicsExtractor g, int x, int y, int size) {
		int c = Draw.withAlpha(0xE8F8FF, 200);
		for (int i = 0; i < size; i += 5) {
			int d = 2 + (i * 7 % 4);
			g.fill(x + i, y, x + i + 3, y + d, c);
			g.fill(x + i, y + size - d, x + i + 3, y + size, c);
			g.fill(x, y + i, x + d, y + i + 3, c);
			g.fill(x + size - d, y + i, x + size, y + i + 3, c);
		}
	}

	/** Burbujas de aire: una por cada décima parte de aire restante, solo si falta aire. */
	private static void bubbles(GuiGraphicsExtractor g, LocalPlayer p, int x, int y, int size) {
		int max = p.getMaxAirSupply();
		int air = p.getAirSupply();
		if (max <= 0 || air >= max) return;
		int count = Math.max(0, (int) Math.ceil(10f * Math.max(0, air) / max));
		for (int i = 0; i < count; i++) {
			int bx = x + 2 + i * 3, by = y + 2;
			g.fill(bx, by, bx + 3, by + 3, Draw.withAlpha(0x5FB8FF, 220));
			g.fill(bx + 1, by + 1, bx + 2, by + 2, Draw.withAlpha(0xFFFFFF, 230));
		}
	}

	/** Flechas clavadas (hasta 6). */
	private static void arrows(GuiGraphicsExtractor g, LocalPlayer p, int x, int y) {
		int count = Math.min(p.getArrowCount(), ARROW_SPOTS.length);
		for (int i = 0; i < count; i++) {
			int ax = x + ARROW_SPOTS[i][0], ay = y + ARROW_SPOTS[i][1];
			// punta en la cabeza y astil hacia arriba a la derecha, con plumas al final
			for (int s = 0; s < 7; s++) {
				int color = s == 0 ? 0xFF6E6E6E : s >= 5 ? 0xFFEFEFEF : 0xFFC8A060;
				g.fill(ax + s, ay - s, ax + s + 1, ay - s + 1, color);
				if (s >= 4) g.fill(ax + s, ay - s + 1, ax + s + 1, ay - s + 2, 0xFFEFEFEF);
			}
			g.fill(ax - 1, ay + 1, ax + 1, ay + 2, 0xAA000000); // pequeña sombra del impacto
		}
	}
}
