package dev.turkishmc.mcmod;

import com.mojang.blaze3d.platform.InputConstants;
import dev.turkishmc.mcmod.config.ModConfig;
import dev.turkishmc.mcmod.gui.ModuleScreen;
import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.module.EspModule;
import dev.turkishmc.mcmod.module.ModuleManager;
import dev.turkishmc.mcmod.render.EspRenderer;
import dev.turkishmc.mcmod.render.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public final class McModClient implements ClientModInitializer {
	public static final ModuleManager MODULES = new ModuleManager();
	public static final String ESP_MODULE_NAME = "esp";

	private static KeyMapping openMenuKey;
	private static final Map<String, Boolean> MODULE_KEY_STATES = new HashMap<>();

	@Override
	public void onInitializeClient() {
		MODULES.register(new EspModule());
		MODULES.discover("dev.turkishmc.mcmod.module.features");
		ModConfig.load(MODULES);

		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.mcmod.open_menu",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(McModClient::onEndClientTick);
		HudOverlay.register();
		WorldRenderEvents.END_EXTRACTION.register(EspRenderer::extract);
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(EspRenderer::render);
	}

	private static void onEndClientTick(Minecraft client) {
		while (openMenuKey.consumeClick()) {
			if (client.screen instanceof ModuleScreen) {
				client.setScreen(null);
			} else {
				client.setScreen(new ModuleScreen(MODULES));
			}
		}

		for (Module module : MODULES.getModules()) {
			int keyCode = module.getKeyCode();
			boolean pressed = keyCode >= 0
					&& GLFW.glfwGetKey(client.getWindow().handle(), keyCode) == GLFW.GLFW_PRESS;
			boolean wasPressed = MODULE_KEY_STATES.getOrDefault(module.getId(), false);
			boolean justPressed = pressed && !wasPressed;
			if (keyCode >= 0) {
				module.onKeybind(pressed, justPressed, client);
				MODULE_KEY_STATES.put(module.getId(), pressed);
				if (justPressed) {
					ModConfig.save(MODULES);
				}
			}
			module.onClientTick(client);
		}
	}
}
