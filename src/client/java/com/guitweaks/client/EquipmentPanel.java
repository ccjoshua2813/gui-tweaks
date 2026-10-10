package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Equipo del jugador en dos perfiles:
 * 1 = piezas de armadura flotando junto al panel de estado;
 * 2 = columnas a los lados de la barra de items.
 * Las piezas de armadura son siluetas del color del material que se apagan al desgastarse.
 */
public final class EquipmentPanel {
	// '#' = píxel relleno, '.' = vacío
	private static final String[] HELMET = {
		"..######..", ".########.", "##########", "##########", "##########", "###....###", "###....###", "###....###"};
	private static final String[] CHEST = {
		"###......###", "############", "############", ".##########.", ".##########.", ".##########.", ".##########.", "..########..", "..########.."};
	private static final String[] ELYTRA = {
		"##..........##", "###........###", "####......####", "#####....#####", "#####....#####", ".####....####.", "..###....###..", "...##....##..."};
	private static final String[] LEGS = {
		"##########", "##########", "##########", "####..####", "###....###", "###....###", "###....###", "###....###"};
	private static final String[] BOOTS = {
		".##......##.", ".##......##.", ".##......##.", "####....####", "####....####", "####....####"};

	private static final int GAP = 4;
	private static final int MARGIN = 4;
	private static final int EMPTY_COLOR = 0x802E3037;
	private static final int WORN_COLOR = 0xFF3C3C40;

	private static ItemStack chestIcon;

