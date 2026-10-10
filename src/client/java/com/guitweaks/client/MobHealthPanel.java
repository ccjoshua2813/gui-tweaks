package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Barrita de vida del mob al que apuntas, hasta una distancia configurable. */
public final class MobHealthPanel {
	private static final int BAR_W = 64;
	private static final int BAR_H = 5;
	private static final int PAD = 4;
	private static final float TEXT_SCALE = 0.8f;
	private static final long HOLD_MS = 120, FADE_MS = 220;

	private static LivingEntity target;
	private static long lastSeen;
	private static float shownHealth;

	private MobHealthPanel() {}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || !cfg.showMobHealth) return;

		long now = System.currentTimeMillis();
		LivingEntity found = find(mc, cfg);
		if (found != null) {
			if (found != target) {
				target = found;
				shownHealth = found.getHealth();
			}
			lastSeen = now;
		}
		if (target == null) return;
		long gone = now - lastSeen;
		if (gone > HOLD_MS + FADE_MS || target.isRemoved() || !target.isAlive()) {
			target = null;
			return;
		}
		float fade = gone > HOLD_MS ? 1f - (gone - HOLD_MS) / (float) FADE_MS : 1f;
		int alpha = Math.round(255 * fade);
		if (alpha < 8) return;

		// La vida mostrada se acerca poco a poco a la real
		float max = Math.max(1f, target.getMaxHealth());
		shownHealth += (target.getHealth() - shownHealth) * 0.2f;
		float frac = Math.max(0f, Math.min(1f, shownHealth / max));
		int barColor = frac > 0.5f ? Draw.lerp(0xFFF2C94C, 0xFF5CD65C, (frac - 0.5f) * 2f) : Draw.lerp(0xFFE53935, 0xFFF2C94C, frac * 2f);

		Font font = mc.font;
		String name = target.getDisplayName().getString();
		String hp = Math.round(Math.max(0f, target.getHealth())) + "/" + Math.round(max);
		int nameColor = target instanceof Enemy ? 0xFFFF9A9A : target instanceof Player ? cfg.accentColor() : 0xFFFFFFFF;

		float nameW = font.width(name) * TEXT_SCALE;
		float hpW = font.width(hp) * 0.7f;
		int w = Math.round(Math.max(nameW, BAR_W + 4 + hpW)) + PAD * 2;
		int h = PAD + 8 + 3 + BAR_H + PAD;

		float s = cfg.scalePercent / 100f;
		int px = Math.round(g.guiWidth() / 2f - w * s / 2f);
		int py = Math.round(g.guiHeight() / 2f + 16);

		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(s, s);
		Draw.card(g, 0, 0, w, h, Draw.withAlpha(cfg.accentColor(), alpha), Math.round(cfg.opacityPercent * fade));

		text(g, font, name, (w - nameW) / 2f, PAD, TEXT_SCALE, Draw.withAlpha(nameColor, alpha));

		int bx = Math.round((w - (BAR_W + 4 + hpW)) / 2f), by = PAD + 8 + 3;
		Draw.pill(g, bx - 1, by - 1, BAR_W + 2, BAR_H + 2, Draw.withAlpha(0x000000, alpha));
		Draw.pill(g, bx, by, BAR_W, BAR_H, Draw.withAlpha(0x23252B, alpha));
		if (frac > 0f) {
			int fw = Math.max(BAR_H, Math.round(BAR_W * frac));
			Draw.pill(g, bx, by, fw, BAR_H, Draw.withAlpha(Draw.shade(barColor, 1.2f), alpha), Draw.withAlpha(Draw.shade(barColor, 0.75f), alpha));
		}
		text(g, font, hp, bx + BAR_W + 4, by - 0.5f, 0.7f, Draw.withAlpha(0xFFFFFF, alpha));

		g.pose().popMatrix();
	}

	private static void text(GuiGraphicsExtractor g, Font font, String text, float x, float y, float scale, int color) {
		g.pose().pushMatrix();
		g.pose().translate(x, y);
		g.pose().scale(scale, scale);
		g.text(font, text, 0, 0, color, true);
		g.pose().popMatrix();
	}

	/** Mob al que apunta la cruz (o el jugador), dentro de la distancia y sin paredes en medio. */
	private static LivingEntity find(Minecraft mc, GuiTweaksConfig cfg) {
		Entity cam = mc.getCameraEntity();
		if (cam == null) return null;
		double dist = cfg.mobDistance;
		Vec3 eye = cam.getEyePosition(1f);
		Vec3 look = cam.getViewVector(1f);
		Vec3 end = eye.add(look.scale(dist));
		AABB box = cam.getBoundingBox().expandTowards(look.scale(dist)).inflate(1.0);

		EntityHitResult hit = ProjectileUtil.getEntityHitResult(cam, eye, end, box,
			e -> e != cam && e instanceof LivingEntity && !(e instanceof ArmorStand)
				&& !e.isSpectator() && e.isPickable() && !e.isInvisibleTo(mc.player) && allowed(e, cfg),
			dist * dist);
		if (hit == null) return null;

		// Si hay un bloque entre medio, no se ve
		BlockHitResult block = mc.level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, cam));
		if (block.getType() != HitResult.Type.MISS
			&& block.getLocation().distanceToSqr(eye) < hit.getLocation().distanceToSqr(eye)) return null;
		return (LivingEntity) hit.getEntity();
	}

	private static boolean allowed(Entity e, GuiTweaksConfig cfg) {
		if (e instanceof Player) return cfg.mobPlayers;
		if (e instanceof Enemy) return cfg.mobHostile;
		return cfg.mobPassive;
	}
}
