package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;

/** Tarjeta con la cabeza del jugador y las barras de vida, comida, armadura y XP. */
public final class StatusPanel {
	private static final int HEAD = 32;
	private static final int ICON = 9;
	private static final int BAR_H = 7;
	private static final int PITCH = 10;
	private static final int SEGMENTS = 10;
	private static final int PAD = 5;
	private static final int GAP = 5;
	private static final int NUM_W = 16;
	private static final int MARGIN = 4;

	private static final Identifier HEART = Identifier.withDefaultNamespace("hud/heart/full");
	private static final Identifier FOOD = Identifier.withDefaultNamespace("hud/food_full");
	private static final Identifier ARMOR = Identifier.withDefaultNamespace("hud/armor_full");

	private static final int EMPTY = 0xFF23252B;
	private static final int HEALTH_COLOR = 0xFFE03A3A;
	private static final int HEALTH_POISON = 0xFF9AAA20;
	private static final int HEALTH_WITHER = 0xFF555555;
	private static final int HEALTH_FROZEN = 0xFF7FD4FF;
	private static final int HEALTH_HURT = 0xFFFFFFFF;
	private static final int ABSORPTION = 0xFFFFC800;
	private static final int OVERFLOW = 0xFFFF9090;
	private static final int FOOD_COLOR = 0xFFE08A2E;
	private static final int FOOD_HUNGER = 0xFF86963F;
	private static final int ARMOR_COLOR = 0xFFB8C4D0;

	private StatusPanel() {}

	/** El panel de estado solo aparece en modos con vida (no en creativo/espectador). */
	public static boolean isVisible(Minecraft mc) {
		return mc.player != null && mc.gameMode != null && mc.gameMode.canHurtPlayer();
	}

	public static int width(GuiTweaksConfig cfg) {
		int headW = cfg.showHead ? HEAD + GAP : 0;
		int numW = cfg.showNumbers ? NUM_W : 0;
		return headW + ICON + 3 + cfg.barWidth + numW + PAD * 2;
	}

	public static int height() {
		return HEAD + PAD * 2;
	}

