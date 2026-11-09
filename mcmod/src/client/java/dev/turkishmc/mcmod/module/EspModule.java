package dev.turkishmc.mcmod.module;

public final class EspModule extends Module {
	public EspModule() {
		super("esp", "ESP", "Oyuncu ve mobları renkli kutularla vurgular.", Category.VISUAL);
		addColorSetting("color", "Oyuncu rengi", 0x55FF55);
		addColorSetting("hostile_color", "Hostile rengi", 0xFF5555);
		addColorSetting("passive_color", "Pasif mob rengi", 0x55AAFF);
	}
}
