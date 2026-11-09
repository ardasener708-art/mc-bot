package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;

public final class NametagsModule extends Module {
	public NametagsModule() {
		super("nametags", "Nametags", "Varlık adını, canını ve mesafeyi gösterir.", Category.VISUAL);
		addIntegerSetting("scale", "Etiket boyutu", 25, 10, 40, 5);
		addIntegerSetting("range", "Menzil", 64, 16, 128, 16);
	}
}