	private EquipmentPanel() {}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || cfg.equipmentMode == 0) return;
		if (cfg.equipmentMode == 1) top(g, mc, p, cfg); else sides(g, mc, p, cfg);
	}

	// ---------------------------------------------------------------- perfil 1: piezas flotando
	private static void top(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer p, GuiTweaksConfig cfg) {
		float s = cfg.scalePercent / 100f;
		int w = 14;                       // ancho de la pieza más ancha (élitros)
		int h = StatusPanel.height();

		boolean statusShown = StatusPanel.isVisible(mc);
		int px;
		if (statusShown) {
			int statusW = Math.round(StatusPanel.width(cfg) * s);
			px = cfg.isRight() ? Math.round(g.guiWidth() - MARGIN - statusW - GAP - w * s) : MARGIN + statusW + GAP;
		} else {
			px = cfg.isRight() ? Math.round(g.guiWidth() - MARGIN - w * s) : MARGIN;
		}

		g.pose().pushMatrix();
		g.pose().translate(px, MARGIN);
		g.pose().scale(s, s);

		// Sin recuadro ni manos: solo las piezas de armadura apiladas, centradas en la altura del panel
		int y = (h - 34) / 2;
		y = piece(g, HELMET, p.getItemBySlot(EquipmentSlot.HEAD), 0, y, 1, w) + 1;
		y = piece(g, chestShape(p), p.getItemBySlot(EquipmentSlot.CHEST), 0, y, 1, w) + 1;
		y = piece(g, LEGS, p.getItemBySlot(EquipmentSlot.LEGS), 0, y, 1, w) + 1;
		piece(g, BOOTS, p.getItemBySlot(EquipmentSlot.FEET), 0, y, 1, w);

		g.pose().popMatrix();
	}

	// ---------------------------------------------------------------- perfil 2: columnas
	private static void sides(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer p, GuiTweaksConfig cfg) {
		Font font = mc.font;
		int colW = 30, rowH = 22;
		int arrows = countArrows(p);
		int leftRows = arrows > 0 ? 4 : 3;
		int free = freeSlots(p);

		int hotbarLeft = g.guiWidth() / 2 - 91;
		int hotbarRight = g.guiWidth() / 2 + 91;
		int bottom = g.guiHeight() - 2;

		// Izquierda (de arriba a abajo): mano izquierda, casco, pechera/élitros, flechas
		int lh = leftRows * rowH + 6;
		int lx = hotbarLeft - 28 - colW, ly = bottom - lh;
		Draw.card(g, lx, ly, colW, lh, cfg.accentColor(), cfg.opacityPercent);
		int y = ly + 3;
		handItem(g, font, p.getOffhandItem(), lx + (colW - 16) / 2, y + 3);
		y += rowH;
		piece(g, HELMET, p.getItemBySlot(EquipmentSlot.HEAD), lx, y + 2, 2, colW);
		y += rowH;
		piece(g, chestShape(p), p.getItemBySlot(EquipmentSlot.CHEST), lx, y + 2, 2, colW);
		y += rowH;
		if (arrows > 0) counter(g, font, new ItemStack(Items.ARROW), String.valueOf(arrows), 0xFFFFFFFF, lx, y, colW);

		// Derecha: mano derecha, pantalón, botas, espacios libres
		int rh = 4 * rowH + 6;
		int rx = hotbarRight + 4, ry = bottom - rh;
		Draw.card(g, rx, ry, colW, rh, cfg.accentColor(), cfg.opacityPercent);
		y = ry + 3;
		handItem(g, font, p.getMainHandItem(), rx + (colW - 16) / 2, y + 3);
		y += rowH;
		piece(g, LEGS, p.getItemBySlot(EquipmentSlot.LEGS), rx, y + 2, 2, colW);
		y += rowH;
		piece(g, BOOTS, p.getItemBySlot(EquipmentSlot.FEET), rx, y + 4, 2, colW);
		y += rowH;
		if (chestIcon == null) chestIcon = new ItemStack(Items.CHEST);
		int freeColor = free <= 2 ? 0xFFFF6B6B : free <= 6 ? 0xFFFFD166 : 0xFFB8F5B8;
		counter(g, font, chestIcon, String.valueOf(free), freeColor, rx, y, colW);
	}

	// ---------------------------------------------------------------- piezas
	private static String[] chestShape(LocalPlayer p) {
		return p.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA ? ELYTRA : CHEST;
	}

	/** Dibuja una silueta centrada en {@code areaW}; devuelve la Y de su borde inferior. */
	private static int piece(GuiGraphicsExtractor g, String[] shape, ItemStack stack, int areaX, int y, int sc, int areaW) {
		int w = shape[0].length() * sc;
		int x = areaX + (areaW - w) / 2;
		boolean empty = stack.isEmpty();
		int color = empty ? EMPTY_COLOR : wornColor(stack);
		int outline = empty ? 0x66000000 : 0xFF000000;
		for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) drawShape(g, shape, x + d[0], y + d[1], sc, outline, false);
		drawShape(g, shape, x, y, sc, color, !empty);
		return y + shape.length * sc;
	}

	private static void drawShape(GuiGraphicsExtractor g, String[] shape, int x, int y, int sc, int color, boolean shaded) {
		for (int r = 0; r < shape.length; r++) {
			int c = shaded ? Draw.shade(color, 1.18f - 0.36f * r / Math.max(1, shape.length - 1)) : color;
			String row = shape[r];
			int start = -1;
			for (int i = 0; i <= row.length(); i++) {
				boolean on = i < row.length() && row.charAt(i) == '#';
				if (on && start < 0) start = i;
				if (!on && start >= 0) {
					g.fill(x + start * sc, y + r * sc, x + i * sc, y + (r + 1) * sc, c);
					start = -1;
				}
			}
		}
	}

	/** Color del material, apagado hacia gris a medida que se desgasta. */
	private static int wornColor(ItemStack stack) {
		int base = materialColor(stack);
		float frac = 1f;
		if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
			frac = Math.max(0f, Math.min(1f, (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage()));
		}
		return Draw.lerp(base, WORN_COLOR, (1f - frac) * 0.85f);
	}

	private static int materialColor(ItemStack stack) {
		if (stack.getItem() == Items.ELYTRA) return 0xFF9E8FB5;
		Equippable eq = stack.get(DataComponents.EQUIPPABLE);
		String id = eq != null && eq.assetId().isPresent() ? eq.assetId().get().identifier().getPath() : "";
		if (id.contains("leather")) return 0xFF000000 | (DyedItemColor.getOrDefault(stack, 0xA06540) & 0xFFFFFF);
		if (id.contains("chain")) return 0xFF9AA0A6;
		if (id.contains("iron")) return 0xFFD8D8D8;
		if (id.contains("gold")) return 0xFFFAD64A;
		if (id.contains("diamond")) return 0xFF4AEDD9;
		if (id.contains("netherite")) return 0xFF5B5559;
		if (id.contains("turtle")) return 0xFF6FBF4A;
		if (id.contains("copper")) return 0xFFE0795A;
		return 0xFFBFC5CC;
	}

	// ---------------------------------------------------------------- items y contadores
	private static void handItem(GuiGraphicsExtractor g, Font font, ItemStack stack, int x, int y) {
		Draw.rounded(g, x - 1, y - 1, 18, 18, 0x50000000);
		if (stack.isEmpty()) return;
		g.item(stack, x, y);
		g.itemDecorations(font, stack, x, y);
	}

	/** Icono pequeño con un número al lado, centrado en la fila. */
	private static void counter(GuiGraphicsExtractor g, Font font, ItemStack icon, String text, int color, int x, int y, int colW) {
		g.pose().pushMatrix();
		g.pose().translate(x + 3, y + 3);
		g.pose().scale(0.75f, 0.75f);
		g.item(icon, 0, 0);
		g.pose().popMatrix();
		g.text(font, text, x + 3 + 13, y + 7, color);
	}

	private static boolean isArrow(ItemStack s) {
		Item i = s.getItem();
		return i == Items.ARROW || i == Items.SPECTRAL_ARROW || i == Items.TIPPED_ARROW;
	}

	private static int countArrows(LocalPlayer p) {
		int n = 0;
		for (ItemStack s : p.getInventory().getNonEquipmentItems()) if (isArrow(s)) n += s.getCount();
		if (isArrow(p.getOffhandItem())) n += p.getOffhandItem().getCount();
		return n;
	}

	private static int freeSlots(LocalPlayer p) {
		int free = 0;
		for (ItemStack s : p.getInventory().getNonEquipmentItems()) if (s.isEmpty()) free++;
		return free;
	}
}
