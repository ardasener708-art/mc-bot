package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.render.HudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class KeystrokesModule extends Module {
	public KeystrokesModule() {
		super("keystrokes", "Keystrokes", "WASD ve fare tıklamalarını ekranda gösterir.", Category.HUD);
	}

	@Override
	protected void drawHud(Minecraft client, GuiGraphics graphics) {
		HudOverlay.drawKeystrokes(graphics, client);
	}
}
