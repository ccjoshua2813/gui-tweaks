package com.guitweaks.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Panel pequeño y centrado de comandos rápidos que aparece al abrir el chat (T o /). */
public final class ChatQuickCommands {
	private static final int PER_ROW = 4;
	private static final int PAD = 3;
	private static final int BUTTON_H = 14;
	private static final int GAP = 2;
	private static final int TOP = 4;
	private static final int MIN_W = 36;
	private static final int MAX_W = 84;

	private ChatQuickCommands() {}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			GuiTweaksConfig cfg = GuiTweaksConfig.get();
			if (!(screen instanceof ChatScreen) || !cfg.showQuickCommands) return;

			List<GuiTweaksConfig.QuickCommand> entries = new ArrayList<>();
			for (GuiTweaksConfig.QuickCommand q : cfg.quickCommands) {
				if (!clean(q.command).isEmpty()) entries.add(q);
			}
			if (entries.isEmpty()) return;

			Font font = client.font;

			// Todos los botones del mismo ancho: el del nombre más largo
			int buttonW = MIN_W;
			for (GuiTweaksConfig.QuickCommand q : entries) buttonW = Math.max(buttonW, font.width(labelOf(q)) + 10);
			buttonW = Math.min(buttonW, MAX_W);

			int rows = (entries.size() + PER_ROW - 1) / PER_ROW;
			int cols = Math.min(PER_ROW, entries.size());
			int w = cols * buttonW + (cols - 1) * GAP + PAD * 2;
			int h = rows * BUTTON_H + (rows - 1) * GAP + PAD * 2;
			int x = (scaledWidth - w) / 2;

			// Fondo de la tarjeta: se dibuja bajo los botones
			ScreenEvents.afterBackground(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
				Draw.card(graphics, x, TOP, w, h, cfg.accentColor(), cfg.opacityPercent));

			// Cada fila se centra por separado: se reparte desde el centro hacia los lados
			for (int i = 0; i < entries.size(); i++) {
				int row = i / PER_ROW;
				int inRow = Math.min(PER_ROW, entries.size() - row * PER_ROW);
				int rowW = inRow * buttonW + (inRow - 1) * GAP;
				int bx = (scaledWidth - rowW) / 2 + (i % PER_ROW) * (buttonW + GAP);
				int by = TOP + PAD + row * (BUTTON_H + GAP);

				GuiTweaksConfig.QuickCommand q = entries.get(i);
				String command = clean(q.command);
				Button button = Button.builder(Component.literal(labelOf(q)), b -> run(client, command, q.send))
					.bounds(bx, by, buttonW, BUTTON_H)
					.tooltip(Tooltip.create(Component.literal("/" + command + (q.send ? "" : " …"))))
					.build();
				Screens.getWidgets(screen).add(button);
			}
		});
	}

	private static String labelOf(GuiTweaksConfig.QuickCommand q) {
		return q.label == null || q.label.isBlank() ? clean(q.command) : q.label;
	}

	public static String clean(String command) {
		if (command == null) return "";
		String c = command.trim();
		while (c.startsWith("/")) c = c.substring(1);
		return c.trim();
	}

	private static void run(Minecraft mc, String command, boolean send) {
		if (send) {
			if (mc.getConnection() != null) mc.getConnection().sendCommand(command);
		} else {
			// Deja el comando escrito para completar los argumentos
			mc.setScreenAndShow(new ChatScreen("/" + command + " ", false));
		}
	}
}
