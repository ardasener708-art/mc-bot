package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.render.HudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class EffectHudModule extends Module {
	public EffectHudModule() {
		super("effect_hud", "Efekt HUD", "Aktif iksir efektlerini ve sürelerini listeler.", Category.HUD);
		addColorSetting("color", "Vurgu rengi", 0x59D5C5);
	}

	@Override
	protected void drawHud(Minecraft client, GuiGraphics graphics) {
		HudOverlay.drawEffects(graphics, client, getSetting("color").getInt());
	}
}
