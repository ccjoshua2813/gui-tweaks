package com.guitweaks.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Dibuja sprites del GUI sin nombrar la clase {@code RenderPipeline}, que en la 26.3 cambió de paquete
 * ({@code com.mojang.blaze3d.pipeline} pasó a {@code com.mojang.renderpearl.api.pipeline}). Los métodos
 * se buscan al primer uso, así que el mismo jar sirve en la 26.2 y en la 26.3. Si algo no se encuentra,
 * el icono simplemente no se dibuja (el juego no se rompe) y se avisa una vez en el log.
 */
public final class Sprites {
	private static final String SPRITE = "blitSprite";

	private static MethodHandle plain, tinted, part;
	private static boolean looked, broken;

	private Sprites() {}

	/** Sprite del GUI a (x, y) con tamaño (w, h). */
	public static void blit(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int w, int h) {
		MethodHandle mh = handle(0);
		if (mh == null) return;
		try {
			mh.invokeExact(g, sprite, x, y, w, h);
		} catch (Throwable t) {
			fail(t);
		}
	}

	/** Igual que {@link #blit}, con un color (ARGB) que tiñe el sprite. */
	public static void blitTinted(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int w, int h, int color) {
		MethodHandle mh = handle(1);
		if (mh == null) return;
		try {
			mh.invokeExact(g, sprite, x, y, w, h, color);
		} catch (Throwable t) {
			fail(t);
		}
	}

	/** Un trozo del sprite: (anchoTextura, altoTextura, u, v, x, y, w, h), como el método del juego. */
	public static void blitPart(GuiGraphicsExtractor g, Identifier sprite, int texW, int texH, int u, int v, int x, int y, int w, int h) {
		MethodHandle mh = handle(2);
		if (mh == null) return;
		try {
			mh.invokeExact(g, sprite, texW, texH, u, v, x, y, w, h);
		} catch (Throwable t) {
			fail(t);
		}
	}

	private static MethodHandle handle(int which) {
		if (!looked) lookUp();
		return broken ? null : which == 0 ? plain : which == 1 ? tinted : part;
	}

	private static void lookUp() {
		looked = true;
		try {
			Object pipeline = RenderPipelines.class.getField("GUI_TEXTURED").get(null);
			plain = find(pipeline, 4);
			tinted = find(pipeline, 5);
			part = find(pipeline, 8);
		} catch (Throwable t) {
			fail(t);
		}
		if (plain == null || tinted == null || part == null) broken = true;
	}

	/** Busca blitSprite(pipeline, Identifier, int × n) y devuelve un manejador con el pipeline ya puesto. */
	private static MethodHandle find(Object pipeline, int ints) throws ReflectiveOperationException {
		for (Method m : GuiGraphicsExtractor.class.getMethods()) {
			Class<?>[] p = m.getParameterTypes();
			if (!m.getName().equals(SPRITE) || p.length != 2 + ints || p[1] != Identifier.class) continue;
			if (!p[0].isInstance(pipeline)) continue;
			boolean allInts = true;
			for (int i = 2; i < p.length; i++) allInts &= p[i] == int.class;
			if (!allInts) continue;
			return MethodHandles.insertArguments(MethodHandles.publicLookup().unreflect(m), 1, pipeline);
		}
		return null;
	}

	private static void fail(Throwable t) {
		if (!broken) System.err.println("[guitweaks] No se pudieron dibujar los iconos del GUI: " + t);
		broken = true;
	}
}
