package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import net.minecraft.client.Minecraft;

public final class AutoSprintModule extends Module {
	public AutoSprintModule() {
		super("auto_sprint", "Auto Sprint", "Uygun olduğunda otomatik koşar.", Category.UTILITY);
	}

	@Override
	protected void tick(Minecraft client) {
		if (client.player != null && !client.player.isShiftKeyDown() && !client.player.isPassenger()) {
			client.player.setSprinting(true);
		}
	}
}
