package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;

/**
 * Barras que el juego dibuja con su estilo y que, al ocultar las vanilla, el mod sustituye:
 * la fuerza de salto al montar un caballo y el aire bajo el agua.
 */
public final class ExtraBars {
	private static final Identifier AIR = Identifier.withDefaultNamespace("hud/air");
	private static final Identifier MOUNT_EMPTY = Identifier.withDefaultNamespace("hud/heart/vehicle_container");
	private static final Identifier MOUNT_FULL = Identifier.withDefaultNamespace("hud/heart/vehicle_full");
	private static final Identifier MOUNT_HALF = Identifier.withDefaultNamespace("hud/heart/vehicle_half");

	private ExtraBars() {}

	/** Fuerza de salto de la montura (caballo, mula...), sobre la barra de items. */
	public static void renderJump(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || !cfg.hideVanillaBars) return;
		PlayerRideableJumping vehicle = p.jumpableVehicle();
		if (vehicle == null) return;

		int w = 120, h = 3;
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

	/**
	 * Vida de la montura: los mismos iconos del juego (siguen el texture pack), pero centrados
	 * sobre la barra de items y más pequeños, en vez de pegados a la derecha.
	 */
	public static void renderMount(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || !cfg.hideVanillaBars || !(p.getVehicle() instanceof LivingEntity mount)) return;

		int total = Math.max(1, (int) (mount.getMaxHealth() + 0.5f) / 2);       // un icono por cada 2 puntos
		int hp = (int) Math.ceil(mount.getHealth());
		int perRow = 10;
		float scale = 0.75f;
		int rows = (total + perRow - 1) / perRow;

		// Justo encima de la barra de salto (o de la barra de items si la montura no salta)
		int baseY = g.guiHeight() - 22 - 4 - (p.jumpableVehicle() != null ? 3 + 4 : 0) - Math.round(9 * scale);
		for (int r = 0; r < rows; r++) {
			int inRow = Math.min(perRow, total - r * perRow);
			float rowW = ((inRow - 1) * 8 + 9) * scale;
			float x0 = g.guiWidth() / 2f - rowW / 2f;
			float y0 = baseY - r * 10 * scale;
			g.pose().pushMatrix();
			g.pose().translate(x0, y0);
			g.pose().scale(scale, scale);
			for (int i = 0; i < inRow; i++) {
				int idx = r * perRow + i;
				int ix = i * 8;
				g.blitSprite(RenderPipelines.GUI_TEXTURED, MOUNT_EMPTY, ix, 0, 9, 9);
				if (idx * 2 + 1 < hp) g.blitSprite(RenderPipelines.GUI_TEXTURED, MOUNT_FULL, ix, 0, 9, 9);
				else if (idx * 2 + 1 == hp) g.blitSprite(RenderPipelines.GUI_TEXTURED, MOUNT_HALF, ix, 0, 9, 9);
			}
			g.pose().popMatrix();
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
		// Mismo grosor, ancho y posición que la barra de salto (nunca se usan a la vez)
		int w = 120, h = 3;
		int x = g.guiWidth() / 2 - w / 2, y = g.guiHeight() - 22 - 4 - h;
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
