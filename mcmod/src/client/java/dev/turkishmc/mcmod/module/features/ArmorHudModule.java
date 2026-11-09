package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.render.HudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class ArmorHudModule extends Module {
	public ArmorHudModule() {
		super("armor_hud", "Armor HUD", "Zırh ve eldeki eşyaların dayanıklılığını gösterir.", Category.HUD);
	}

	@Override
	protected void drawHud(Minecraft client, GuiGraphics graphics) {
		HudOverlay.drawArmor(graphics, client);
	}
}
