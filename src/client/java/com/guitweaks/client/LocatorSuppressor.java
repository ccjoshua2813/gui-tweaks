package com.guitweaks.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Quita los puntitos de jugadores del localizador vanilla (los que salen justo sobre la barra de items).
 *
 * El juego dibuja la barra de información (XP, localizador o salto) en dos llamadas: el fondo, que Fabric
 * deja ocultar, y el contenido (aquí los puntos de los jugadores), que Fabric no cubre. En vez de tocar el
 * código del juego, mientras las barras vanilla estén ocultas se cambia la barra "localizador" por una
 * vacía, y al volver a mostrarlas se restaura la original. Nuestra brújula ocupa su lugar.
 */
public final class LocatorSuppressor {
	private static Field barsField, currentField;
	private static Object locatorKey, emptyKey;
	private static Supplier<ContextualBar> original;
	private static boolean suppressed, failed;

	private LocatorSuppressor() {}

	/** Se llama en cada tick del cliente. */
	@SuppressWarnings("unchecked")
	public static void tick(Minecraft mc) {
		if (failed || mc.gui == null) return;
		boolean want = GuiTweaksConfig.get().hideVanillaBars;
		if (want == suppressed) return;
		try {
			Hud hud = mc.gui.hud;
			if (barsField == null) {
				barsField = Hud.class.getDeclaredField("contextualInfoBars");
				barsField.setAccessible(true);
				currentField = Hud.class.getDeclaredField("contextualInfoBar");
				currentField.setAccessible(true);
				// El tipo de estado es privado en el juego: sus valores se buscan por nombre
				for (Object constant : Class.forName("net.minecraft.client.gui.Hud$ContextualInfo").getEnumConstants()) {
					String name = ((Enum<?>) constant).name();
					if (name.equals("LOCATOR")) locatorKey = constant;
					if (name.equals("EMPTY")) emptyKey = constant;
				}
				if (locatorKey == null || emptyKey == null) throw new IllegalStateException("estados de barra no encontrados");
			}
			Map<Object, Supplier<ContextualBar>> map = (Map<Object, Supplier<ContextualBar>>) barsField.get(hud);
			if (want) {
				original = map.get(locatorKey);
				swap(hud, map, () -> ContextualBar.EMPTY);
			} else if (original != null) {
				swap(hud, map, original);
			}
			// El juego conserva la barra elegida mientras no cambie de estado: se fuerza a elegirla de nuevo
			currentField.set(hud, Pair.of(emptyKey, ContextualBar.EMPTY));
			suppressed = want;
		} catch (Throwable t) {
			failed = true;
			System.err.println("[guitweaks] No se pudo ocultar el localizador vanilla: " + t);
		}
	}

	private static void swap(Hud hud, Map<Object, Supplier<ContextualBar>> map, Supplier<ContextualBar> bar) throws Exception {
		try {
			map.put(locatorKey, bar);
		} catch (UnsupportedOperationException immutable) {
			// El mapa del juego no admite cambios: se sustituye por una copia
			Map<Object, Supplier<ContextualBar>> copy = new HashMap<>(map);
			copy.put(locatorKey, bar);
			barsField.set(hud, copy);
		}
	}
}
