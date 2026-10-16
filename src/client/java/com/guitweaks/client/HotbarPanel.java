package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * Barra de items con el estilo del mod: mismo fondo, color de acento y opacidad que los demás paneles.
 * Los items se dibujan con el renderizador del juego, así que siguen el texture pack activo.
 * Las posiciones coinciden con las de la barra vanilla (182x22, items a +3,+3), para no descolocar
 * otros elementos del HUD.
 */
public final class HotbarPanel {
	private static final int WIDTH = 182;
	private static final int HEIGHT = 22;

	private static final Identifier ATTACK_BG = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_background");
	private static final Identifier ATTACK_PROGRESS = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_progress");

	private HotbarPanel() {}

	/** Dibuja un item con la animación de "salto" que el juego hace al recogerlo. */
	private static void popItem(GuiGraphicsExtractor g, Minecraft mc, ItemStack stack, int ix, int iy, float partial) {
		float pop = stack.getPopTime() - partial;
		if (pop > 0f) {
			float k = 1f + pop / 5f;
			g.pose().pushMatrix();
			g.pose().translate(ix + 8, iy + 12);
			g.pose().scale(1f / k, (k + 1f) / 2f);
			g.pose().translate(-(ix + 8), -(iy + 12));
		}
		g.item(stack, ix, iy);
		if (pop > 0f) g.pose().popMatrix();
		g.itemDecorations(mc.font, stack, ix, iy);
	}

	/** Indicador de ataque junto a la barra, si el jugador lo tiene configurado así en las opciones del juego. */
	private static void attackIndicator(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer p, int x0, int y0) {
		if (mc.options.attackIndicator().get() != AttackIndicatorStatus.HOTBAR) return;
		float f = p.getAttackStrengthScale(0f);
		if (f >= 1f) return;
		boolean right = p.getMainArm() == HumanoidArm.RIGHT;
		int x = right ? x0 + WIDTH + 6 : x0 - 24;
		int y = y0 + 2;
		Sprites.blit(g, ATTACK_BG, x, y, 18, 18);
		int h = (int) (f * 19f);
		Sprites.blitPart(g, ATTACK_PROGRESS, 18, 18, 0, 18 - h, x, y + 18 - h, 18, h);
	}

	/** ¿Debe dibujarse esta barra en vez de la vanilla? */
	public static boolean active(Minecraft mc) {
		return GuiTweaksConfig.get().hotbarStyle == 1 && mc.player != null && !mc.player.isSpectator();
	}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null) return;

		int accent = cfg.accentColor();
		int x0 = g.guiWidth() / 2 - WIDTH / 2;
		int y0 = g.guiHeight() - HEIGHT;
		float op = Math.max(0, Math.min(100, cfg.opacityPercent)) / 100f;
		float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		Draw.card(g, x0, y0, WIDTH, HEIGHT, accent, cfg.opacityPercent);

		int selected = p.getInventory().getSelectedSlot();
		for (int i = 0; i < 9; i++) {
			int cx = x0 + 1 + i * 20, cy = y0 + 1;
			// Casilla ligeramente más oscura; con opacidad 0 no se dibuja, solo la selección
			if (op > 0) Draw.rounded(g, cx + 1, cy + 1, 18, 18, Draw.withAlpha(0x000000, Math.round(70 * op)));
			if (i == selected) selection(g, cx, cy, accent);
			ItemStack stack = p.getInventory().getItem(i);
			if (!stack.isEmpty()) popItem(g, mc, stack, cx + 2, cy + 2, partial);
		}

		attackIndicator(g, mc, p, x0, y0);

		// Mano secundaria, al lado contrario de la mano principal
		ItemStack off = p.getOffhandItem();
		if (!off.isEmpty()) {
			boolean left = p.getMainArm() == HumanoidArm.RIGHT;
			int ox = left ? x0 - 29 : x0 + WIDTH + 7;
			Draw.card(g, ox, y0, 22, HEIGHT, accent, cfg.opacityPercent);
			popItem(g, mc, off, ox + 3, y0 + 3, partial);
		}
	}

	/** Marco de la casilla seleccionada, siempre visible aunque el fondo sea transparente. */
	private static void selection(GuiGraphicsExtractor g, int cx, int cy, int accent) {
		g.fill(cx + 1, cy + 1, cx + 19, cy + 19, Draw.withAlpha(accent, 45));
		int c = Draw.withAlpha(accent, 255);
		g.fill(cx, cy, cx + 20, cy + 2, c);
		g.fill(cx, cy + 18, cx + 20, cy + 20, c);
		g.fill(cx, cy + 2, cx + 2, cy + 18, c);
		g.fill(cx + 18, cy + 2, cx + 20, cy + 18, c);
	}
}
