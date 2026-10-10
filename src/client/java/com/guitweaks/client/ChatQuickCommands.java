package com.guitweaks.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel de comandos rápidos que aparece al abrir el chat (T o /): una caja con el estilo del mod
 * y los botones en cuadrícula de 3 o 4 por fila. Los botones son los del juego (para el clic y el
 * foco) pero invisibles; el aspecto lo dibuja el mod con el color de acento y la opacidad elegidos.
 */
public final class ChatQuickCommands {
	private static final int PAD = 4;
	private static final int BUTTON_H = 13;
	private static final int GAP = 3;
	private static final int TOP = 4;
	private static final int MIN_W = 38;
	private static final int MAX_W = 84;
	private static final float TEXT_SCALE = 0.8f;

	private ChatQuickCommands() {}

	/** Botones por fila: hasta 4 en una fila; con más, filas de 3 (o de 4 a partir de 7). */
	static int perRow(int count) {
		return count <= 4 ? Math.max(1, count) : count <= 6 ? 3 : 4;
	}

	private record Cell(Button button, String label) {}

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

			// Todos los botones del mismo ancho: el del nombre más largo (el texto se reduce si no cabe)
			int buttonW = MIN_W;
			for (GuiTweaksConfig.QuickCommand q : entries) {
				buttonW = Math.max(buttonW, Math.round(font.width(labelOf(q)) * TEXT_SCALE) + 10);
			}
			buttonW = Math.min(buttonW, MAX_W);

			int perRow = perRow(entries.size());
			int rows = (entries.size() + perRow - 1) / perRow;
			int cols = Math.min(perRow, entries.size());
			int w = cols * buttonW + (cols - 1) * GAP + PAD * 2;
			int h = rows * BUTTON_H + (rows - 1) * GAP + PAD * 2;
			int x = (scaledWidth - w) / 2;
			int y = cfg.chatPanelCenter ? Math.round(scaledHeight * 0.43f - h / 2f) : TOP;

			// Caja del panel: se dibuja bajo los botones
			ScreenEvents.afterBackground(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
				Draw.card(graphics, x, y, w, h, cfg.accentColor(), cfg.opacityPercent));

			List<Cell> cells = new ArrayList<>();
			for (int i = 0; i < entries.size(); i++) {
				int row = i / perRow;
				int inRow = Math.min(perRow, entries.size() - row * perRow);
				int rowW = inRow * buttonW + (inRow - 1) * GAP;
				int bx = (scaledWidth - rowW) / 2 + (i % perRow) * (buttonW + GAP);
				int by = y + PAD + row * (BUTTON_H + GAP);

				GuiTweaksConfig.QuickCommand q = entries.get(i);
				String command = clean(q.command);
				Button button = Button.builder(Component.literal(labelOf(q)), b -> run(client, command, q.send))
					.bounds(bx, by, buttonW, BUTTON_H)
					.tooltip(Tooltip.create(Component.literal("/" + command + (q.send ? "" : " ..."))))
					.build();
				button.setAlpha(0f);                 // el aspecto lo pone el mod
				Screens.getWidgets(screen).add(button);
				cells.add(new Cell(button, labelOf(q)));
			}

			// Aspecto de cada botón, encima de los botones invisibles
			ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> {
				for (Cell c : cells) draw(graphics, font, c, cfg);
			});
		});
	}

	private static void draw(GuiGraphicsExtractor g, Font font, Cell c, GuiTweaksConfig cfg) {
		Button b = c.button();
		int accent = cfg.accentColor();
		float op = Math.max(0, Math.min(100, cfg.opacityPercent)) / 100f;
		boolean hover = b.isHoveredOrFocused();
		int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();

		// Fondo y contorno; con opacidad 0 solo se ve el resalte al pasar el ratón
		Draw.rounded(g, x, y, w, h, hover ? Draw.withAlpha(accent, 255) : Draw.withAlpha(0x000000, Math.round(110 * op)));
		Draw.rounded(g, x + 1, y + 1, w - 2, h - 2,
			hover ? Draw.withAlpha(Draw.shade(accent, 0.35f), 255) : Draw.withAlpha(0x1C1F26, Math.round(255 * op)));

		// Texto centrado y reducido si no cabe
		float scale = Math.min(TEXT_SCALE, (w - 6) / (float) Math.max(1, font.width(c.label())));
		float tw = font.width(c.label()) * scale;
		g.pose().pushMatrix();
		g.pose().translate(x + (w - tw) / 2f, y + (h - 9 * scale) / 2f + 0.5f);
		g.pose().scale(scale, scale);
		g.text(font, c.label(), 0, 0, hover ? 0xFFFFFFFF : 0xFFE6E6E6, true);
		g.pose().popMatrix();
	}

	private static String labelOf(GuiTweaksConfig.QuickCommand q) {
		return q.label == null || q.label.isBlank() ? clean(q.command) : q.label;
	}

	public static String clean(String command) {
		if (command == null) return "";
		String c = command.trim();
		while (c.startsWith("/")) c = c.substring(1).trim();
		return c;
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
