package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.render.HudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class InfoHudModule extends Module {
	public InfoHudModule() {
		super("info_hud", "Bilgi HUD", "FPS, ping ve koordinatları ekranda gösterir.", Category.HUD);
	}

	@Override
	protected void drawHud(Minecraft client, GuiGraphics graphics) {
		HudOverlay.drawInfo(graphics, client);
	}
}
