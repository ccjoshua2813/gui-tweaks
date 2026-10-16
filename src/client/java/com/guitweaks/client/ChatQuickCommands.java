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
 * Si hay más de un grupo de comandos, arriba va un botón largo que alterna entre ellos.
 */
public final class ChatQuickCommands {
	private static final int PAD = 4;
	private static final int BUTTON_H = 13;
	private static final int GAP = 3;
	private static final int TOP = 4;
	private static final int MIN_W = 38;
	private static final int MAX_W = 58;   // más compacto: los nombres largos se deslizan
	private static final int MIN_HEADER_W = 70;
	private static final int MAX_HEADER_W = 150;
	private static final float TEXT_SCALE = 0.8f;

	private ChatQuickCommands() {}

	/** Botones por fila: hasta 4 en una fila; con más, filas de 3 (o de 4 a partir de 7). */
	static int perRow(int count) {
		return count <= 4 ? Math.max(1, count) : count <= 6 ? 3 : 4;
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			GuiTweaksConfig cfg = GuiTweaksConfig.get();
			if (!(screen instanceof ChatScreen) || !cfg.showQuickCommands) return;

			Panel panel = new Panel(client, scaledWidth, scaledHeight);
			panel.layout();
			if (!panel.show && !panel.hasHeader()) return;
			panel.addTo(screen);

			// Caja del panel: se dibuja bajo los botones
			ScreenEvents.afterBackground(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> panel.drawBox(graphics));
			// Aspecto de cada botón, encima de los botones invisibles
			ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> panel.drawButtons(graphics));
		});
	}

	/**
	 * Estado del panel en un chat abierto. Los botones se crean una sola vez y, al cambiar de grupo,
	 * solo se reubican, se renombran o se ocultan (así no se toca la lista de widgets de la pantalla).
	 */
	private static final class Panel {
		private final Minecraft client;
		private final Font font;
		private final int sw, sh;
		private final Button[] buttons = new Button[GuiTweaksConfig.MAX_QUICK_COMMANDS];
		private final String[] labels = new String[GuiTweaksConfig.MAX_QUICK_COMMANDS];
		private final String[] commands = new String[GuiTweaksConfig.MAX_QUICK_COMMANDS];
		private final boolean[] send = new boolean[GuiTweaksConfig.MAX_QUICK_COMMANDS];
		private final Button header;
		private String headerLabel = "";
		private int x, y, w, h;
		boolean show;

		Panel(Minecraft client, int sw, int sh) {
			this.client = client;
			this.font = client.font;
			this.sw = sw;
			this.sh = sh;
			for (int i = 0; i < buttons.length; i++) {
				final int idx = i;
				Button b = Button.builder(Component.empty(), btn -> run(client, commands[idx], send[idx])).bounds(0, 0, MIN_W, BUTTON_H).build();
				b.setAlpha(0f);                 // el aspecto lo pone el mod
				b.visible = false;
				buttons[i] = b;
			}
			if (GuiTweaksConfig.get().commandGroups.size() > 1) {
				header = Button.builder(Component.empty(), btn -> nextGroup())
					.bounds(0, 0, MIN_HEADER_W, BUTTON_H)
					.tooltip(Tooltip.create(Component.translatable("guitweaks.chat.group_tooltip")))
					.build();
				header.setAlpha(0f);
			} else {
				header = null;
			}
		}

		boolean hasHeader() {
			return header != null;
		}

		void addTo(net.minecraft.client.gui.screens.Screen screen) {
			for (Button b : buttons) Screens.getWidgets(screen).add(b);
			if (header != null) Screens.getWidgets(screen).add(header);
		}

		private void nextGroup() {
			GuiTweaksConfig cfg = GuiTweaksConfig.get();
			cfg.activeCommandGroup = (cfg.activeCommandGroup + 1) % cfg.commandGroups.size();
			cfg.save();
			layout();
		}

		/** Coloca el panel y los botones según el grupo activo. */
		void layout() {
			GuiTweaksConfig cfg = GuiTweaksConfig.get();
			List<GuiTweaksConfig.QuickCommand> entries = new ArrayList<>();
			for (GuiTweaksConfig.QuickCommand q : cfg.commands()) {
				if (!clean(q.command).isEmpty()) entries.add(q);
			}
			int count = entries.size();

			// Todos los botones del mismo ancho: el del nombre más largo (el texto se reduce si no cabe)
			int buttonW = MIN_W;
			for (GuiTweaksConfig.QuickCommand q : entries) {
				buttonW = Math.max(buttonW, Math.round(font.width(labelOf(q)) * TEXT_SCALE) + 10);
			}
			buttonW = Math.min(buttonW, MAX_W);

			int perRow = perRow(count);
			int rows = count == 0 ? 0 : (count + perRow - 1) / perRow;
			int cols = Math.min(perRow, count);
			int inner = count == 0 ? 0 : cols * buttonW + (cols - 1) * GAP;

			int headerH = 0;
			if (header != null) {
				headerLabel = QuickCommandsScreen.groupName(cfg.activeGroup(), cfg.activeCommandGroup);
				int want = Math.round(font.width(headerLabel) * TEXT_SCALE) + 14;
				inner = Math.max(inner, Math.max(MIN_HEADER_W, Math.min(MAX_HEADER_W, want)));
				headerH = BUTTON_H + (rows > 0 ? GAP : 0);
			}
			w = inner + PAD * 2;
			h = headerH + (rows > 0 ? rows * BUTTON_H + (rows - 1) * GAP : 0) + PAD * 2;
			x = (sw - w) / 2;
			y = cfg.chatPanelCenter ? Math.round(sh * 0.43f - h / 2f) : TOP;
			show = count > 0 || header != null;

			if (header != null) {
				header.setX(x + PAD);
				header.setY(y + PAD);
				header.setWidth(inner);
			}
			for (int i = 0; i < buttons.length; i++) {
				Button b = buttons[i];
				if (i >= count) {
					b.visible = false;
					commands[i] = null;
					continue;
				}
				GuiTweaksConfig.QuickCommand q = entries.get(i);
				int row = i / perRow;
				int inRow = Math.min(perRow, count - row * perRow);
				int rowW = inRow * buttonW + (inRow - 1) * GAP;
				commands[i] = clean(q.command);
				send[i] = q.send;
				labels[i] = labelOf(q);
				b.visible = true;
				b.setX((sw - rowW) / 2 + (i % perRow) * (buttonW + GAP));
				b.setY(y + PAD + headerH + row * (BUTTON_H + GAP));
				b.setWidth(buttonW);
				b.setMessage(Component.literal(labels[i]));
				b.setTooltip(Tooltip.create(Component.literal("/" + commands[i] + (q.send ? "" : " ..."))));
			}
		}

		void drawBox(GuiGraphicsExtractor g) {
			if (show) Draw.card(g, x, y, w, h, GuiTweaksConfig.get().accentColor(), GuiTweaksConfig.get().opacityPercent);
		}

		void drawButtons(GuiGraphicsExtractor g) {
			GuiTweaksConfig cfg = GuiTweaksConfig.get();
			if (header != null) draw(g, font, header, headerLabel, cfg, true);
			for (int i = 0; i < buttons.length; i++) {
				if (buttons[i].visible) draw(g, font, buttons[i], labels[i], cfg, false);
			}
		}
	}

	private static void draw(GuiGraphicsExtractor g, Font font, Button b, String label, GuiTweaksConfig cfg, boolean isHeader) {
		int accent = cfg.accentColor();
		boolean hover = b.isHoveredOrFocused();
		int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();

		// Fondo y contorno; con opacidad 0 solo se ve el resalte al pasar el ratón
		Draw.buttonBox(g, x, y, w, h, hover, accent, cfg.opacityPercent);

		// Texto centrado; si no cabe, se desliza de un extremo al otro con una pausa en cada uno
		float tw = font.width(label) * TEXT_SCALE;
		float avail = w - 8;
		float offset = 0f, textX = x + (w - tw) / 2f;
		if (tw > avail) {
			textX = x + 4;
			offset = marquee(tw - avail);
		}
		int color = hover ? 0xFFFFFFFF : isHeader ? Draw.withAlpha(accent, 255) : 0xFFE6E6E6;
		g.enableScissor(x + 4, y, x + w - 4, y + h);
		g.pose().pushMatrix();
		g.pose().translate(textX - offset, y + (h - 9 * TEXT_SCALE) / 2f + 0.5f);
		g.pose().scale(TEXT_SCALE, TEXT_SCALE);
		g.text(font, label, 0, 0, color, true);
		g.pose().popMatrix();
		g.disableScissor();
	}

	/** Desplazamiento (0 a {@code range} píxeles) que va y vuelve, con una pausa en cada extremo. */
	private static float marquee(float range) {
		double speed = 18.0, pause = 0.8;                       // píxeles por segundo, segundos
		double travel = range / speed, cycle = 2 * (travel + pause);
		double t = (System.currentTimeMillis() / 1000.0) % cycle;
		if (t < pause) return 0f;
		if (t < pause + travel) return (float) ((t - pause) * speed);
		if (t < 2 * pause + travel) return range;
		return (float) (range - (t - 2 * pause - travel) * speed);
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
		if (command == null || command.isEmpty()) return;
		if (send) {
			if (mc.getConnection() != null) mc.getConnection().sendCommand(command);
		} else {
			// Deja el comando escrito para completar los argumentos
			mc.setScreenAndShow(new ChatScreen("/" + command + " ", false));
		}
	}
}
