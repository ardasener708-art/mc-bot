package dev.turkishmc.mcmod.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.GsonBuilder;
import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.module.ModuleManager;
import dev.turkishmc.mcmod.module.ModuleSetting;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("McMod/Config");
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mcmod.json");

	private ModConfig() {
	}

	public static void load(ModuleManager manager) {
		if (!Files.exists(FILE)) {
			return;
		}

		try (Reader reader = Files.newBufferedReader(FILE)) {
			JsonElement rootElement = JsonParser.parseReader(reader);
			if (!rootElement.isJsonObject()) {
				throw new JsonParseException("Config kökü JSON nesnesi değil.");
			}
			JsonObject modules = rootElement.getAsJsonObject().getAsJsonObject("modules");
			if (modules == null) {
				throw new JsonParseException("Config içinde modules nesnesi bulunamadı.");
			}

			for (var entry : modules.entrySet()) {
				Module module = manager.findModule(entry.getKey());
				if (module == null || !entry.getValue().isJsonObject()) {
					continue;
				}

				JsonObject values = entry.getValue().getAsJsonObject();
				if (values.has("enabled") && values.get("enabled").isJsonPrimitive()) {
					JsonPrimitive primitive = values.getAsJsonPrimitive("enabled");
					if (primitive.isBoolean()) {
						module.setEnabled(primitive.getAsBoolean());
					}
				}
				if (values.has("keyCode") && values.get("keyCode").isJsonPrimitive()) {
					JsonPrimitive primitive = values.getAsJsonPrimitive("keyCode");
					if (primitive.isNumber()) {
						module.setKeyCode(primitive.getAsInt());
					}
				}
				JsonObject settings = values.getAsJsonObject("settings");
				if (settings != null) {
					for (ModuleSetting setting : module.getSettings()) {
						JsonElement value = settings.get(setting.getId());
						if (value != null && value.isJsonPrimitive()) {
							JsonPrimitive primitive = value.getAsJsonPrimitive();
							if (setting.getType() == ModuleSetting.Type.BOOLEAN && primitive.isBoolean()) {
								setting.setValue(primitive.getAsBoolean());
							} else if (setting.getType() != ModuleSetting.Type.BOOLEAN && primitive.isNumber()) {
								setting.setValue(primitive.getAsInt());
							}
						}
					}
				}
			}
		} catch (IOException | JsonParseException exception) {
			LOGGER.error("McMod config dosyası okunamadı: {}", FILE, exception);
		}
	}

	public static void save(ModuleManager manager) {
		JsonObject root = new JsonObject();
		JsonObject modules = new JsonObject();

		for (Module module : manager.getModules()) {
			JsonObject values = new JsonObject();
			values.addProperty("enabled", module.isEnabled());
			values.addProperty("keyCode", module.getKeyCode());

			JsonObject settings = new JsonObject();
			for (ModuleSetting setting : module.getSettings()) {
				if (setting.getType() == ModuleSetting.Type.BOOLEAN) {
					settings.addProperty(setting.getId(), setting.getBoolean());
				} else {
					settings.addProperty(setting.getId(), setting.getInt());
				}
			}
			values.add("settings", settings);
			modules.add(module.getId(), values);
		}
		root.add("modules", modules);

		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE)) {
				new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
			}
		} catch (IOException exception) {
			LOGGER.error("McMod config dosyası kaydedilemedi: {}", FILE, exception);
		}
	}
}
