package com.guitweaks.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * (Experimental) Dibuja en el mundo la trayectoria que seguiría la flecha al soltar el arco o disparar
 * una ballesta cargada. Se simula la física de la flecha del juego (gravedad 0,05 por tick y
 * rozamiento 0,99) hasta que choca con un bloque, y el trazo se va desvaneciendo con la distancia.
 */
public final class ArrowTrail {
	private static final int MAX_TICKS = 200;
	private static final double GRAVITY = 0.05, DRAG = 0.99, WATER_DRAG = 0.6;

	private ArrowTrail() {}

	public static void render(LevelRenderContext ctx) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || mc.level == null || !cfg.arrowTrail || p.isSpectator()) return;

		double speed = launchSpeed(p);
		if (speed <= 0) return;

		float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float yaw = p.getViewYRot(partial), pitch = p.getViewXRot(partial);
		double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
		// Dirección de mirada de Minecraft
		Vec3 dir = new Vec3(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr));

		// La flecha sale un poco por debajo de los ojos y hereda parte de tu movimiento
		Vec3 pos = p.getEyePosition(partial).add(0, -0.1, 0);
		Vec3 move = p.getKnownMovement();
		Vec3 vel = dir.scale(speed).add(move.x, p.onGround() ? 0 : move.y, move.z);

		int color = cfg.accentColor();
		Vec3 prev = pos;
		Vec3 end = null;
		try (var ignored = ctx.levelRenderer().collectPerFrameRenderThreadGizmos()) {
			for (int t = 0; t < MAX_TICKS; t++) {
				Vec3 next = pos.add(vel);
				BlockHitResult hit = mc.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
				boolean hitBlock = hit.getType() != HitResult.Type.MISS;
				Vec3 stop = hitBlock ? hit.getLocation() : next;

				// Estela: más opaca cerca de ti y más tenue lejos
				float fade = 1f - 0.75f * (float) t / MAX_TICKS;
				int a = Math.round(255 * Mth.clamp(fade, 0.25f, 1f));
				if (t > 0 || stop.distanceToSqr(prev) > 0.0001) Gizmos.line(prev, stop, (a << 24) | (color & 0xFFFFFF), 2.5f);
				prev = stop;

				if (hitBlock) {
					end = stop;
					break;
				}
				boolean inWater = !mc.level.getFluidState(net.minecraft.core.BlockPos.containing(next)).isEmpty();
				vel = vel.scale(inWater ? WATER_DRAG : DRAG).subtract(0, GRAVITY, 0);
				pos = next;
				if (pos.y < mc.level.getMinY() - 16) break;
			}
			// Marca del punto de impacto
			if (end != null) {
				Gizmos.point(end, 0xFFFFFFFF, 9f);
				Gizmos.point(end, (0xFF << 24) | (color & 0xFFFFFF), 6f);
			}
		}
	}

	/** Velocidad inicial de la flecha si se soltara ahora, o 0 si no se está apuntando con nada. */
	private static double launchSpeed(LocalPlayer p) {
		ItemStack use = p.isUsingItem() ? p.getUseItem() : ItemStack.EMPTY;
		if (use.is(Items.BOW)) {
			float f = p.getTicksUsingItem() / 20f;
			f = (f * f + f * 2f) / 3f;
			if (f < 0.1f) return 0;
			return Math.min(1f, f) * 3.0;
		}
		// Ballesta ya cargada, en cualquiera de las manos
		for (ItemStack s : new ItemStack[] {p.getMainHandItem(), p.getOffhandItem()}) {
			if (s.is(Items.CROSSBOW)) {
				ChargedProjectiles loaded = s.get(DataComponents.CHARGED_PROJECTILES);
				if (loaded != null && !loaded.isEmpty()) return 3.15;
			}
		}
		return 0;
	}
}
