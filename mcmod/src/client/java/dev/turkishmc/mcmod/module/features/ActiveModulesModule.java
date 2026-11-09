package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.render.HudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class ActiveModulesModule extends Module {
	public ActiveModulesModule() {
		super("active_modules", "Aktif Modüller", "Açık modülleri ekranın köşesinde listeler.", Category.HUD);
	}

	@Override
	protected void drawHud(Minecraft client, GuiGraphics graphics) {
		HudOverlay.drawActiveModules(graphics);
	}
}
