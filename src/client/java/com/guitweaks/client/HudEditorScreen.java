package com.guitweaks.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Editor de posición de los paneles: los paneles se ven detrás con datos de muestra y se arrastran con el
 * ratón (o se afinan con las flechas). Se guarda el desplazamiento respecto a la posición automática.
 */
public class HudEditorScreen extends Screen {
	private static final int EDGE_KEEP = 12;     // lo mínimo que debe quedar a la vista de un panel
	private static final int SNAP = 3;           // atracción al centro de la pantalla

	private final Screen parent;
	private String dragging, selected;
	private double accX, accY;

	public HudEditorScreen(Screen parent) {
		super(Component.translatable("guitweaks.layout.title"));
		this.parent = parent;
	}

	private static Component t(String key) {
		return Component.translatable("guitweaks.layout." + key);
	}

	@Override
	protected void init() {
		PanelLayout.editing = true;
		addRenderableWidget(Button.builder(t("done"), b -> onClose()).bounds(width / 2 - 125, height - 28, 120, 20).build());
		addRenderableWidget(Button.builder(t("reset"), b -> {
			GuiTweaksConfig.get().panelOffsets.clear();
			selected = null;
		}).bounds(width / 2 + 5, height - 28, 120, 20).build());
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		g.fill(0, 0, width, height, 0x40000000);          // se ve el HUD detrás, apenas oscurecido
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		int accent = GuiTweaksConfig.get().accentColor();

		g.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
		g.centeredText(font, t(minecraft.player == null ? "empty" : "hint"), width / 2, 20, 0xFFC8C8C8);

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

	/** Mueve un panel, sin sacarlo de la pantalla y con atracción al centro si se arrastra con el ratón. */
	private void move(String id, int ix, int iy, boolean snap) {
		PanelLayout.Rect r = PanelLayout.get(id);
		if (r == null) return;
		// No dejar que salga del todo de la pantalla
		int nx = Math.max(EDGE_KEEP - r.w(), Math.min(width - EDGE_KEEP, r.x() + ix));
		int ny = Math.max(EDGE_KEEP - r.h(), Math.min(height - EDGE_KEEP, r.y() + iy));
		ix = nx - r.x();
		iy = ny - r.y();
		if (snap) {
			int centerDiff = width / 2 - (nx + r.w() / 2);
			if (Math.abs(centerDiff) <= SNAP) ix += centerDiff;
		}
		PanelLayout.setOffset(id, PanelLayout.dx(id) + ix, PanelLayout.dy(id) + iy);
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