	public static void render(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (!isVisible(mc)) return;

		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Font font = mc.font;
		int accent = cfg.accentColor();
		int barW = cfg.barWidth;

		int headW = cfg.showHead ? HEAD + GAP : 0;
		int w = width(cfg);
		int h = height();

		float s = Draw.fit(cfg.scalePercent / 100f, w, g.guiWidth());
		int px = (cfg.isRight() ? Math.round(g.guiWidth() - MARGIN - w * s) : MARGIN) + PanelLayout.dx("status");
		int py = MARGIN + PanelLayout.dy("status");
		PanelLayout.record("status", px, py, Math.round(w * s), Math.round(h * s));

		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(s, s);

		Draw.card(g, 0, 0, w, h, accent, cfg.opacityPercent);

		int x = PAD, y = PAD;
		if (cfg.showHead) {
			Draw.rounded(g, x - 1, y - 1, HEAD + 2, HEAD + 2, Draw.withAlpha(accent, 255));
			int shake = cfg.headEffects ? HeadEffects.shake(p) : 0;
			if (cfg.head3d) {
				g.fill(x, y, x + HEAD, y + HEAD, 0xFF14161C);
				Head3D.draw(g, p, Math.round(px + (x + shake) * s), Math.round(py + y * s), Math.round(HEAD * s), cfg);
			} else {
				PlayerFaceExtractor.extractRenderState(g, p.getSkin(), x + shake, y, HEAD);
			}
			if (cfg.headEffects) HeadEffects.render(g, p, x, y, HEAD);
			// Insignia del nivel sobre la esquina inferior de la cabeza
			String level = String.valueOf(p.experienceLevel);
			int lw = font.width(level) + 5;
			int lx = x + HEAD - lw + 1, ly = y + HEAD - 9;
			Draw.rounded(g, lx, ly, lw, 10, 0xFF000000);
			Draw.rounded(g, lx + 1, ly + 1, lw - 2, 8, Draw.withAlpha(Draw.shade(accent, 0.45f), 255));
			g.text(font, level, lx + 3, ly + 1, 0xFFFFFFFF);
		}

		int iconX = x + headW;
		int barX = iconX + ICON + 3;
		int numX = barX + barW + 4;

		// Vida: si hay más de 20 puntos se muestra por "capas" de 20 (x2, x3...)
		float hp = p.getHealth();
		float maxHp = p.getMaxHealth();
		int healthColor = HEALTH_COLOR;
		if (p.hasEffect(MobEffects.POISON)) healthColor = HEALTH_POISON;
		if (p.hasEffect(MobEffects.WITHER)) healthColor = HEALTH_WITHER;
		if (p.isFullyFrozen()) healthColor = HEALTH_FROZEN;
		if (p.hurtTime > 0) healthColor = HEALTH_HURT;
		int layers = Math.max(1, (int) Math.ceil(hp / 20f - 1e-4));
		float layerFraction = layers == 1 ? Math.min(hp, 20f) / 20f : (hp - 20f * (layers - 1)) / 20f;
		int hy = y;
		Sprites.blit(g, HEART, iconX, hy - 1, ICON, ICON);
		// Bajo la capa actual se ve, atenuada, la capa anterior
		pillBar(g, barX, hy, barW, BAR_H, layerFraction, healthColor, layers > 1 ? Draw.shade(healthColor, 0.55f) : EMPTY);
		if (layers > 1) {
			small(g, font, "x" + layers, barX + 3, hy + 1, 0xFFFFFFFF);
		} else if (maxHp > 20.5f) {
			small(g, font, "+", barX + barW - 7, hy + 1, ABSORPTION);
		}
		if (p.getAbsorptionAmount() > 0f) {
			int aw = Math.max(BAR_H, Math.round(barW * Math.min(1f, p.getAbsorptionAmount() / 20f)));
			Draw.pill(g, barX, hy + BAR_H + 1, aw, 2, ABSORPTION);
		}
		if (cfg.showNumbers) g.text(font, String.valueOf(Math.round(hp)), numX, hy, 0xFFE6E6E6);

		// Comida
		int fy = y + PITCH;
		int food = p.getFoodData().getFoodLevel();
		Sprites.blit(g, FOOD, iconX, fy - 1, ICON, ICON);
		pillBar(g, barX, fy, barW, BAR_H, food / 20f, p.hasEffect(MobEffects.HUNGER) ? FOOD_HUNGER : FOOD_COLOR, EMPTY);
		if (cfg.showNumbers) g.text(font, String.valueOf(food), numX, fy, 0xFFE6E6E6);

		// Armadura
		int ay = y + PITCH * 2;
		int armor = p.getArmorValue();
		Sprites.blit(g, ARMOR, iconX, ay - 1, ICON, ICON);
		pillBar(g, barX, ay, barW, BAR_H, armor / 20f, ARMOR_COLOR, EMPTY);
		if (cfg.showNumbers) g.text(font, String.valueOf(armor), numX, ay, 0xFFE6E6E6);

		// XP: píldora fina; si no hay cabeza, el nivel va al final
		int xy = y + PITCH * 3 + 1;
		pillBar(g, iconX, xy, ICON + 3 + barW, 4, p.experienceProgress, accent, EMPTY);
		if (!cfg.showHead && cfg.showNumbers) g.text(font, String.valueOf(p.experienceLevel), numX, xy - 2, accent);

		g.pose().popMatrix();
	}

	/**
	 * Barra tipo píldora: contorno oscuro, fondo, relleno con degradado y marcas cada 10%
	 * (cada marca = un icono del juego, 2 puntos).
	 */
	private static void pillBar(GuiGraphicsExtractor g, int x, int y, int w, int h, float fraction, int color, int emptyColor) {
		fraction = Math.max(0f, Math.min(1f, fraction));
		Draw.pill(g, x - 1, y - 1, w + 2, h + 2, 0xFF000000);
		Draw.pill(g, x, y, w, h, Draw.shade(emptyColor, 1.15f), emptyColor);
		if (fraction > 0f) {
			int fw = Math.max(h, Math.round(w * fraction));
			Draw.pill(g, x, y, fw, h, Draw.shade(color, 1.25f), Draw.shade(color, 0.75f));
			// brillo superior
			g.fill(x + h / 2, y + 1, x + Math.max(h / 2 + 1, fw - h / 2), y + 2, Draw.withAlpha(0xFFFFFF, 90));
		}
		if (h >= 6) {
			for (int i = 1; i < SEGMENTS; i++) {
				int tx = x + Math.round(w * i / (float) SEGMENTS);
				g.fill(tx, y + 1, tx + 1, y + h - 1, Draw.withAlpha(0x000000, 90));
			}
		}
	}

	/** Texto pequeño (70%) con sombra, para indicadores dentro de las barras. */
	private static void small(GuiGraphicsExtractor g, Font font, String text, int x, int y, int color) {
		g.pose().pushMatrix();
		g.pose().translate(x, y);
		g.pose().scale(0.7f, 0.7f);
		g.text(font, text, 0, 0, color, true);
		g.pose().popMatrix();
	}
}
