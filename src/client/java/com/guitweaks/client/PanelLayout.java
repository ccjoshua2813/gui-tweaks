package com.guitweaks.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Posiciones de los paneles del mod. Cada panel suma a su posición automática un desplazamiento que el
 * jugador puede cambiar arrastrándolo en el editor, y anota el rectángulo que ocupa para que el editor
 * sepa dónde está. Todo ocurre en el hilo de render.
 */
public final class PanelLayout {
	/** true mientras está abierto el editor: los paneles se dibujan con datos de muestra. */
	public static boolean editing;

	public record Rect(String id, int x, int y, int w, int h, long time) {}

	private static final Map<String, Rect> RECTS = new LinkedHashMap<>();

	private PanelLayout() {}

	public static int dx(String id) {
		int[] o = GuiTweaksConfig.get().panelOffsets.get(id);
		return o == null ? 0 : o[0];
	}

	public static int dy(String id) {
		int[] o = GuiTweaksConfig.get().panelOffsets.get(id);
		return o == null ? 0 : o[1];
	}

	public static void setOffset(String id, int dx, int dy) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		if (dx == 0 && dy == 0) cfg.panelOffsets.remove(id);
		else cfg.panelOffsets.put(id, new int[] {dx, dy});
	}

	/** Anota el rectángulo (en píxeles de pantalla) que ocupa un panel en este fotograma. */
	public static void record(String id, int x, int y, int w, int h) {
		RECTS.put(id, new Rect(id, x, y, w, h, System.currentTimeMillis()));
	}

	public static Rect get(String id) {
		return RECTS.get(id);
	}

	/** Paneles dibujados en los últimos instantes (los que no están en pantalla desaparecen solos). */
	public static List<Rect> recent() {
		long limit = System.currentTimeMillis() - 500;
		List<Rect> out = new ArrayList<>();
		for (Rect r : RECTS.values()) if (r.time() >= limit) out.add(r);
		return out;
	}
}
