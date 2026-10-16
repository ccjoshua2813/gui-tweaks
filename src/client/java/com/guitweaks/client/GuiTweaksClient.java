package com.guitweaks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class GuiTweaksClient implements ClientModInitializer {
	public static final String MOD_ID = "guitweaks";

	private static KeyMapping nextViewKey;

	/** Carga la siguiente ranura de vista ocupada (la anterior queda guardada para «Deshacer carga»). */
	private static void switchToNextView(Minecraft mc) {
		GuiTweaksConfig cfg = GuiTweaksConfig.get();
		int next = cfg.nextUsedSlot();
		if (next < 0) {
			if (mc.player != null) mc.player.sendSystemMessage(Component.translatable("guitweaks.view.none"));
			return;
		}
		cfg.loadSlot(next);
		cfg.save();
		String name = cfg.viewSlots.get(next).name;
		if (name == null || name.isBlank()) name = Component.translatable("guitweaks.slots.slot", next + 1).getString();
		if (mc.player != null) mc.player.sendSystemMessage(Component.translatable("guitweaks.view.switched", name));
	}

	@Override
	public void onInitializeClient() {
		GuiTweaksConfig.load();
		ChatQuickCommands.register();
		PauseConfigButton.register();

		// Tecla (sin asignar por defecto) para pasar a la siguiente vista guardada
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		// Constructor de tecla de teclado que existe igual en la 26.2 y la 26.3 (el tipo de tecla se llama distinto)
		nextViewKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.guitweaks.next_view", GLFW.GLFW_KEY_UNKNOWN, category));
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (nextViewKey.consumeClick()) switchToNextView(mc);
		});
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(LocatorSuppressor::tick);
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.BEFORE_GIZMOS.register(ArrowTrail::render);

		// Las barras vanilla se ocultan según la config en vivo (sin reiniciar).
		for (Identifier id : new Identifier[] {
			VanillaHudElements.HEALTH_BAR, VanillaHudElements.FOOD_BAR, VanillaHudElements.ARMOR_BAR,
			VanillaHudElements.INFO_BAR, VanillaHudElements.EXPERIENCE_LEVEL, VanillaHudElements.AIR_BAR,
			VanillaHudElements.MOUNT_HEALTH
		}) {
			HudElementRegistry.replaceElement(id, original -> (graphics, tickCounter) -> {
				if (!GuiTweaksConfig.get().hideVanillaBars) original.extractRenderState(graphics, tickCounter);
			});
		}

		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "status_panel"),
			(graphics, tickCounter) -> StatusPanel.render(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "info_panel"),
			(graphics, tickCounter) -> InfoPanel.render(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "effects_panel"),
			(graphics, tickCounter) -> EffectsPanel.render(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "jump_bar"),
			(graphics, tickCounter) -> ExtraBars.renderJump(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "mount_health"),
			(graphics, tickCounter) -> ExtraBars.renderMount(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "air_bar"),
			(graphics, tickCounter) -> ExtraBars.renderAir(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "equipment_panel"),
			(graphics, tickCounter) -> EquipmentPanel.render(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "mob_health"),
			(graphics, tickCounter) -> MobHealthPanel.render(graphics));
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "item_popup"),
			(graphics, tickCounter) -> ItemPopup.render(graphics));

		// La barra de items vanilla se sustituye por la del mod solo si el estilo está activado
		HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, original -> (graphics, tickCounter) -> {
			if (HotbarPanel.active(net.minecraft.client.Minecraft.getInstance())) HotbarPanel.render(graphics);
			else original.extractRenderState(graphics, tickCounter);
		});

		// Se ocultan los equivalentes vanilla cuando el propio está activo
		HudElementRegistry.replaceElement(VanillaHudElements.HELD_ITEM_TOOLTIP, original -> (graphics, tickCounter) -> {
			if (!GuiTweaksConfig.get().showItemPopup) original.extractRenderState(graphics, tickCounter);
		});
		HudElementRegistry.replaceElement(VanillaHudElements.MOB_EFFECTS, original -> (graphics, tickCounter) -> {
			if (!GuiTweaksConfig.get().showEffectsPanel) original.extractRenderState(graphics, tickCounter);
		});
	}
}
