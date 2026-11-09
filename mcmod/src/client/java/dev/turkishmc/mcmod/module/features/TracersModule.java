package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;

public final class TracersModule extends Module {
	public TracersModule() {
		super("tracers", "Tracers", "Oyuncu ve moblara kameradan renkli çizgi çizer.", Category.VISUAL);
		addColorSetting("color", "Çizgi rengi", 0x59D5C5);
		addIntegerSetting("range", "Menzil", 96, 16, 256, 16);
	}
}
