package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PlayerRideableJumping;

/**
 * Barras que el juego dibuja con su estilo y que, al ocultar las vanilla, el mod sustituye:
 * la fuerza de salto al montar un caballo y el aire bajo el agua.
 */
public final class ExtraBars {
	private static final Identifier AIR = Identifier.withDefaultNamespace("hud/air");

	private ExtraBars() {}

	/** Fuerza de salto de la montura (caballo, mula...), sobre la barra de items. */
	public static void renderJump(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || !cfg.hideVanillaBars) return;
		PlayerRideableJumping vehicle = p.jumpableVehicle();
		if (vehicle == null) return;

		int w = 110, h = 4;
		int x = g.guiWidth() / 2 - w / 2, y = g.guiHeight() - 22 - 4 - h;
		boolean cooldown = vehicle.getJumpCooldown() > 0;
		float charge = cooldown ? 1f : Math.max(0f, Math.min(1f, p.getJumpRidingScale()));
		int accent = cfg.accentColor();
		int color = cooldown ? 0xFF6B6F78 : charge > 0.85f ? Draw.lerp(accent, 0xFFFFC800, 0.6f) : accent;

		Draw.pill(g, x - 1, y - 1, w + 2, h + 2, 0xFF000000);
		Draw.pill(g, x, y, w, h, 0xFF23252B);
		if (charge > 0f) {
			int fw = Math.max(h, Math.round(w * charge));
			Draw.pill(g, x, y, fw, h, Draw.shade(color, 1.2f), Draw.shade(color, 0.75f));
		}
	}

	/** Burbujas de aire: icono y barra en píldora que se vacía, con aviso rojo al quedar poco. */
	public static void renderAir(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || !cfg.hideVanillaBars) return;
		int max = p.getMaxAirSupply();
		int air = Math.max(0, p.getAirSupply());
		boolean under = p.isEyeInFluid(FluidTags.WATER);
		if (max <= 0 || (air >= max && !under)) return;

		float frac = Math.max(0f, Math.min(1f, air / (float) max));
		int w = 64, h = 4;
		int y = g.guiHeight() - 22 - 4 - h - (p.jumpableVehicle() != null ? 8 : 0);
		int x = g.guiWidth() / 2 - (w + 12) / 2 + 12;
		boolean low = frac < 0.2f;
		int color = low ? 0xFFE53935 : Draw.lerp(0xFF4FC3F7, 0xFF8FE3FF, frac);
		if (low && (System.currentTimeMillis() / 250) % 2 == 0) color = 0xFFFF8A80;

		g.blitSprite(RenderPipelines.GUI_TEXTURED, AIR, x - 12, y - 3, 9, 9);
		Draw.pill(g, x - 1, y - 1, w + 2, h + 2, 0xFF000000);
		Draw.pill(g, x, y, w, h, 0xFF23252B);
		if (frac > 0f) {
			int fw = Math.max(h, Math.round(w * frac));
			Draw.pill(g, x, y, fw, h, Draw.shade(color, 1.2f), Draw.shade(color, 0.75f));
		}
	}
}
