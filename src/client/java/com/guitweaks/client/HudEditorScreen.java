package com.guitweaks.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor de posición de los paneles: los paneles se ven detrás con datos de muestra y se arrastran con el
 * ratón (o se afinan con las flechas). Se guarda el desplazamiento respecto a la posición automática.
 */
public class HudEditorScreen extends Screen {
	private static final int EDGE_KEEP = 12;     // lo mínimo que debe quedar a la vista de un panel
	private static final int SNAP = 3;           // atracción al centro de la pantalla
	private static final int GRID = 6;           // la cuadrícula guía divide la pantalla en 6x6

	private final Screen parent;
	/** Posiciones al abrir el editor: "Deshacer cambios" vuelve a ellas (y no toca nada más del mod). */
	private final Map<String, int[]> original = new HashMap<>();
	private Button undoButton;
	private String dragging, selected;
	private double accX, accY;

	public HudEditorScreen(Screen parent) {
		super(Component.translatable("guitweaks.layout.title"));
		this.parent = parent;
		for (Map.Entry<String, int[]> e : GuiTweaksConfig.get().panelOffsets.entrySet()) original.put(e.getKey(), e.getValue().clone());
	}

	private boolean changed() {
		Map<String, int[]> now = GuiTweaksConfig.get().panelOffsets;
		if (now.size() != original.size()) return true;
		for (Map.Entry<String, int[]> e : original.entrySet()) {
			if (!Arrays.equals(e.getValue(), now.get(e.getKey()))) return true;
		}
		return false;
	}

	private static Component t(String key) {
		return Component.translatable("guitweaks.layout." + key);
	}

	@Override
	protected void init() {
		PanelLayout.editing = true;
		// Los dos botones caben dentro de la zona reservada (182 px de ancho), para no tapar ningún panel
		int[] z = PanelLayout.reservedZone(width, height);
		addRenderableWidget(Button.builder(t("done"), b -> onClose()).bounds(z[0], height - 28, 60, 20).build());
		undoButton = addRenderableWidget(Button.builder(t("reset"), b -> askUndo()).bounds(z[0] + 62, height - 28, 120, 20).build());
	}

	/** Pide confirmación antes de volver a las posiciones con las que se abrió el editor. */
	private void askUndo() {
		if (!changed()) return;
		minecraft.setScreenAndShow(new ConfirmScreen(yes -> {
			if (yes) {
				GuiTweaksConfig cfg = GuiTweaksConfig.get();
				cfg.panelOffsets = new HashMap<>();
				for (Map.Entry<String, int[]> e : original.entrySet()) cfg.panelOffsets.put(e.getKey(), e.getValue().clone());
				cfg.save();
				selected = null;
			}
			minecraft.setScreenAndShow(this);
		}, t("reset.title"), t("reset.message")));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		g.fill(0, 0, width, height, 0x40000000);          // se ve el HUD detrás, apenas oscurecido
		drawOverlay(g);                                    // todo lo demás va en el fondo: los botones quedan encima
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		if (undoButton != null) undoButton.active = changed();     // nada que deshacer = botón apagado
		super.extractRenderState(g, mouseX, mouseY, delta);
	}

	/** Cuadrícula guía, zona reservada, paneles y ayuda. */
	private void drawOverlay(GuiGraphicsExtractor g) {
		int accent = GuiTweaksConfig.get().accentColor();

		// Cuadrícula guía 6x6 (las líneas del centro, algo más marcadas)
		for (int i = 1; i < GRID; i++) {
			int gx = width * i / GRID, gy = height * i / GRID;
			int c = i == GRID / 2 ? 0x55FFFFFF : 0x26FFFFFF;
			g.fill(gx, 0, gx + 1, height, c);
			g.fill(0, gy, width, gy + 1, c);
		}

		// Zona reservada: barra de items y barras de aire, salto y montura
		int[] z = PanelLayout.reservedZone(width, height);
		g.fill(z[0], z[1], z[0] + z[2], z[1] + z[3], 0x30FF4040);
		g.fill(z[0], z[1], z[0] + z[2], z[1] + 1, 0x90FF4040);
		g.fill(z[0], z[1], z[0] + 1, z[1] + z[3], 0x90FF4040);
		g.fill(z[0] + z[2] - 1, z[1], z[0] + z[2], z[1] + z[3], 0x90FF4040);
		// Ayuda y nombre de la zona, dentro de la zona (encima de los botones), para no pisar ningún panel
		g.centeredText(font, t(minecraft.player == null ? "empty" : "hint"), width / 2, z[1] + 3, 0xFFC8C8C8);
		g.centeredText(font, t("zone"), width / 2, z[1] + 13, 0xFFFF9090);

		for (PanelLayout.Rect r : PanelLayout.recent()) {
			boolean sel = r.id().equals(selected);
			g.fill(r.x(), r.y(), r.x() + r.w(), r.y() + r.h(), Draw.withAlpha(accent, sel ? 70 : 32));
			int c = Draw.withAlpha(accent, sel ? 255 : 170);
			g.fill(r.x(), r.y(), r.x() + r.w(), r.y() + 1, c);
			g.fill(r.x(), r.y() + r.h() - 1, r.x() + r.w(), r.y() + r.h(), c);
			g.fill(r.x(), r.y(), r.x() + 1, r.y() + r.h(), c);
			g.fill(r.x() + r.w() - 1, r.y(), r.x() + r.w(), r.y() + r.h(), c);
			// Nombre sobre el panel (dentro si no cabe arriba)
			Component label = Component.translatable("guitweaks.layout.panel." + r.id());
			int ly = r.y() >= 10 ? r.y() - 9 : r.y() + 2;
			g.text(font, label, r.x() + 1, ly, sel ? 0xFFFFFFFF : Draw.withAlpha(accent, 255), true);
		}
	}

