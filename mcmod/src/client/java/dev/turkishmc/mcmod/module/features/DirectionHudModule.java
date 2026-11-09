package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.render.HudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class DirectionHudModule extends Module {
	public DirectionHudModule() {
		super("direction_hud", "Yön HUD", "Pusula yönünü ekranın üst kısmında gösterir.", Category.HUD);
		addColorSetting("color", "Pusula rengi", 0x59D5C5);
	}

	@Override
	protected void drawHud(Minecraft client, GuiGraphics graphics) {
		HudOverlay.drawDirection(graphics, client, getSetting("color").getInt());
	}
}
