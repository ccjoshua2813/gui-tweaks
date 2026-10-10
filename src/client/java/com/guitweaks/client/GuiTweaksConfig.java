package com.guitweaks.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Configuración guardada en config/guitweaks.json. */
public class GuiTweaksConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("guitweaks.json");

	/** Colores de acento disponibles (ARGB). */
	public static final int[] ACCENTS = {0xFF7CFC00, 0xFF4FC3F7, 0xFFFF5252, 0xFFB388FF, 0xFFFFC800, 0xFFEEEEEE};
	public static final String[] ACCENT_KEYS = {"green", "blue", "red", "purple", "gold", "white"};

	/** "RIGHT" o "LEFT": lado de la pantalla del panel de estado. */
	public String side = "RIGHT";
	public boolean hideVanillaBars = true;
	public boolean showHead = true;
	/** Cabeza en 3D que gira con la cámara, en vez de la cara plana. */
	public boolean head3d = false;
	/** Zoom de la cabeza 3D en porcentaje (50-150). */
	public int head3dZoom = 100;
	/** Ajuste de altura de la cabeza 3D, en centésimas de bloque (-30 a 30). */
	public int head3dHeight = 0;
	public boolean showNumbers = true;
	/** Animaciones sobre la cabeza: fuego, golpes, flechas, veneno, etc. */
	public boolean headEffects = true;
	public int accent = 0;
	/** Escala del panel en porcentaje (50-150). */
	public int scalePercent = 100;
	/** Opacidad del fondo en porcentaje (0-100). */
	public int opacityPercent = 70;
	/** Ancho de las barras en píxeles (60-140). */
	public int barWidth = 90;

	public boolean showInfoPanel = true;
	/** Estilo de la info: 0 = una línea bajo el panel, 1 = brújula plana, 2 = brújula circular (arriba al centro). */
	public int infoStyle = 1;
	/** Marcadores de otros jugadores en la brújula. */
	public boolean compassPlayers = true;
	/** true = cabeza del jugador, false = punto de color. */
	public boolean compassHeads = true;
	/** Alcance de los marcadores, en bloques (50-1000). */
	public int compassRange = 300;
	/** Píxeles que se baja la brújula (para no pisar la barra de jefe). */
	public int compassOffsetY = 0;
	public boolean showFps = true;
	public boolean showCoords = true;
	public boolean showDirection = true;

	/** Botones de comandos rápidos que aparecen al abrir el chat. */
	public boolean showQuickCommands = true;
	/** Botón de configuración en la pantalla de pausa (Esc). */
	public boolean showPauseButton = true;
	/** Panel de comandos del chat: true = centrado en la pantalla, false = arriba. */
	public boolean chatPanelCenter = true;
	/** Aviso temporal con nombre, encantamientos y durabilidad al cambiar de item. */
	public boolean showItemPopup = true;
	/** Segundos que se muestra el aviso del item (1-6). */
	public int popupSeconds = 3;
	/** Efectos de poción en un panel propio (reemplaza los iconos vanilla). */
	public boolean showEffectsPanel = true;
	/** Equipo: 0 = apagado, 1 = muñequito junto al panel, 2 = columnas junto a la barra de items. */
	public int equipmentMode = 0;
	/** Barra de items: 0 = vanilla, 1 = con el estilo del mod. */
	public int hotbarStyle = 0;

	/** Barra de vida del mob al que apuntas. */
	public boolean showMobHealth = true;
	/** Distancia máxima, en bloques (4-64). */
	public int mobDistance = 20;
	public boolean mobHostile = true;
	public boolean mobPassive = true;
	public boolean mobPlayers = false;
	public List<QuickCommand> quickCommands = defaultCommands();

	/** Un botón del chat. {@code send}: ejecuta al instante; si no, deja el comando escrito en el chat. */
	public static class QuickCommand {
		public String label = "";
		public String command = "";
		public boolean send = true;

		public QuickCommand() {}

		public QuickCommand(String label, String command, boolean send) {
			this.label = label;
			this.command = command;
			this.send = send;
		}
	}

	public static final int MAX_QUICK_COMMANDS = 6;

	private static List<QuickCommand> defaultCommands() {
		List<QuickCommand> l = new ArrayList<>();
		l.add(new QuickCommand("Home", "home", true));
		l.add(new QuickCommand("Jugadores conectados", "list", true));
		return l;
	}

	private static GuiTweaksConfig instance = new GuiTweaksConfig();

	public static GuiTweaksConfig get() {
		return instance;
	}

	public boolean isRight() {
		return !"LEFT".equalsIgnoreCase(side);
	}

	public int accentColor() {
		return ACCENTS[Math.floorMod(accent, ACCENTS.length)];
	}

	public void copyFrom(GuiTweaksConfig o) {
		side = o.side; hideVanillaBars = o.hideVanillaBars; showHead = o.showHead; head3d = o.head3d; head3dZoom = o.head3dZoom; head3dHeight = o.head3dHeight;
		showNumbers = o.showNumbers; headEffects = o.headEffects; accent = o.accent; scalePercent = o.scalePercent;
		opacityPercent = o.opacityPercent; barWidth = o.barWidth; showInfoPanel = o.showInfoPanel; infoStyle = o.infoStyle;
		compassPlayers = o.compassPlayers; compassHeads = o.compassHeads;
		compassRange = o.compassRange; compassOffsetY = o.compassOffsetY;
		showFps = o.showFps; showCoords = o.showCoords; showDirection = o.showDirection;
		showQuickCommands = o.showQuickCommands; showPauseButton = o.showPauseButton; chatPanelCenter = o.chatPanelCenter; showItemPopup = o.showItemPopup;
		popupSeconds = o.popupSeconds; showEffectsPanel = o.showEffectsPanel; equipmentMode = o.equipmentMode; hotbarStyle = o.hotbarStyle;
		showMobHealth = o.showMobHealth; mobDistance = o.mobDistance;
		mobHostile = o.mobHostile; mobPassive = o.mobPassive; mobPlayers = o.mobPlayers;
		quickCommands = new ArrayList<>();
		for (QuickCommand q : o.quickCommands) quickCommands.add(new QuickCommand(q.label, q.command, q.send));
	}

	public static void load() {
		try {
			if (Files.exists(FILE)) {
				GuiTweaksConfig loaded = GSON.fromJson(Files.readString(FILE), GuiTweaksConfig.class);
				if (loaded != null) instance = loaded;
			}
		} catch (Exception e) {
			System.err.println("[guitweaks] No se pudo leer la config: " + e);
		}
		instance.scalePercent = Math.max(50, Math.min(150, instance.scalePercent));
		instance.opacityPercent = Math.max(0, Math.min(100, instance.opacityPercent));
		instance.barWidth = Math.max(60, Math.min(140, instance.barWidth));
		instance.equipmentMode = Math.max(0, Math.min(2, instance.equipmentMode));
		instance.mobDistance = Math.max(4, Math.min(64, instance.mobDistance));
		instance.infoStyle = Math.max(0, Math.min(2, instance.infoStyle));
		instance.head3dZoom = Math.max(50, Math.min(150, instance.head3dZoom));
		instance.head3dHeight = Math.max(-30, Math.min(30, instance.head3dHeight));
		instance.compassRange = Math.max(50, Math.min(1000, instance.compassRange));
		instance.compassOffsetY = Math.max(0, Math.min(60, instance.compassOffsetY));
		instance.hotbarStyle = Math.max(0, Math.min(1, instance.hotbarStyle));
		instance.popupSeconds = Math.max(1, Math.min(6, instance.popupSeconds));
		if (instance.quickCommands == null) instance.quickCommands = defaultCommands();
		while (instance.quickCommands.size() > MAX_QUICK_COMMANDS) instance.quickCommands.remove(instance.quickCommands.size() - 1);
		instance.save();
	}

	public void save() {
		try {
			Files.writeString(FILE, GSON.toJson(this));
		} catch (IOException e) {
			System.err.println("[guitweaks] No se pudo guardar la config: " + e);
		}
	}
}