	private PanelLayout.Rect hit(double mx, double my) {
		List<PanelLayout.Rect> list = PanelLayout.recent();
		for (int i = list.size() - 1; i >= 0; i--) {       // el último dibujado queda encima
			PanelLayout.Rect r = list.get(i);
			if (mx >= r.x() - 2 && mx <= r.x() + r.w() + 2 && my >= r.y() - 2 && my <= r.y() + r.h() + 2) return r;
		}
		return null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		if (event.button() != 0) return false;
		PanelLayout.Rect r = hit(event.x(), event.y());
		selected = r == null ? null : r.id();
		dragging = selected;
		accX = accY = 0;
		return r != null;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (dragging == null) return super.mouseDragged(event, dx, dy);
		accX += dx;
		accY += dy;
		int ix = (int) accX, iy = (int) accY;
		if (ix != 0 || iy != 0) {
			accX -= ix;
			accY -= iy;
			move(dragging, ix, iy, true);
		}
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		boolean was = dragging != null;
		dragging = null;
		if (was) GuiTweaksConfig.get().save();
		return was || super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (selected != null) {
			int step = (event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0 ? 5 : 1;
			int key = event.key();
			int ix = key == GLFW.GLFW_KEY_RIGHT ? step : key == GLFW.GLFW_KEY_LEFT ? -step : 0;
			int iy = key == GLFW.GLFW_KEY_DOWN ? step : key == GLFW.GLFW_KEY_UP ? -step : 0;
			if (ix != 0 || iy != 0) {
				move(selected, ix, iy, false);
				return true;
			}
		}
		return super.keyPressed(event);
	}

	private static long overlap(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
		int ox = Math.min(ax + aw, bx + bw) - Math.max(ax, bx);
		int oy = Math.min(ay + ah, by + bh) - Math.max(ay, by);
		return ox > 0 && oy > 0 ? (long) ox * oy : 0;
	}

	/** Área que un panel en (x, y) pisaría de los otros paneles y de la zona reservada. */
	private long overlapArea(String id, int x, int y, int w, int h) {
		int[] z = PanelLayout.reservedZone(width, height);
		long total = overlap(x, y, w, h, z[0], z[1], z[2], z[3]);
		for (PanelLayout.Rect o : PanelLayout.recent()) {
			if (!o.id().equals(id)) total += overlap(x, y, w, h, o.x(), o.y(), o.w(), o.h());
		}
		return total;
	}

	/**
	 * Mueve un panel sin sacarlo de la pantalla y sin que pise a otro panel ni la zona reservada. Si el
	 * movimiento chocaría, avanza lo que se pueda (primero en horizontal y luego en vertical, así se desliza
	 * a lo largo del obstáculo). Un panel que ya estaba pisando algo puede moverse mientras no pise más.
	 */
	private void move(String id, int ix, int iy, boolean snap) {
		PanelLayout.Rect r = PanelLayout.get(id);
		if (r == null) return;
		int w = r.w(), h = r.h();
		// No dejar que salga del todo de la pantalla
		int nx = Math.max(EDGE_KEEP - w, Math.min(width - EDGE_KEEP, r.x() + ix));
		int ny = Math.max(EDGE_KEEP - h, Math.min(height - EDGE_KEEP, r.y() + iy));
		ix = nx - r.x();
		iy = ny - r.y();

		long cur = overlapArea(id, r.x(), r.y(), w, h);
		int fx = 0;
		for (int k = ix; k != 0; k -= Integer.signum(k)) {
			long a = overlapArea(id, r.x() + k, r.y(), w, h);
			if (a == 0 || a <= cur) { fx = k; break; }
		}
		long afterX = overlapArea(id, r.x() + fx, r.y(), w, h);
		int fy = 0;
		for (int k = iy; k != 0; k -= Integer.signum(k)) {
			long a = overlapArea(id, r.x() + fx, r.y() + k, w, h);
			if (a == 0 || a <= afterX) { fy = k; break; }
		}
		if (snap) {
			int centerDiff = width / 2 - (r.x() + fx + w / 2);
			if (centerDiff != 0 && Math.abs(centerDiff) <= SNAP) {
				long base = overlapArea(id, r.x() + fx, r.y() + fy, w, h);
				long a = overlapArea(id, r.x() + fx + centerDiff, r.y() + fy, w, h);
				if (a == 0 || a <= base) fx += centerDiff;
			}
		}
		if (fx == 0 && fy == 0) return;
		PanelLayout.setOffset(id, PanelLayout.dx(id) + fx, PanelLayout.dy(id) + fy);
	}

	@Override
	public void onClose() {
		GuiTweaksConfig.get().save();
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void removed() {
		PanelLayout.editing = false;
		GuiTweaksConfig.get().save();
	}
}
