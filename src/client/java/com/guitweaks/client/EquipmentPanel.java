package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Equipo del jugador con los iconos nativos de los items, en dos perfiles:
 * 1 = armadura apilada junto al panel de estado (iconos reducidos);
 * 2 = columnas junto a la barra de items, con iconos del tamaño de los de la hotbar.
 * Las piezas que no se llevan muestran la ranura vacía del inventario; el desgaste se ve
 * con la barra de durabilidad del propio juego.
 */
public final class EquipmentPanel {
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private static final Identifier[] EMPTY_SLOT = {
		Identifier.withDefaultNamespace("container/slot/helmet"),
		Identifier.withDefaultNamespace("container/slot/chestplate"),
		Identifier.withDefaultNamespace("container/slot/leggings"),
		Identifier.withDefaultNamespace("container/slot/boots")};

	private static final int ICON = 16;      // tamaño de un icono de la hotbar
	private static final int ROW = 18;       // alto de cada fila en las columnas
	private static final int SMALL = 10;     // tamaño de los iconos del perfil superior
	private static final int GAP = 4;
	private static final int MARGIN = 4;

	private static ItemStack chestIcon;

	private EquipmentPanel() {}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || cfg.equipmentMode == 0) return;
		if (cfg.equipmentMode == 1) top(g, mc, p, cfg); else sides(g, mc, p, cfg);
	}

	// ---------------------------------------------------------------- perfil 1: armadura junto al panel
	private static void top(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer p, GuiTweaksConfig cfg) {
		float s = cfg.scalePercent / 100f;
		int w = SMALL;
		int h = StatusPanel.height();

		int px;
		if (StatusPanel.isVisible(mc)) {
			int statusW = Math.round(StatusPanel.width(cfg) * s);
			px = cfg.isRight() ? Math.round(g.guiWidth() - MARGIN - statusW - GAP - w * s) : MARGIN + statusW + GAP;
		} else {
			px = cfg.isRight() ? Math.round(g.guiWidth() - MARGIN - w * s) : MARGIN;
		}

		g.pose().pushMatrix();
		g.pose().translate(px, MARGIN);
		g.pose().scale(s, s);
		int y = (h - SMALL * ARMOR.length) / 2;
		for (int i = 0; i < ARMOR.length; i++) {
			icon(g, mc.font, p.getItemBySlot(ARMOR[i]), EMPTY_SLOT[i], 0, y + i * SMALL, SMALL);
		}
		g.pose().popMatrix();
	}

	// ---------------------------------------------------------------- perfil 2: columnas junto a la hotbar
	private static void sides(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer p, GuiTweaksConfig cfg) {
		Font font = mc.font;
		int tint = Draw.withAlpha(cfg.accentColor(), Math.round(30 * Math.min(100, cfg.opacityPercent) / 70f));   // fondo muy tenue; con opacidad 0 desaparece
		int hotbarLeft = g.guiWidth() / 2 - 91;
		int hotbarRight = g.guiWidth() / 2 + 91;
		int arrows = countArrows(p);

		// Izquierda: sin ranura de mano izquierda (ya la dibuja el juego). Casco, pechera y flechas
		// quedan apilados justo encima de esa ranura vanilla.
		int rows = arrows > 0 ? 3 : 2;
		int lx = hotbarLeft - 26;
		int ly = g.guiHeight() - 24 - rows * ROW;
		if (cfg.opacityPercent > 0) Draw.rounded(g, lx - 2, ly, ICON + 4, rows * ROW, tint);
		icon(g, font, p.getItemBySlot(EquipmentSlot.HEAD), EMPTY_SLOT[0], lx, ly + 1, ICON);
		icon(g, font, p.getItemBySlot(EquipmentSlot.CHEST), EMPTY_SLOT[1], lx, ly + ROW + 1, ICON);
		if (arrows > 0) counter(g, font, new ItemStack(Items.ARROW), String.valueOf(arrows), 0xFFFFFFFF, lx, ly + ROW * 2 + 1);

		// Derecha: mano derecha, pantalón, botas y espacios libres
		int rx = hotbarRight + 8 + (mc.options.attackIndicator().get() == net.minecraft.client.AttackIndicatorStatus.HOTBAR ? 24 : 0);
		int ry = g.guiHeight() - 3 - 4 * ROW;
		if (cfg.opacityPercent > 0) Draw.rounded(g, rx - 2, ry, ICON + 4, 4 * ROW, tint);
		ItemStack hand = p.getMainHandItem();
		if (!hand.isEmpty()) {
			g.item(hand, rx, ry + 1);
			g.itemDecorations(font, hand, rx, ry + 1);
		}
		icon(g, font, p.getItemBySlot(EquipmentSlot.LEGS), EMPTY_SLOT[2], rx, ry + ROW + 1, ICON);
		icon(g, font, p.getItemBySlot(EquipmentSlot.FEET), EMPTY_SLOT[3], rx, ry + ROW * 2 + 1, ICON);
		if (chestIcon == null) chestIcon = new ItemStack(Items.CHEST);
		int free = freeSlots(p);
		int freeColor = free <= 2 ? 0xFFFF6B6B : free <= 6 ? 0xFFFFD166 : 0xFFB8F5B8;
		counter(g, font, chestIcon, String.valueOf(free), freeColor, rx, ry + ROW * 3 + 1);
	}

	// ---------------------------------------------------------------- dibujo
	/** Icono nativo del item (con su barra de durabilidad) o la ranura vacía, a {@code size} píxeles. */
	private static void icon(GuiGraphicsExtractor g, Font font, ItemStack stack, Identifier emptySprite, int x, int y, int size) {
		float sc = size / (float) ICON;
		g.pose().pushMatrix();
		g.pose().translate(x, y);
		g.pose().scale(sc, sc);
		if (stack.isEmpty()) {
			g.blitSprite(RenderPipelines.GUI_TEXTURED, emptySprite, 0, 0, ICON, ICON, 0x66FFFFFF);
		} else {
			g.item(stack, 0, 0);
			g.itemDecorations(font, stack, 0, 0);
		}
		g.pose().popMatrix();
	}

	/** Icono con un número en la esquina inferior derecha, como la cantidad de un item. */
	private static void counter(GuiGraphicsExtractor g, Font font, ItemStack icon, String text, int color, int x, int y) {
		g.item(icon, x, y);
		g.text(font, text, x + ICON + 1 - font.width(text), y + 9, color, true);
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
