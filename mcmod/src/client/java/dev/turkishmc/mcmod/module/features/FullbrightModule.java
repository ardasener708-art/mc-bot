package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import net.minecraft.client.Minecraft;

public final class FullbrightModule extends Module {
	private double previousGamma = 1.0;

	public FullbrightModule() {
		super("fullbright", "Fullbright", "Karanlık alanları tam aydınlık gösterir.", Category.VISUAL);
	}

	@Override
	protected void onToggled(boolean enabled) {
		Minecraft client = Minecraft.getInstance();
		if (enabled) {
			previousGamma = client.options.gamma().get();
			client.options.gamma().set(100.0);
		} else {
			client.options.gamma().set(previousGamma);
		}
	}

	@Override
	protected void tick(Minecraft client) {
		if (client.options.gamma().get() < 100.0) {
			client.options.gamma().set(100.0);
		}
	}
}
