package dev.turkishmc.mcmod.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Module {
	private final String id;
	private final String name;
	private final String description;
	private final Category category;
	private final Map<String, ModuleSetting> settings = new LinkedHashMap<>();
	private boolean enabled;
	private int keyCode = -1;

	protected Module(String id, String name, String description, Category category) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected Module(String name) {
		this(name.toLowerCase().replace(' ', '_'), name, "Varlık kutularını dünyada göster.", Category.VISUAL);
	}

	public final String getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return description;
	}

	public final Category getCategory() {
		return category;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final int getKeyCode() {
		return keyCode;
	}

	public final void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public final void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			onToggled(enabled);
		}
	}

	public final void toggle() {
		setEnabled(!enabled);
	}

	protected final ModuleSetting addBooleanSetting(String id, String label, boolean value) {
		return addSetting(new ModuleSetting(id, label, ModuleSetting.Type.BOOLEAN, value, 0, 1));
	}

	protected final ModuleSetting addIntegerSetting(String id, String label, int value, int min, int max, int step) {
		return addSetting(new ModuleSetting(id, label, ModuleSetting.Type.INTEGER, value, min, max, step));
	}

	protected final ModuleSetting addColorSetting(String id, String label, int value) {
		return addSetting(new ModuleSetting(id, label, ModuleSetting.Type.COLOR, value, 0, 0xFFFFFF, 1));
	}

	private ModuleSetting addSetting(ModuleSetting setting) {
		if (settings.putIfAbsent(setting.getId(), setting) != null) {
			throw new IllegalArgumentException("Aynı ayar kimliği zaten var: " + setting.getId());
		}
		return setting;
	}

	public final Collection<ModuleSetting> getSettings() {
		return settings.values();
	}

	public final ModuleSetting getSetting(String id) {
		ModuleSetting setting = settings.get(id);
		if (setting == null) {
			throw new IllegalArgumentException("Modül ayarı bulunamadı: " + id);
		}
		return setting;
	}

	public final void onClientTick(Minecraft client) {
		if (enabled) {
			tick(client);
		}
	}

	public final void renderHud(Minecraft client, GuiGraphics graphics) {
		if (enabled) {
			drawHud(client, graphics);
		}
	}

	public final void extractWorld(WorldExtractionContext context) {
		if (enabled) {
			extractWorldState(context);
		}
	}

	public final void renderWorld(WorldRenderContext context) {
		if (enabled) {
			drawWorld(context);
		}
	}

	public void onKeybind(boolean down, boolean pressed, Minecraft client) {
		if (pressed) {
			toggle();
		}
	}

	protected void onToggled(boolean enabled) {
	}

	protected void tick(Minecraft client) {
	}

	protected void drawHud(Minecraft client, GuiGraphics graphics) {
	}

	protected void extractWorldState(WorldExtractionContext context) {
	}

	protected void drawWorld(WorldRenderContext context) {
	}

	public enum Category {
		VISUAL("Görsel"),
		HUD("HUD"),
		UTILITY("Kolaylık");

		private final String title;

		Category(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}
}
