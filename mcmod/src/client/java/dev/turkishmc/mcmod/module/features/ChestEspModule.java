package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;

public final class ChestEspModule extends Module {
	public ChestEspModule() {
		super("chest_esp", "Chest ESP", "Yakındaki sandık, varil ve shulkerları vurgular.", Category.VISUAL);
		addColorSetting("color", "Kutu rengi", 0xFFAA33);
		addIntegerSetting("range", "Menzil", 16, 8, 32, 8);
	}
}
