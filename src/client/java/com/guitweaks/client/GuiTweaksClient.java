package com.guitweaks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class GuiTweaksClient implements ClientModInitializer {
	public static final String MOD_ID = "guitweaks";

	@Override
	public void onInitializeClient() {
		GuiTweaksConfig.load();
		ChatQuickCommands.register();
		PauseConfigButton.register();
		net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.BEFORE_GIZMOS.register(ArrowTrail::render);

		// Las barras vanilla se ocultan según la config en vivo (sin reiniciar).
		for (Identifier id : new Identifier[] {
			VanillaHudElements.HEALTH_BAR, VanillaHudElements.FOOD_BAR, VanillaHudElements.ARMOR_BAR,
			VanillaHudElements.INFO_BAR, VanillaHudElements.EXPERIENCE_LEVEL
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
