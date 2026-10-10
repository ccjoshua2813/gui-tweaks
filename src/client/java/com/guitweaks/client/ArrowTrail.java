package com.guitweaks.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
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
		Entity hitEntity = null;
		try (var ignored = ctx.levelRenderer().collectPerFrameRenderThreadGizmos()) {
			for (int t = 0; t < MAX_TICKS; t++) {
				Vec3 next = pos.add(vel);
				BlockHitResult hit = mc.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
				boolean hitBlock = hit.getType() != HitResult.Type.MISS;
				Vec3 stop = hitBlock ? hit.getLocation() : next;

				// Colisión con mobs y jugadores, como la flecha del juego (el tirador no cuenta al principio)
				final int tick = t;
				AABB box = new AABB(pos, stop).inflate(1.0);
				EntityHitResult eHit = ProjectileUtil.getEntityHitResult(p, pos, stop, box,
					e -> !e.isSpectator() && e.isPickable() && (e != p || tick > 5), stop.distanceToSqr(pos) + 1.0);
				boolean hitMob = eHit != null;
				if (hitMob) stop = eHit.getLocation();

				// Estela: más opaca cerca de ti y más tenue lejos
				float fade = 1f - 0.7f * (float) t / MAX_TICKS;
				int a = Math.round(255 * Mth.clamp(fade, 0.3f, 1f));
				Gizmos.line(prev, stop, (a << 24) | (color & 0xFFFFFF), 3f);
				// Un punto cada 4 ticks: ayuda a ver la profundidad y la caída desde primera persona
				if (t % 4 == 0 && t > 0) Gizmos.point(stop, (a << 24) | (color & 0xFFFFFF), 5f);
				prev = stop;

				if (hitMob || hitBlock) {
					end = stop;
					hitEntity = hitMob ? eHit.getEntity() : null;
					break;
				}
				boolean inWater = !mc.level.getFluidState(net.minecraft.core.BlockPos.containing(next)).isEmpty();
				vel = vel.scale(inWater ? WATER_DRAG : DRAG).subtract(0, GRAVITY, 0);
				pos = next;
				if (pos.y < mc.level.getMinY() - 16) break;
			}

			// Punto de impacto bien visible, incluso lejos: aro, punto y un mástil que se ve por encima del terreno
			if (end != null) {
				int mark = 0xFF000000 | (color & 0xFFFFFF);
				double dist = end.distanceTo(p.getEyePosition(partial));
				float size = (float) Mth.clamp(0.25 + dist * 0.012, 0.25, 1.5);
				Gizmos.circle(end, size, GizmoStyle.stroke(mark, 3f)).setAlwaysOnTop();
				Gizmos.point(end, 0xFFFFFFFF, 11f).setAlwaysOnTop();
				Gizmos.point(end, mark, 7f).setAlwaysOnTop();
				Gizmos.line(end, end.add(0, 1.5 + dist * 0.02, 0), mark, 2f).setAlwaysOnTop();
				// El objetivo se marca con un borde muy fino del color de acento (sin verse a través de paredes)
				if (hitEntity != null) Gizmos.cuboid(hitEntity.getBoundingBox(), GizmoStyle.stroke(mark, 1f));
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
