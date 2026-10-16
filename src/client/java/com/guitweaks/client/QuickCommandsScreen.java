package com.guitweaks.client;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Editor de los botones del chat, con sugerencias de comandos. Los botones se reparten en grupos con
 * nombre (hasta 3, por ejemplo uno por servidor) y en el chat se alterna entre ellos con un botón largo.
 */
public class QuickCommandsScreen extends Screen {
	private static final int ROW = 22;
	private static final int CHIPS = 6;
	private static final String[] COMMON = {
		"list", "tp", "gamemode", "kick", "ban", "pardon", "whitelist", "op", "time", "weather", "give",
		"say", "msg", "kill", "clear", "effect", "difficulty", "seed", "tps", "spawn", "home", "warp", "shop", "help"
	};

	private final Screen parent;
	private final List<EditBox> commandBoxes = new ArrayList<>();
	private final Button[] chips = new Button[CHIPS];
	private final String[] chipValues = new String[CHIPS];
	private EditBox activeBox;
	private String lastActiveValue = "";

	public QuickCommandsScreen(Screen parent) {
		super(Component.translatable("guitweaks.commands.title"));
		this.parent = parent;
	}

	private static Component t(String key) {
		return Component.translatable("guitweaks.commands." + key);
	}

	/** Nombre que se muestra de un grupo: el suyo o, si no tiene, "Grupo N". */
	static String groupName(GuiTweaksConfig.CommandGroup g, int index) {
		return g.name == null || g.name.isBlank() ? Component.translatable("guitweaks.commands.group_default", index + 1).getString() : g.name;
	}

