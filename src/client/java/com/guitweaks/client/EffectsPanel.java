package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Efectos de poción activos, en píldoras pequeñas con icono y tiempo restante. */
public final class EffectsPanel {
	private static final float TEXT_SCALE = 0.75f;
	private static final int ICON = 9;
	private static final int PAD = 4;
	private static final int CHIP_H = 13;
	private static final int GAP = 3;
	private static final int MARGIN = 4;
	private static final int SPACING = 3;
	private static final String[] ROMAN = {"", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

	private EffectsPanel() {}

	private record Chip(Identifier icon, String text, boolean good, boolean blink, int width) {}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !cfg.showEffectsPanel || mc.player.getActiveEffects().isEmpty()) return;

		Font font = mc.font;
		List<MobEffectInstance> effects = new ArrayList<>(mc.player.getActiveEffects());
		effects.sort(Comparator.comparing((MobEffectInstance e) -> e.getEffect().value().isBeneficial()).reversed()
			.thenComparingInt(e -> e.isInfiniteDuration() ? Integer.MAX_VALUE : e.getDuration()));

		List<Chip> chips = new ArrayList<>();
		for (MobEffectInstance e : effects) {
			var key = e.getEffect().unwrapKey();
			if (key.isEmpty()) continue;
			Identifier id = key.get().identifier();
			Identifier icon = Identifier.fromNamespaceAndPath(id.getNamespace(), "mob_effect/" + id.getPath());
			String text = e.isInfiniteDuration() ? "\u221E" : time(e.getDuration());
			if (e.getAmplifier() > 0) text += " " + (e.getAmplifier() < ROMAN.length ? ROMAN[e.getAmplifier()] : String.valueOf(e.getAmplifier() + 1));
			boolean blink = !e.isInfiniteDuration() && e.getDuration() < 200;
			int w = 3 + ICON + 2 + Math.round(font.width(text) * TEXT_SCALE) + 4;
			chips.add(new Chip(icon, text, e.getEffect().value().isBeneficial(), blink, w));
		}
		if (chips.isEmpty()) return;

		// Reparto en filas, con el mismo ancho máximo que el panel de estado
		int maxRow = Math.max(StatusPanel.width(cfg), 110) - PAD * 2;
		List<List<Chip>> rows = new ArrayList<>();
		List<Chip> row = new ArrayList<>();
		int rowW = 0, widest = 0;
		for (Chip c : chips) {
			if (!row.isEmpty() && rowW + GAP + c.width > maxRow) {
				rows.add(row); widest = Math.max(widest, rowW);
				row = new ArrayList<>(); rowW = 0;
			}
			rowW += (row.isEmpty() ? 0 : GAP) + c.width;
			row.add(c);
		}
		rows.add(row); widest = Math.max(widest, rowW);

		float s = cfg.scalePercent / 100f;
		int w = widest + PAD * 2;
		s = Draw.fit(s, w, g.guiWidth());
		int h = rows.size() * CHIP_H + (rows.size() - 1) * GAP + PAD * 2;

		int px = cfg.isRight() ? Math.round(g.guiWidth() - MARGIN - w * s) : MARGIN;
		int py = MARGIN;
		if (StatusPanel.isVisible(mc)) py += Math.round((StatusPanel.height() + SPACING) * s);
		py += InfoPanel.occupiedHeight(cfg);

		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(s, s);
		Draw.card(g, 0, 0, w, h, cfg.accentColor(), cfg.opacityPercent);

		double t = System.currentTimeMillis();
		int y = PAD;
		for (List<Chip> r : rows) {
			int x = PAD;
			for (Chip c : r) {
				int border = c.good ? 0xFF4CAF50 : 0xFFE53935;
				int a = c.blink ? 120 + (int) (135 * (0.5 + 0.5 * Math.sin(t / 150))) : 255;
				Draw.pill(g, x, y, c.width, CHIP_H, Draw.withAlpha(border, a));
				Draw.pill(g, x + 1, y + 1, c.width - 2, CHIP_H - 2, 0xFF1B1D23);
				g.blitSprite(RenderPipelines.GUI_TEXTURED, c.icon, x + 3, y + (CHIP_H - ICON) / 2, ICON, ICON);
				g.pose().pushMatrix();
				g.pose().translate(x + 3 + ICON + 2, y + 4);
				g.pose().scale(TEXT_SCALE, TEXT_SCALE);
				g.text(font, c.text, 0, 0, Draw.withAlpha(0xFFFFFF, a));
				g.pose().popMatrix();
				x += c.width + GAP;
			}
			y += CHIP_H + GAP;
		}
		g.pose().popMatrix();
	}

	private static String time(int ticks) {
		int sec = Math.max(0, ticks / 20);
		return sec >= 60 ? (sec / 60) + ":" + String.format("%02d", sec % 60) : sec + "s";
	}
}
