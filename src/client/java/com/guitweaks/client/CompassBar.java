package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Brújula plana arriba al centro: los puntos cardinales se desplazan al girar, con los otros jugadores
 * marcados (su cabeza o un punto) en la dirección en que están, y debajo los FPS y las coordenadas.
 * Los jugadores salen del sistema de localizador del juego (también los lejanos, si el servidor lo envía).
 */
public final class CompassBar {
	private static final int BAR_W = 170;          // ancho de la tira, 180 grados de visión
	private static final int PAD = 5;
	private static final int MARKER_H = 11;
	private static final int STRIP_H = 12;
	private static final int INFO_H = 9;
	private static final int TOP = 4;
	private static final float TEXT_SCALE = 0.8f;
	private static final String[] POINTS = {"N", "NE", "E", "SE", "S", "SO", "O", "NO"};

	private record Marker(UUID id, float angle, double distance, int color) {}

	private CompassBar() {}

	public static void render(GuiGraphicsExtractor g) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;

		if (cfg.infoStyle == 2) {
			renderDial(g, mc, cfg);
			return;
		}
		boolean strip = cfg.showDirection;
		boolean markers = strip && cfg.compassPlayers;
		boolean info = cfg.showFps || cfg.showCoords;
		if (!strip && !info) return;

		Entity cam = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
		float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float yaw = cam.getViewYRot(partial);

		int w = BAR_W + PAD * 2;
		int h = PAD + (markers ? MARKER_H : 0) + (strip ? STRIP_H + (info ? 3 : 0) : 0) + (info ? INFO_H : 0) + PAD;
		float s = cfg.scalePercent / 100f;
		int px = Math.round(g.guiWidth() / 2f - w * s / 2f);
		int py = TOP + cfg.compassOffsetY;

		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(s, s);
		Draw.card(g, 0, 0, w, h, cfg.accentColor(), cfg.opacityPercent);

