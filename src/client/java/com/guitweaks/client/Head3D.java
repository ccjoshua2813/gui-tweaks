package com.guitweaks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * Cabeza del jugador en 3D, dibujada con el mismo renderizador que la pantalla del inventario
 * (modelo real con skin, capa del sombrero y casco). Se recorta para mostrar solo la cabeza y gira
 * con la cámara: al mover el ratón se vuelve hacia ese lado y recupera su posición, y se inclina
 * según hacia dónde miras arriba o abajo.
 */
public final class Head3D {
	private static final float HEAD_BLOCKS = 0.5f;      // lado de la cabeza en bloques
	private static final float MAX_TURN = 28f;          // giro máximo, en grados
	private static final float MAX_TILT = 24f;

	private static boolean init;
	private static float lastYaw;
	private static long lastTime;
	private static float turn;                          // grados, positivo = hacia la derecha de la pantalla

	private Head3D() {}

	/** Actualiza el giro según el movimiento de la cámara. Se llama una vez por fotograma. */
	private static void update(LocalPlayer p) {
		long now = System.nanoTime();
		float yaw = p.getYRot();
		if (!init) {
			init = true;
			lastYaw = yaw;
			lastTime = now;
		}
		float dt = Math.min(0.1f, (now - lastTime) / 1_000_000_000f);
		lastTime = now;
		float delta = Mth.wrapDegrees(yaw - lastYaw);
		lastYaw = yaw;
		turn += delta * 1.1f;                                  // girar a la derecha lleva la cabeza a la derecha
		turn *= (float) Math.exp(-dt * 5.0);                   // y vuelve poco a poco al centro
		turn = Math.max(-MAX_TURN, Math.min(MAX_TURN, turn));
	}

	/**
	 * @param ax,ay esquina de la caja de la cabeza, en píxeles de pantalla (sin la escala del panel aplicada)
	 * @param size  lado de la caja, en píxeles de pantalla
	 */
	public static void draw(GuiGraphicsExtractor g, LocalPlayer p, int ax, int ay, int size, GuiTweaksConfig cfg) {
		update(p);

		// El juego gira el modelo 20 * atan(dx / 40) grados, con el atan en radianes: se invierte
		// para que el giro pedido en grados salga exacto.
		float tiltDeg = Math.max(-MAX_TILT, Math.min(MAX_TILT, -p.getXRot() * 0.35f));
		float cx = ax + size / 2f, cy = ay + size / 2f;
		float mouseX = cx + 40f * (float) Math.tan(turn / 20.0);          // positivo = mira a la derecha
		float mouseY = cy - 40f * (float) Math.tan(tiltDeg / 20.0);       // por encima del centro = mira arriba

		// Se centra la cabeza (no el cuerpo) en la caja: el juego centra la mitad de la altura más este desvío
		float headCenter = p.getEyeHeight() - 0.07f;
		float yOffset = headCenter - p.getBbHeight() / 2f + cfg.head3dHeight / 100f;
		int scale = Math.round(size / HEAD_BLOCKS * 0.92f * cfg.head3dZoom / 100f);

		g.pose().pushMatrix();
		g.pose().identity();                                    // coordenadas de pantalla, sin la escala del panel
		g.enableScissor(ax, ay, ax + size, ay + size);
		InventoryScreen.extractEntityInInventoryFollowsMouse(g, ax, ay, ax + size, ay + size, scale, yOffset, mouseX, mouseY, p);
		g.disableScissor();
		g.pose().popMatrix();
	}
}
