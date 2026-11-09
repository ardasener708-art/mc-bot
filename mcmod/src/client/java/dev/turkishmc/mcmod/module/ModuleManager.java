package dev.turkishmc.mcmod.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ModuleManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("McMod/Modules");
	private final Map<String, Module> modules = new LinkedHashMap<>();

	public void register(Module module) {
		Objects.requireNonNull(module, "module");
		if (modules.putIfAbsent(module.getId(), module) != null) {
			throw new IllegalArgumentException("Aynı modül kimliği zaten kayıtlı: " + module.getId());
		}
	}

	public Collection<Module> getModules() {
		return List.copyOf(modules.values());
	}

	public List<Module> getModules(Module.Category category) {
		return modules.values().stream().filter(module -> module.getCategory() == category).toList();
	}

	public Module getModule(String id) {
		Module module = modules.get(id);
		if (module == null) {
			throw new IllegalArgumentException("Kayıtlı modül bulunamadı: " + id);
		}
		return module;
	}

	public Module findModule(String id) {
		return modules.get(id);
	}

	public void discover(String packageName) {
		String resourcePath = packageName.replace('.', '/');
		ClassLoader classLoader = ModuleManager.class.getClassLoader();
		Set<String> classNames = new LinkedHashSet<>();

		try {
			var container = FabricLoader.getInstance().getModContainer("mcmod")
					.orElseThrow(() -> new IllegalStateException("mcmod mod container bulunamadı."));
			for (Path root : container.getRootPaths()) {
				Path directory = root.resolve(resourcePath);
				if (Files.isDirectory(directory)) {
					try (var files = Files.list(directory)) {
						files.filter(path -> path.getFileName().toString().endsWith(".class"))
								.filter(path -> !path.getFileName().toString().contains("$"))
								.forEach(path -> classNames.add(packageName + "."
										+ path.getFileName().toString().replaceFirst("\\.class$", "")));
					}
				}
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Modül sınıfları keşfedilemedi: " + packageName, exception);
		}
		if (classNames.isEmpty()) {
			throw new IllegalStateException("Modül paketi boş veya erişilemiyor: " + packageName);
		}

		for (String className : classNames) {
			try {
				Class<?> candidate = Class.forName(className, true, classLoader);
				if (Module.class.isAssignableFrom(candidate) && !Modifier.isAbstract(candidate.getModifiers())) {
					register((Module) candidate.getDeclaredConstructor().newInstance());
				}
			} catch (ReflectiveOperationException exception) {
				LOGGER.error("Modül sınıfı oluşturulamadı: {}", className, exception);
			}
		}
	}
}
