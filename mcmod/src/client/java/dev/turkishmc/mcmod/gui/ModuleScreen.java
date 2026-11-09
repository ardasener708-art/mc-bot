package dev.turkishmc.mcmod.gui;

import dev.turkishmc.mcmod.config.ModConfig;
import dev.turkishmc.mcmod.module.Module;
import dev.turkishmc.mcmod.module.ModuleManager;
import dev.turkishmc.mcmod.module.ModuleSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class ModuleScreen extends Screen {
	private static final int PANEL = 0xFF101824;
	private static final int PANEL_LIGHT = 0xFF172331;
	private static final int BORDER = 0xFF263746;
	private static final int TEXT = 0xFFF0F5FA;
	private static final int MUTED = 0xFF8293A5;
	private static final int ACCENT = 0xFF59D5C5;

	private final ModuleManager moduleManager;
	private Module.Category selectedCategory = Module.Category.VISUAL;
	private Module selectedModule;
	private EditBox searchBox;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int sidebarWidth;
	private int scrollOffset;
	private boolean waitingForKey;

	public ModuleScreen(ModuleManager moduleManager) {
		super(Component.literal("McMod"));
		this.moduleManager = moduleManager;
	}

	@Override
	protected void init() {
		panelWidth = Math.min(500, width - 24);
		panelHeight = Math.min(340, height - 24);
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		sidebarWidth = Math.min(104, panelWidth / 3);

		searchBox = new EditBox(font, panelX + sidebarWidth + 18, panelY + 68,
				Math.max(80, panelWidth - sidebarWidth - 36), 20, Component.literal("Ara"));
		searchBox.setHint(Component.literal("Modül ara..."));
		searchBox.setMaxLength(40);
		addRenderableWidget(searchBox);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, 0x99050A11);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL);
		graphics.fill(panelX, panelY, panelX + sidebarWidth, panelY + panelHeight, 0xFF0C131D);
		graphics.fill(panelX + sidebarWidth, panelY, panelX + sidebarWidth + 2, panelY + panelHeight, BORDER);

		drawSidebar(graphics);
		if (selectedModule == null) {
			drawModuleList(graphics, mouseX, mouseY);
		} else {
			drawSettings(graphics, mouseX, mouseY);
		}
		super.render(graphics, mouseX, mouseY, delta);
	}

	private void drawSidebar(GuiGraphics graphics) {
		graphics.fill(panelX, panelY, panelX + sidebarWidth, panelY + 3, ACCENT);
		graphics.fill(panelX + 16, panelY + 18, panelX + 43, panelY + 45, 0xFF18333A);
		graphics.drawCenteredString(font, "M", panelX + 29, panelY + 27, ACCENT);
		graphics.drawString(font, "MC MOD", panelX + 16, panelY + 54, TEXT);
		graphics.drawString(font, "CLIENT", panelX + 16, panelY + 68, MUTED);
		graphics.drawString(font, "MODÜLLER", panelX + 14, panelY + 98, MUTED);

		int categoryY = panelY + 113;
		for (Module.Category category : Module.Category.values()) {
			boolean selected = selectedCategory == category;
			if (selected) {
				graphics.fill(panelX + 8, categoryY - 5, panelX + sidebarWidth - 8, categoryY + 17, 0xFF172A35);
				graphics.fill(panelX + 8, categoryY - 5, panelX + 10, categoryY + 17, ACCENT);
			}
			graphics.drawString(font, category.getTitle(), panelX + 18, categoryY + 2,
					selected ? TEXT : MUTED);
			categoryY += 27;
		}

		graphics.fill(panelX + 12, panelY + panelHeight - 39,
				panelX + sidebarWidth - 12, panelY + panelHeight - 38, BORDER);
		graphics.drawString(font, "FABRIC 1.21.11", panelX + 14, panelY + panelHeight - 27, MUTED);
	}

	private void drawModuleList(GuiGraphics graphics, int mouseX, int mouseY) {
		int contentX = panelX + sidebarWidth + 18;
		int contentRight = panelX + panelWidth - 16;
		graphics.drawString(font, selectedCategory.getTitle().toUpperCase(), contentX, panelY + 17, ACCENT);
		graphics.drawString(font, "Özellikler", contentX, panelY + 34, TEXT);
		graphics.drawString(font, "Bir karta sağ tıklayarak ayarları aç.", contentX, panelY + 51, MUTED);

		List<Module> visible = filteredModules();
		int firstY = panelY + 96;
		int cardHeight = 48;
		int gap = 7;
		int maxVisible = Math.max(1, (panelY + panelHeight - 31 - firstY) / (cardHeight + gap));
		scrollOffset = Math.min(scrollOffset, Math.max(0, visible.size() - maxVisible));
		int end = Math.min(visible.size(), scrollOffset + maxVisible);

		for (int index = scrollOffset; index < end; index++) {
			Module module = visible.get(index);
			int rowY = firstY + (index - scrollOffset) * (cardHeight + gap);
			graphics.fill(contentX, rowY, contentRight, rowY + cardHeight, PANEL_LIGHT);
			graphics.fill(contentX, rowY, contentX + 2, rowY + cardHeight,
					module.isEnabled() ? ACCENT : BORDER);
			graphics.drawString(font, module.getName(), contentX + 11, rowY + 7, TEXT);
			graphics.drawString(font, trim(module.getDescription(), 40), contentX + 11, rowY + 25, MUTED);

			int toggleWidth = 58;
			int toggleX = contentRight - toggleWidth - 9;
			graphics.fill(toggleX, rowY + 13, toggleX + toggleWidth, rowY + 35,
					module.isEnabled() ? 0xFF14534E : 0xFF293541);
			graphics.drawCenteredString(font, module.isEnabled() ? "AÇIK" : "KAPALI",
					toggleX + toggleWidth / 2, rowY + 20, module.isEnabled() ? ACCENT : MUTED);
		}

		String count = visible.isEmpty() ? "Sonuç yok" : (scrollOffset + 1) + "-" + end + " / " + visible.size();
		graphics.drawString(font, count + "  ·  Sağ Shift: kapat", contentX, panelY + panelHeight - 20, MUTED);
	}

	private void drawSettings(GuiGraphics graphics, int mouseX, int mouseY) {
		int contentX = panelX + sidebarWidth + 18;
		int contentRight = panelX + panelWidth - 16;
		graphics.drawString(font, "<  GERİ", contentX, panelY + 17, ACCENT);
		graphics.drawString(font, selectedModule.getName(), contentX, panelY + 38, TEXT);
		graphics.drawString(font, trim(selectedModule.getDescription(), 48), contentX, panelY + 55, MUTED);

		int y = panelY + 83;
		for (ModuleSetting setting : selectedModule.getSettings()) {
			graphics.fill(contentX, y, contentRight, y + 27, PANEL_LIGHT);
			graphics.drawString(font, setting.getLabel(), contentX + 8, y + 9, TEXT);
			if (setting.getType() == ModuleSetting.Type.COLOR) {
				graphics.fill(contentRight - 94, y + 7, contentRight - 82, y + 20,
						0xFF000000 | setting.getInt());
			}
			graphics.drawString(font, setting.getDisplayValue(), contentRight - 76, y + 9, ACCENT);
			y += 31;
		}

		graphics.drawString(font, "Tuş: " + keyName(selectedModule.getKeyCode())
						+ (waitingForKey ? "  (tuşa bas)" : "  ·  Tuş atamak için buraya tıkla"),
				contentX, panelY + panelHeight - 34, waitingForKey ? ACCENT : MUTED);
		graphics.drawString(font, "Ayarı değiştirmek için tıkla  ·  Sağ tık: geri",
				contentX, panelY + panelHeight - 20, MUTED);
	}

	private List<Module> filteredModules() {
		String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
		List<Module> result = new ArrayList<>();
		for (Module module : moduleManager.getModules(selectedCategory)) {
			if (query.isEmpty() || module.getName().toLowerCase().contains(query)
					|| module.getDescription().toLowerCase().contains(query)) {
				result.add(module);
			}
		}
		return result;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (super.mouseClicked(event, doubled)) {
			return true;
		}

		double mouseX = event.x();
		double mouseY = event.y();
		if (mouseX >= panelX && mouseX < panelX + sidebarWidth
				&& mouseY >= panelY + 108 && mouseY < panelY + 108 + Module.Category.values().length * 27) {
			int categoryIndex = (int) (mouseY - (panelY + 108)) / 27;
			selectedCategory = Module.Category.values()[categoryIndex];
			selectedModule = null;
			scrollOffset = 0;
			return true;
		}

		int contentX = panelX + sidebarWidth + 18;
		int contentRight = panelX + panelWidth - 16;
		if (selectedModule != null) {
			int settingY = panelY + 83;
			for (ModuleSetting setting : selectedModule.getSettings()) {
				if ((event.button() == 0 || event.button() == 1)
						&& mouseX >= contentX && mouseX < contentRight
						&& mouseY >= settingY && mouseY < settingY + 27) {
					setting.adjust(event.button() == 0);
					ModConfig.save(moduleManager);
					return true;
				}
				settingY += 31;
			}

			if (event.button() == 1 || (mouseX >= contentX && mouseX < contentX + 48
					&& mouseY >= panelY + 10 && mouseY <= panelY + 30)) {
				selectedModule = null;
				waitingForKey = false;
				searchBox.setVisible(true);
				return true;
			}

			if (mouseX >= contentX && mouseX < contentRight
					&& mouseY >= panelY + panelHeight - 46) {
				waitingForKey = true;
				return true;
			}

			return true;
		}

		if (mouseX >= contentX && mouseX < contentRight && mouseY >= panelY + 96) {
			List<Module> visible = filteredModules();
			int row = (int) (mouseY - (panelY + 96)) / 55;
			int index = scrollOffset + row;
			int rowY = panelY + 96 + row * 55;
			if (row >= 0 && index < visible.size() && mouseY < rowY + 48) {
				Module module = visible.get(index);
				if (event.button() == 1) {
					selectedModule = module;
					searchBox.setVisible(false);
				} else if (mouseX >= contentRight - 72) {
					module.toggle();
					ModConfig.save(moduleManager);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (waitingForKey && selectedModule != null) {
			selectedModule.setKeyCode(event.key() == GLFW.GLFW_KEY_ESCAPE ? -1 : event.key());
			waitingForKey = false;
			ModConfig.save(moduleManager);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (selectedModule == null && mouseX >= panelX + sidebarWidth && mouseX < panelX + panelWidth) {
			int visibleCount = filteredModules().size();
			int maxVisible = Math.max(1,
					(panelHeight - 31 - 96) / (48 + 7));
			scrollOffset = Math.max(0, Math.min(visibleCount - maxVisible,
					scrollOffset + (verticalAmount < 0 ? 1 : -1)));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private static String keyName(int keyCode) {
		return keyCode < 0 ? "Yok" : GLFW.glfwGetKeyName(keyCode, 0) == null
				? "Klavye " + keyCode : GLFW.glfwGetKeyName(keyCode, 0).toUpperCase();
	}

	private static String trim(String value, int maxLength) {
		return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "...";
	}
}