		Font font = mc.font;
		int cx = w / 2;
		int y = PAD;
		if (markers) {
			drawMarkers(g, mc, cfg, cam, partial, yaw, cx, y);
			y += MARKER_H;
		}
		if (strip) {
			drawStrip(g, font, cfg, yaw, cx, y);
			y += STRIP_H + (info ? 3 : 0);
		}
		if (info) drawInfo(g, font, mc, cfg, cx, y);
		g.pose().popMatrix();
	}

	// ---------------------------------------------------------------- brújula circular
	private static final int DIAL_R = 22;

	/** Dial redondo: la rosa de los vientos gira al moverte y los jugadores quedan sobre el anillo. */
	private static void renderDial(GuiGraphicsExtractor g, Minecraft mc, GuiTweaksConfig cfg) {
		boolean dial = cfg.showDirection;
		boolean info = cfg.showFps || cfg.showCoords;
		if (!dial && !info) return;

		Font font = mc.font;
		Entity cam = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
		float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float yaw = cam.getViewYRot(partial);

		int infoW = info ? infoWidth(font, mc, cfg) + 2 : 0;
		int dialSize = dial ? (DIAL_R + 6) * 2 : 0;
		int w = Math.max(dialSize, infoW) + PAD * 2;
		int h = PAD + (dial ? dialSize + (info ? 2 : 0) : 0) + (info ? INFO_H : 0) + PAD;
		float s = cfg.scalePercent / 100f;
		int px = Math.round(g.guiWidth() / 2f - w * s / 2f);
		int py = TOP + cfg.compassOffsetY;

		g.pose().pushMatrix();
		g.pose().translate(px, py);
		g.pose().scale(s, s);
		Draw.card(g, 0, 0, w, h, cfg.accentColor(), cfg.opacityPercent);

		int cx = w / 2;
		int y = PAD;
		if (dial) {
			int cy = y + dialSize / 2;
			int accent = cfg.accentColor();
			float heading = Mth.wrapDegrees(yaw + 180f);

			// Anillo con marcas cada 10 grados; las de los puntos cardinales son más largas
			for (int b = 0; b < 360; b += 10) {
				double ang = Math.toRadians(b - heading);
				int len = b % 90 == 0 ? 4 : b % 30 == 0 ? 3 : 1;
				int color = b == 0 ? Draw.withAlpha(accent, 255) : Draw.withAlpha(0xFFFFFF, b % 90 == 0 ? 220 : 90);
				for (int i = 0; i < len; i++) {
					int tx = cx + (int) Math.round(Math.sin(ang) * (DIAL_R - i));
					int ty = cy - (int) Math.round(Math.cos(ang) * (DIAL_R - i));
					g.fill(tx, ty, tx + 1, ty + 1, color);
				}
			}
			// Letras dentro del anillo, siempre derechas
			String[] letters = {"N", "E", "S", "O"};
			for (int k = 0; k < 4; k++) {
				double ang = Math.toRadians(k * 90 - heading);
				float tw = font.width(letters[k]) * TEXT_SCALE;
				float lx = cx + (float) (Math.sin(ang) * (DIAL_R - 9)) - tw / 2f;
				float ly = cy - (float) (Math.cos(ang) * (DIAL_R - 9)) - 3.5f;
				g.pose().pushMatrix();
				g.pose().translate(lx, ly);
				g.pose().scale(TEXT_SCALE, TEXT_SCALE);
				g.text(font, letters[k], 0, 0, k == 0 ? Draw.withAlpha(accent, 255) : 0xFFFFFFFF, true);
				g.pose().popMatrix();
			}
			// Puntero fijo arriba
			g.fill(cx - 1, cy - DIAL_R - 4, cx + 2, cy - DIAL_R - 3, Draw.withAlpha(accent, 255));
			g.fill(cx, cy - DIAL_R - 3, cx + 1, cy - DIAL_R - 2, Draw.withAlpha(accent, 255));
			// Rumbo en grados, en el centro
			String deg = Math.round(heading < 0 ? heading + 360 : heading) + "\u00b0";
			float dw = font.width(deg) * 0.7f;
			g.pose().pushMatrix();
			g.pose().translate(cx - dw / 2f, cy - 3);
			g.pose().scale(0.7f, 0.7f);
			g.text(font, deg, 0, 0, 0xFFC8C8C8, true);
			g.pose().popMatrix();

			// Jugadores sobre el anillo
			if (cfg.compassPlayers) {
				List<Marker> list = collect(mc, cfg, cam, partial, yaw);
				list.sort(Comparator.comparingDouble(Marker::distance).reversed());
				for (Marker m : list) {
					double ang = Math.toRadians(m.angle());
					int mx = cx + (int) Math.round(Math.sin(ang) * (DIAL_R + 1));
					int my = cy - (int) Math.round(Math.cos(ang) * (DIAL_R + 1));
					float near = 1f - 0.45f * (float) Math.min(1.0, m.distance() / cfg.compassRange);
					int alpha = Math.round(255 * near);
					PlayerInfo pi = cfg.compassHeads && m.id() != null && mc.getConnection() != null
						? mc.getConnection().getPlayerInfo(m.id()) : null;
					if (pi != null) {
						g.fill(mx - 5, my - 5, mx + 5, my + 5, 0xFF000000);
						PlayerFaceExtractor.extractRenderState(g, pi.getSkin(), mx - 4, my - 4, 8);
					} else {
						g.fill(mx - 3, my - 3, mx + 3, my + 3, Draw.withAlpha(0x000000, alpha));
						g.fill(mx - 2, my - 2, mx + 2, my + 2, Draw.withAlpha(m.color(), alpha));
					}
				}
			}
			y += dialSize + (info ? 2 : 0);
		}
		if (info) drawInfo(g, font, mc, cfg, cx, y);
		g.pose().popMatrix();
	}

	private static int infoWidth(Font font, Minecraft mc, GuiTweaksConfig cfg) {
		float total = 0;
		int n = 0;
		if (cfg.showFps) { total += font.width("FPS") + 3 + font.width("999"); n++; }
		if (cfg.showCoords) { total += font.width("XYZ") + 3 + font.width("-0000 000 -0000"); n++; }
		total += 8 * Math.max(0, n - 1);
		return Math.round(total * TEXT_SCALE);
	}

	// ---------------------------------------------------------------- tira de direcciones
	private static void drawStrip(GuiGraphicsExtractor g, Font font, GuiTweaksConfig cfg, float yaw, int cx, int y) {
		int accent = cfg.accentColor();
		float heading = Mth.wrapDegrees(yaw + 180f);           // 0 = norte, 90 = este
		float ppd = BAR_W / 180f;

		g.fill(cx - BAR_W / 2, y + 10, cx + BAR_W / 2, y + 11, Draw.withAlpha(0xFFFFFF, 50));
		for (int b = 0; b < 360; b += 15) {
			float diff = Mth.wrapDegrees(b - heading);
			if (Math.abs(diff) > 90f) continue;
			int x = Math.round(cx + diff * ppd);
			int a = Math.round(255 * Math.max(0f, Math.min(1f, (90f - Math.abs(diff)) / 30f)));
			if (b % 45 == 0) {
				String label = POINTS[b / 45];
				int color = b == 0 ? accent : b % 90 == 0 ? 0xFFFFFF : 0xB0B0B0;
				float tw = font.width(label) * TEXT_SCALE;
				g.pose().pushMatrix();
				g.pose().translate(x - tw / 2f, y + 1);
				g.pose().scale(TEXT_SCALE, TEXT_SCALE);
				g.text(font, label, 0, 0, Draw.withAlpha(color, a), true);
				g.pose().popMatrix();
			} else {
				g.fill(x, y + 4, x + 1, y + 9, Draw.withAlpha(0xFFFFFF, Math.round(a * 0.4f)));
			}
		}
		// Puntero central
		g.fill(cx, y + 9, cx + 1, y + 11, Draw.withAlpha(accent, 255));
		g.fill(cx - 1, y + 10, cx + 2, y + 11, Draw.withAlpha(accent, 255));
	}

	// ---------------------------------------------------------------- jugadores
	private static void drawMarkers(GuiGraphicsExtractor g, Minecraft mc, GuiTweaksConfig cfg, Entity cam, float partial,
									float yaw, int cx, int y) {
		List<Marker> list = collect(mc, cfg, cam, partial, yaw);
		list.sort(Comparator.comparingDouble(Marker::distance).reversed());   // los cercanos quedan encima
		float ppd = BAR_W / 180f;
		for (Marker m : list) {
			boolean edge = Math.abs(m.angle()) > 90f;
			float a = Math.max(-90f, Math.min(90f, m.angle()));
			int x = Math.round(cx + a * ppd);
			float near = 1f - 0.45f * (float) Math.min(1.0, m.distance() / cfg.compassRange);
			int alpha = Math.round(255 * near * (edge ? 0.6f : 1f));

			PlayerInfo info = !edge && cfg.compassHeads && m.id() != null && mc.getConnection() != null
				? mc.getConnection().getPlayerInfo(m.id()) : null;
			if (info != null) {
				g.fill(x - 5, y, x + 5, y + 10, 0xFF000000);
				PlayerFaceExtractor.extractRenderState(g, info.getSkin(), x - 4, y + 1, 8);
			} else {
				g.fill(x - 3, y + 2, x + 3, y + 8, Draw.withAlpha(0x000000, alpha));
				g.fill(x - 2, y + 3, x + 2, y + 7, Draw.withAlpha(m.color(), alpha));
			}
		}
	}

	/** Jugadores a marcar: los del localizador del juego o, si el servidor no los envía, los cercanos. */
	private static List<Marker> collect(Minecraft mc, GuiTweaksConfig cfg, Entity cam, float partial, float yaw) {
		List<Marker> out = new ArrayList<>();
		TrackedWaypoint.Camera camera = new TrackedWaypoint.Camera() {
			@Override public float yaw() { return yaw; }
			@Override public Vec3 position() { return cam.getEyePosition(partial); }
		};
		PartialTickSupplier partialSupplier = e -> partial;

		var connection = mc.getConnection();
		boolean found = false;
		if (connection != null && connection.getWaypointManager().hasWaypoints()) {
			connection.getWaypointManager().forEachWaypoint(cam, wp -> {
				double dist = Math.sqrt(wp.distanceSquared(cam));
				if (dist > cfg.compassRange) return;
				UUID id = wp.id().left().orElse(null);
				if (id != null && id.equals(mc.player.getUUID())) return;
				float angle = (float) wp.yawAngleToCamera(mc.level, camera, partialSupplier);
				int color = wp.icon().color.orElseGet(() -> colorOf(id != null ? id.hashCode() : 0)) | 0xFF000000;
				out.add(new Marker(id, angle, dist, color));
			});
			found = !out.isEmpty();
		}
		if (!found) {
			Vec3 eye = cam.getEyePosition(partial);
			for (AbstractClientPlayer p : mc.level.players()) {
				if (p == mc.player || p == cam || p.isSpectator()) continue;
				double dx = p.getX() - eye.x, dz = p.getZ() - eye.z;
				double dist = Math.sqrt(dx * dx + dz * dz);
				if (dist > cfg.compassRange) continue;
				// Dirección de Minecraft (0 = sur, 90 = oeste), relativa a hacia donde miras
				float angle = Mth.wrapDegrees((float) (Mth.atan2(-dx, dz) * 180.0 / Math.PI) - yaw);
				out.add(new Marker(p.getUUID(), angle, dist, colorOf(p.getUUID().hashCode())));
			}
		}
		return out;
	}

	private static int colorOf(int hash) {
		return Mth.hsvToRgb((Math.abs(hash) % 360) / 360f, 0.65f, 1f) | 0xFF000000;
	}

	// ---------------------------------------------------------------- FPS y coordenadas
	private static void drawInfo(GuiGraphicsExtractor g, Font font, Minecraft mc, GuiTweaksConfig cfg, int cx, int y) {
		List<String[]> items = new ArrayList<>();
		if (cfg.showFps) items.add(new String[] {"FPS", String.valueOf(mc.getFps())});
		if (cfg.showCoords) items.add(new String[] {"XYZ",
			(int) Math.floor(mc.player.getX()) + " " + (int) Math.floor(mc.player.getY()) + " " + (int) Math.floor(mc.player.getZ())});

		float total = 0;
		for (String[] it : items) total += font.width(it[0]) + 3 + font.width(it[1]);
		total += 8 * (items.size() - 1);
		total *= TEXT_SCALE;

		g.pose().pushMatrix();
		g.pose().translate(cx - total / 2f, y);
		g.pose().scale(TEXT_SCALE, TEXT_SCALE);
		int x = 0;
		for (String[] it : items) {
			g.text(font, it[0], x, 0, cfg.accentColor(), true);
			x += font.width(it[0]) + 3;
			g.text(font, it[1], x, 0, 0xFFFFFFFF, true);
			x += font.width(it[1]) + 8;
		}
		g.pose().popMatrix();
	}
}