	@Override
	protected void init() {
		commandBoxes.clear();
		activeBox = null;
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		int x = width / 2 - 170;

		// Pestañas de grupo (una por servidor) y botón para crear otro
		int tabW = 84, tabGap = 3, n = cfg.commandGroups.size();
		for (int i = 0; i < n; i++) {
			final int gi = i;
			Button tab = Button.builder(Component.literal(groupName(cfg.commandGroups.get(i), i)), b -> {
				cfg.activeCommandGroup = gi;
				rebuildWidgets();
			}).bounds(x + i * (tabW + tabGap), 22, tabW, 18).build();
			tab.active = gi != cfg.activeCommandGroup;
			addRenderableWidget(tab);
		}
		if (n < GuiTweaksConfig.MAX_COMMAND_GROUPS) {
			addRenderableWidget(Button.builder(Component.literal("+"), b -> {
				cfg.commandGroups.add(new GuiTweaksConfig.CommandGroup("", new ArrayList<>()));
				cfg.activeCommandGroup = cfg.commandGroups.size() - 1;
				rebuildWidgets();
			}).bounds(x + n * (tabW + tabGap), 22, 20, 18).build());
		}

		// Nombre del grupo activo, botón de borrarlo y de añadir un comando
		GuiTweaksConfig.CommandGroup group = cfg.activeGroup();
		List<GuiTweaksConfig.QuickCommand> list = group.commands;
		EditBox groupBox = new EditBox(font, x, 44, 170, 18, t("group_name"));
		groupBox.setMaxLength(20);
		groupBox.setHint(t("group_name"));
		groupBox.setValue(group.name);
		groupBox.setResponder(v -> group.name = v);
		addRenderableWidget(groupBox);

		Button delete = Button.builder(t("group_delete"), b -> confirmDeleteGroup()).bounds(x + 174, 44, 92, 18).build();
		delete.active = cfg.commandGroups.size() > 1;
		addRenderableWidget(delete);

		Button add = Button.builder(t("add"), b -> {
			list.add(new GuiTweaksConfig.QuickCommand("", "", true));
			rebuildWidgets();
		}).bounds(x + 270, 44, 70, 18).build();
		add.active = list.size() < GuiTweaksConfig.MAX_QUICK_COMMANDS;
		addRenderableWidget(add);

		// Filas de comandos del grupo activo
		int y = 78;
		for (int i = 0; i < list.size(); i++) {
			GuiTweaksConfig.QuickCommand q = list.get(i);
			int ry = y + i * ROW;

			EditBox label = new EditBox(font, x, ry, 80, 20, t("label"));
			label.setMaxLength(28);
			label.setHint(t("label"));
			label.setValue(q.label);
			label.setResponder(v -> q.label = v);
			addRenderableWidget(label);

			EditBox command = new EditBox(font, x + 84, ry, 130, 20, t("command"));
			command.setMaxLength(64);
			command.setHint(t("command"));
			command.setValue(q.command);
			command.setResponder(v -> {
				q.command = v;
				if (command == activeBox) refreshChips();
			});
			addRenderableWidget(command);
			commandBoxes.add(command);

			addRenderableWidget(Button.builder(modeLabel(q), b -> {
				q.send = !q.send;
				b.setMessage(modeLabel(q));
			}).bounds(x + 218, ry, 80, 20).build());

			final int index = i;
			addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
				list.remove(index);
				rebuildWidgets();
			}).bounds(x + 302, ry, 20, 20).build());
		}

		// Sugerencias: un pequeño grupo de botones fijo, se rellena según el cuadro activo
		int chipY = height - 46;
		int chipW = 52;
		for (int i = 0; i < CHIPS; i++) {
			final int ci = i;
			chips[i] = Button.builder(Component.empty(), b -> {
				if (activeBox != null && chipValues[ci] != null) activeBox.setValue(chipValues[ci]);
			}).bounds(width / 2 - (CHIPS * (chipW + 2)) / 2 + i * (chipW + 2), chipY, chipW, 18).build();
			chips[i].visible = false;
			addRenderableWidget(chips[i]);
		}

		addRenderableWidget(Button.builder(t("done"), b -> onClose())
			.bounds(width / 2 - 100, height - 24, 200, 20).build());
	}

	/** Borra el grupo activo (y sus comandos) tras preguntar. */
	private void confirmDeleteGroup() {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		if (cfg.commandGroups.size() <= 1) return;
		minecraft.setScreenAndShow(new ConfirmScreen(yes -> {
			if (yes) {
				cfg.commandGroups.remove(cfg.activeCommandGroup);
				cfg.activeCommandGroup = Math.max(0, Math.min(cfg.commandGroups.size() - 1, cfg.activeCommandGroup));
				cfg.save();
			}
			minecraft.setScreenAndShow(this);
		}, t("group_delete.title"), t("group_delete.message")));
	}

	private static Component modeLabel(GuiTweaksConfig.QuickCommand q) {
		return t(q.send ? "mode.send" : "mode.write");
	}

	/** Comandos que el servidor ofrece (si hay conexión) o una lista común. */
	private List<String> knownCommands() {
		List<String> names = new ArrayList<>();
		if (minecraft != null && minecraft.getConnection() != null) {
			for (CommandNode<?> node : minecraft.getConnection().getCommands().getRoot().getChildren()) {
				names.add(node.getName());
			}
			names.sort(String::compareTo);
		}
		if (names.isEmpty()) names.addAll(List.of(COMMON));
		return names;
	}

	private void refreshChips() {
		for (int i = 0; i < CHIPS; i++) {
			chipValues[i] = null;
			chips[i].visible = false;
		}
		if (activeBox == null) return;

		String typed = ChatQuickCommands.clean(activeBox.getValue()).toLowerCase(Locale.ROOT);
		if (typed.contains(" ")) return; // ya está escribiendo argumentos

		List<String> all = knownCommands();
		List<String> matches = new ArrayList<>();
		for (String n : all) if (n.toLowerCase(Locale.ROOT).startsWith(typed) && !n.equalsIgnoreCase(typed)) matches.add(n);
		if (!typed.isEmpty()) {
			for (String n : all) if (matches.size() < CHIPS && !matches.contains(n) && n.toLowerCase(Locale.ROOT).contains(typed) && !n.equalsIgnoreCase(typed)) matches.add(n);
		}
		for (int i = 0; i < CHIPS && i < matches.size(); i++) {
			chipValues[i] = matches.get(i);
			chips[i].setMessage(Component.literal(matches.get(i)));
			chips[i].visible = true;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		// Detecta qué cuadro de comando tiene el foco para ofrecer sugerencias
		EditBox focused = null;
		for (EditBox b : commandBoxes) if (b.isFocused()) focused = b;
		if (focused != null && (focused != activeBox || !focused.getValue().equals(lastActiveValue))) {
			activeBox = focused;
			lastActiveValue = focused.getValue();
			refreshChips();
		}

		super.extractRenderState(g, mouseX, mouseY, delta);
		int accent = GuiTweaksConfig.get().accentColor();
		g.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
		g.text(font, t("col.label"), width / 2 - 170, 67, accent);
		g.text(font, t("col.command"), width / 2 - 86, 67, accent);
		g.text(font, t("col.mode"), width / 2 + 48, 67, accent);

		boolean anyChip = false;
		for (Button c : chips) anyChip |= c.visible;
		g.centeredText(font, anyChip ? t("suggestions") : t("hint"), width / 2, height - 58, anyChip ? accent : 0xFF9A9A9A);
	}

	@Override
	public void onClose() {
		GuiTweaksConfig.get().save();
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void removed() {
		GuiTweaksConfig.get().save();
	}
}
