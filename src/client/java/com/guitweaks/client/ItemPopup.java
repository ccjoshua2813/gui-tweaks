package com.guitweaks.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

/** Al cambiar de item: nombre, encantamientos y una pequeña barra de durabilidad que se desvanecen. */
public final class ItemPopup {
	private static final int BAR_W = 18;      // no más ancho que una casilla de la hotbar
	private static final int BAR_H = 4;
	private static final int FADE_MS = 600;
	private static final float TEXT_SCALE = 0.85f;
	private static final int LINE_H = 9;
	private static final int MAX_ENCHANTS = 6;

	private static ItemStack lastStack = ItemStack.EMPTY;
	private static int lastSlot = -1;
	private static long changedAt = 0;

	private ItemPopup() {}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !cfg.showItemPopup) return;

		ItemStack stack = mc.player.getInventory().getSelectedItem();
		int slot = mc.player.getInventory().getSelectedSlot();
		long now = System.currentTimeMillis();
		if (slot != lastSlot || !ItemStack.isSameItem(stack, lastStack)) {
			lastSlot = slot;
			lastStack = stack.copy();
			changedAt = now;
		}
		if (stack.isEmpty()) return;

		long total = cfg.popupSeconds * 1000L;
		long elapsed = now - changedAt;
		if (elapsed >= total) return;
		float fade = elapsed > total - FADE_MS ? (total - elapsed) / (float) FADE_MS : 1f;
		int alpha = Math.round(255 * fade);
		if (alpha < 8) return;

		Font font = mc.font;
		int accent = cfg.accentColor();

		// Líneas de texto, de arriba hacia abajo
		List<Component> lines = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
		if (ench == null || ench.isEmpty()) ench = stack.get(DataComponents.STORED_ENCHANTMENTS);
		if (ench != null && !ench.isEmpty()) {
			int shown = 0, total2 = ench.entrySet().size();
			for (var e : ench.entrySet()) {
				if (shown >= MAX_ENCHANTS) break;
				Holder<Enchantment> h = e.getKey();
				String text = Enchantment.getFullname(h, e.getIntValue()).getString();
				boolean curse = h.getRegisteredName().contains("curse");
				lines.add(Component.literal(text));
				colors.add(curse ? 0xFF7B7B : 0xEAF6FF);
				shown++;
			}
			if (total2 > MAX_ENCHANTS) {
				lines.add(Component.literal("+" + (total2 - MAX_ENCHANTS)));
				colors.add(0xD0D0D0);
			}
		}

		boolean durable = stack.isDamageableItem();
		int max = durable ? stack.getMaxDamage() : 0;
		int left = durable ? max - stack.getDamageValue() : 0;
		float frac = durable && max > 0 ? Math.max(0f, Math.min(1f, left / (float) max)) : 0f;
		int barColor = frac > 0.5f ? Draw.lerp(0xFFF2C94C, 0xFF5CD65C, (frac - 0.5f) * 2f) : Draw.lerp(0xFFE53935, 0xFFF2C94C, frac * 2f);
		if (durable) {
			lines.add(Component.literal(left + "/" + max));
			colors.add(barColor & 0xFFFFFF);
		}

		Component name = Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());

		// Todo centrado en la pantalla, un poco por encima de la hotbar:
		// arriba el nombre y los encantamientos, debajo la durabilidad y, al final, la barrita
		int centerX = g.guiWidth() / 2;
		int y = g.guiHeight() - 22 - 12;

		if (durable) {
			int bx = centerX - BAR_W / 2;
			Draw.pill(g, bx - 1, y - 1, BAR_W + 2, BAR_H + 2, Draw.withAlpha(0x000000, alpha));
			Draw.pill(g, bx, y, BAR_W, BAR_H, Draw.withAlpha(0x23252B, alpha));
			int fw = frac <= 0f ? 0 : Math.max(BAR_H, Math.round(BAR_W * frac));
			if (fw > 0) Draw.pill(g, bx, y, fw, BAR_H, Draw.withAlpha(Draw.shade(barColor, 1.2f), alpha), Draw.withAlpha(Draw.shade(barColor, 0.75f), alpha));
		}

		// Texto apilado hacia arriba desde la barra
		for (int i = lines.size() - 1; i >= 0; i--) {
			y -= LINE_H;
			drawSmall(g, font, lines.get(i), centerX, y, (alpha << 24) | colors.get(i));
		}
		y -= LINE_H + 1;
		drawSmall(g, font, name, centerX, y, (alpha << 24) | 0xFFFFFF);
	}

	/** Texto centrado en {@code cx}, a escala reducida, sin salirse de la pantalla. */
	private static void drawSmall(GuiGraphicsExtractor g, Font font, Component text, int cx, int y, int color) {
		float w = font.width(text) * TEXT_SCALE;
		float x = Math.max(2, Math.min(g.guiWidth() - w - 2, cx - w / 2f));
		g.pose().pushMatrix();
		g.pose().translate(x, y);
		g.pose().scale(TEXT_SCALE, TEXT_SCALE);
		g.text(font, text, 0, 0, color);
		g.pose().popMatrix();
	}
}
