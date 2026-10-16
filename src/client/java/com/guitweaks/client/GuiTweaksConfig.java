package com.guitweaks.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Configuración guardada en config/guitweaks.json. */
public class GuiTweaksConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Gson COMPACT = new Gson();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("guitweaks.json");

	/** Colores de acento disponibles (ARGB). */
	public static final int[] ACCENTS = {0xFF7CFC00, 0xFF4FC3F7, 0xFFFF5252, 0xFFB388FF, 0xFFFFC800, 0xFFEEEEEE};
	public static final String[] ACCENT_KEYS = {"green", "blue", "red", "purple", "gold", "white"};

	/** "RIGHT" o "LEFT": lado de la pantalla del panel de estado. */
	// Valores de fábrica elegidos para que la primera impresión sea compacta: panel a la izquierda, escala 65 %, barras de 65 px y equipo junto a la barra de items.
	public String side = "LEFT";
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
	public int scalePercent = 65;
	/** Opacidad del fondo en porcentaje (0-100). */
	public int opacityPercent = 70;
	/** Ancho de las barras en píxeles (60-140). */
	public int barWidth = 65;

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
	public int compassOffsetY = 0;   // en desuso: sustituido por compassDropPercent
	/** Cuánto baja la brújula, de 0 (arriba) a 100 (justo por encima de las barras de salto, aire y montura). */
	public int compassDropPercent = 0;
	public boolean showFps = true;
	public boolean showCoords = true;
	public boolean showDirection = true;

	/** Desplazamiento (dx, dy) de cada panel respecto a su posición automática; lo cambia el editor. */
	public Map<String, int[]> panelOffsets = new HashMap<>();

	/** Botones de comandos rápidos que aparecen al abrir el chat. */
	public boolean showQuickCommands = true;
	/** Botón de configuración en la pantalla de pausa (Esc). */
	public boolean showPauseButton = true;
	/** (Experimental) Estela con la trayectoria de la flecha al apuntar con un arco o una ballesta cargada. */
	public boolean arrowTrail = false;
	/** Panel de comandos del chat: true = centrado en la pantalla, false = arriba. */
	public boolean chatPanelCenter = true;
	/** Aviso temporal con nombre, encantamientos y durabilidad al cambiar de item. */
	public boolean showItemPopup = true;
	/** Segundos que se muestra el aviso del item (1-6). */
	public int popupSeconds = 3;
	/** Efectos de poción en un panel propio (reemplaza los iconos vanilla). */
	public boolean showEffectsPanel = true;
	/** Equipo: 0 = apagado, 1 = muñequito junto al panel, 2 = columnas junto a la barra de items. */
	public int equipmentMode = 2;
	/** Barra de items: 0 = vanilla, 1 = con el estilo del mod. */
	public int hotbarStyle = 0;

	/** Barra de vida del mob al que apuntas. */
	public boolean showMobHealth = true;
	/** Distancia máxima, en bloques (4-64). */
	public int mobDistance = 20;
	public boolean mobHostile = true;
	public boolean mobPassive = true;
	public boolean mobPlayers = false;

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

	/** Conjunto de comandos rápidos con nombre (por ejemplo, uno por servidor). */
	public static class CommandGroup {
		public String name = "";
		public List<QuickCommand> commands = new ArrayList<>();

		public CommandGroup() {}

		public CommandGroup(String name, List<QuickCommand> commands) {
			this.name = name;
			this.commands = commands;
		}
	}

	/** Ranura de vista: un nombre y, si está ocupada, una copia de los ajustes de vista. */
	public static class ViewSlot {
		public String name = "";
		public GuiTweaksConfig data;
	}

	public static final int MAX_QUICK_COMMANDS = 6;
	public static final int MAX_COMMAND_GROUPS = 3;
	public static final int VIEW_SLOTS = 6;

	/** Grupos de comandos del chat (hasta {@link #MAX_COMMAND_GROUPS}) y el que está activo. */
	public List<CommandGroup> commandGroups;
	public int activeCommandGroup = 0;
	/** Solo para leer configuraciones antiguas (0.13 y anteriores): se pasa al primer grupo y se descarta. */
	public List<QuickCommand> quickCommands;

	/** Ranuras de vista guardadas y la última cargada o guardada (-1 = ninguna). */
	public List<ViewSlot> viewSlots;
	public int activeSlot = -1;

	/** Vista justo antes de la última carga de una ranura, para poder deshacerla (solo en memoria). */
	public static GuiTweaksConfig previousView;
	public static int previousSlot = -1;

	private static List<QuickCommand> defaultCommands() {
		List<QuickCommand> l = new ArrayList<>();
		l.add(new QuickCommand("Home", "home", true));
		l.add(new QuickCommand("Jugadores conectados", "list", true));
		return l;
	}

	private static GuiTweaksConfig instance = new GuiTweaksConfig();

	static {
		instance.ensureDefaults();
	}

	public static GuiTweaksConfig get() {
		return instance;
	}

	/** Configuración de fábrica completa (con un grupo de comandos y las 6 ranuras vacías). */
	public static GuiTweaksConfig defaults() {
		GuiTweaksConfig c = new GuiTweaksConfig();
		c.ensureDefaults();
		return c;
	}

	public boolean isRight() {
		return !"LEFT".equalsIgnoreCase(side);
	}

	public int accentColor() {
		return ACCENTS[Math.floorMod(accent, ACCENTS.length)];
	}

	// ---- Grupos de comandos ----

	public CommandGroup activeGroup() {
		return commandGroups.get(Math.floorMod(activeCommandGroup, commandGroups.size()));
	}

	public List<QuickCommand> commands() {
		return activeGroup().commands;
	}

	// ---- Copias ----

	/** Copia solo la vista: ajustes, apariencia y posiciones de los paneles. No toca comandos ni ranuras. */
	public void copyViewFrom(GuiTweaksConfig o) {
		side = o.side; hideVanillaBars = o.hideVanillaBars; showHead = o.showHead; head3d = o.head3d; head3dZoom = o.head3dZoom; head3dHeight = o.head3dHeight;
		showNumbers = o.showNumbers; headEffects = o.headEffects; accent = o.accent; scalePercent = o.scalePercent;
		opacityPercent = o.opacityPercent; barWidth = o.barWidth; showInfoPanel = o.showInfoPanel; infoStyle = o.infoStyle;
		compassPlayers = o.compassPlayers; compassHeads = o.compassHeads;
		compassRange = o.compassRange; compassOffsetY = o.compassOffsetY; compassDropPercent = o.compassDropPercent;
		showFps = o.showFps; showCoords = o.showCoords; showDirection = o.showDirection;
		panelOffsets = new HashMap<>();
		if (o.panelOffsets != null) for (Map.Entry<String, int[]> e : o.panelOffsets.entrySet()) panelOffsets.put(e.getKey(), e.getValue().clone());
		showQuickCommands = o.showQuickCommands; showPauseButton = o.showPauseButton; arrowTrail = o.arrowTrail; chatPanelCenter = o.chatPanelCenter; showItemPopup = o.showItemPopup;
		popupSeconds = o.popupSeconds; showEffectsPanel = o.showEffectsPanel; equipmentMode = o.equipmentMode; hotbarStyle = o.hotbarStyle;
		showMobHealth = o.showMobHealth; mobDistance = o.mobDistance;
		mobHostile = o.mobHostile; mobPassive = o.mobPassive; mobPlayers = o.mobPlayers;
	}

	/** Copia todo: la vista, los grupos de comandos y las ranuras. */
	public void copyFrom(GuiTweaksConfig o) {
		copyViewFrom(o);
		commandGroups = new ArrayList<>();
		if (o.commandGroups != null) {
			for (CommandGroup g : o.commandGroups) {
				List<QuickCommand> list = new ArrayList<>();
				for (QuickCommand q : g.commands) list.add(new QuickCommand(q.label, q.command, q.send));
				commandGroups.add(new CommandGroup(g.name, list));
			}
		}
		activeCommandGroup = o.activeCommandGroup;
		viewSlots = new ArrayList<>();
		if (o.viewSlots != null) {
			for (ViewSlot s : o.viewSlots) {
				ViewSlot c = new ViewSlot();
				c.name = s.name;
				c.data = s.data == null ? null : s.data.snapshotView();
				viewSlots.add(c);
			}
		}
		activeSlot = o.activeSlot;
		ensureDefaults();
	}

	/** Copia de solo la vista, sin comandos ni ranuras (lo que guarda cada ranura). */
	public GuiTweaksConfig snapshotView() {
		GuiTweaksConfig s = new GuiTweaksConfig();
		s.copyViewFrom(this);
		s.panelOffsets = new TreeMap<>(s.panelOffsets);   // orden fijo, para poder comparar
		return s;
	}

	private static String viewJson(GuiTweaksConfig c) {
		return COMPACT.toJson(c.snapshotView());
	}

	// ---- Ranuras de vista ----

	public boolean slotUsed(int i) {
		return i >= 0 && i < viewSlots.size() && viewSlots.get(i).data != null;
	}

	public boolean hasEmptySlot() {
		for (int i = 0; i < viewSlots.size(); i++) if (!slotUsed(i)) return true;
		return false;
	}

	/** ¿La vista actual tiene cambios que no están guardados en la ranura activa (o en ninguna)? */
	public boolean viewDirty() {
		String now = viewJson(this);
		if (slotUsed(activeSlot)) return !now.equals(viewJson(viewSlots.get(activeSlot).data));
		return !now.equals(viewJson(new GuiTweaksConfig()));
	}

	public void saveSlot(int i) {
		viewSlots.get(i).data = snapshotView();
		activeSlot = i;
	}

	/** Carga una ranura guardando antes la vista actual, para poder deshacer la carga. */
	public void loadSlot(int i) {
		if (!slotUsed(i)) return;
		previousView = snapshotView();
		previousSlot = activeSlot;
		copyViewFrom(viewSlots.get(i).data);
		activeSlot = i;
	}

	public boolean canUndoLoad() {
		return previousView != null;
	}

	public void undoLoad() {
		if (previousView == null) return;
		copyViewFrom(previousView);
		activeSlot = previousSlot;
		previousView = null;
		previousSlot = -1;
	}

	public void clearSlot(int i) {
		ViewSlot s = viewSlots.get(i);
		s.data = null;
		s.name = "";
		if (activeSlot == i) activeSlot = -1;
	}

	/** Copia la ranura a la primera libre. Devuelve la ranura destino o -1 si no hay ninguna libre. */
	public int duplicateSlot(int i) {
		if (!slotUsed(i)) return -1;
		for (int j = 0; j < viewSlots.size(); j++) {
			if (!slotUsed(j)) {
				ViewSlot from = viewSlots.get(i), to = viewSlots.get(j);
				to.data = from.data.snapshotView();
				String base = from.name == null ? "" : from.name;
				to.name = base.isBlank() ? "" : (base.length() > 14 ? base.substring(0, 14) : base) + " (2)";
				return j;
			}
		}
		return -1;
	}

	/** Siguiente ranura ocupada después de la activa (da la vuelta); -1 si no hay ninguna. */
	public int nextUsedSlot() {
		for (int k = 1; k <= viewSlots.size(); k++) {
			int j = Math.floorMod(activeSlot + k, viewSlots.size());
			if (slotUsed(j)) return j;
		}
		return -1;
	}

	/** Devuelve a su valor de fábrica solo los ajustes de una pestaña de la configuración. */
	public void resetTab(int tab) {
		GuiTweaksConfig d = new GuiTweaksConfig();
		switch (tab) {
			case 0 -> {
				side = d.side; showHead = d.showHead; head3d = d.head3d; head3dZoom = d.head3dZoom; head3dHeight = d.head3dHeight;
				headEffects = d.headEffects; showNumbers = d.showNumbers; barWidth = d.barWidth;
			}
			case 1 -> {
				showInfoPanel = d.showInfoPanel; infoStyle = d.infoStyle; showFps = d.showFps; showCoords = d.showCoords;
				showDirection = d.showDirection; compassPlayers = d.compassPlayers; compassHeads = d.compassHeads;
				compassRange = d.compassRange; compassOffsetY = d.compassOffsetY; compassDropPercent = d.compassDropPercent;
			}
			case 2 -> {
				hideVanillaBars = d.hideVanillaBars; hotbarStyle = d.hotbarStyle; equipmentMode = d.equipmentMode;
				showEffectsPanel = d.showEffectsPanel; showItemPopup = d.showItemPopup; popupSeconds = d.popupSeconds;
			}
			case 3 -> {
				showMobHealth = d.showMobHealth; mobDistance = d.mobDistance; mobHostile = d.mobHostile;
				mobPassive = d.mobPassive; mobPlayers = d.mobPlayers;
			}
			default -> {
				accent = d.accent; scalePercent = d.scalePercent; opacityPercent = d.opacityPercent;
				arrowTrail = d.arrowTrail; showPauseButton = d.showPauseButton;
				showQuickCommands = d.showQuickCommands; chatPanelCenter = d.chatPanelCenter;
			}
		}
	}

	/** Deja los datos coherentes: pasa los comandos antiguos a un grupo, y rellena grupos y ranuras que falten. */
	public void ensureDefaults() {
		if (commandGroups == null || commandGroups.isEmpty()) {
			commandGroups = new ArrayList<>();
			commandGroups.add(new CommandGroup("Principal", quickCommands != null ? quickCommands : defaultCommands()));
		}
		quickCommands = null;
		while (commandGroups.size() > MAX_COMMAND_GROUPS) commandGroups.remove(commandGroups.size() - 1);
		for (CommandGroup g : commandGroups) {
			if (g.name == null) g.name = "";
			if (g.commands == null) g.commands = new ArrayList<>();
			while (g.commands.size() > MAX_QUICK_COMMANDS) g.commands.remove(g.commands.size() - 1);
		}
		activeCommandGroup = Math.max(0, Math.min(commandGroups.size() - 1, activeCommandGroup));

		if (viewSlots == null) viewSlots = new ArrayList<>();
		while (viewSlots.size() < VIEW_SLOTS) viewSlots.add(new ViewSlot());
		while (viewSlots.size() > VIEW_SLOTS) viewSlots.remove(viewSlots.size() - 1);
		for (int i = 0; i < viewSlots.size(); i++) {
			if (viewSlots.get(i) == null) viewSlots.set(i, new ViewSlot());
			if (viewSlots.get(i).name == null) viewSlots.get(i).name = "";
		}
		if (activeSlot < -1 || activeSlot >= VIEW_SLOTS || !slotUsed(activeSlot)) activeSlot = -1;
		if (panelOffsets == null) panelOffsets = new HashMap<>();
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
		instance.compassDropPercent = Math.max(0, Math.min(100, instance.compassDropPercent));
		instance.hotbarStyle = Math.max(0, Math.min(1, instance.hotbarStyle));
		instance.popupSeconds = Math.max(1, Math.min(6, instance.popupSeconds));
		instance.ensureDefaults();
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
