package dev.turkishmc.mcmod.render;

import dev.turkishmc.mcmod.McModClient;
import dev.turkishmc.mcmod.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class HudOverlay {
	private static final int PANEL = 0xB9101824;
	private static final int ACCENT = 0xFF59D5C5;
	private static final int TEXT = 0xFFF0F5FA;
	private static final int MUTED = 0xFF8293A5;

	private HudOverlay() {
	}

	public static void register() {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("mcmod", "client_hud"), HudOverlay::render);
	}

	private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.options.hideGui) {
			return;
		}

		for (Module module : McModClient.MODULES.getModules()) {
			module.renderHud(client, graphics);
		}
	}

	public static void drawInfo(GuiGraphics graphics, Minecraft client) {
		int x = 8;
		int y = 8;
		int width = 126;
		graphics.fill(x, y, x + width, y + 45, PANEL);
		graphics.fill(x, y, x + 2, y + 45, ACCENT);
		String coordinates = String.format("X %.1f  Y %.1f  Z %.1f",
				client.player.getX(), client.player.getY(), client.player.getZ());
		var info = client.getConnection() == null ? null
				: client.getConnection().getPlayerInfo(client.player.getUUID());
		String ping = info == null ? "--" : info.getLatency() + " ms";
		graphics.drawString(client.font, "FPS  " + client.getFps() + "    PING  " + ping,
				x + 8, y + 8, TEXT);
		graphics.drawString(client.font, coordinates, x + 8, y + 26, MUTED);
	}

	public static void drawArmor(GuiGraphics graphics, Minecraft client) {
		List<ItemStack> items = new ArrayList<>();
		for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST,
				EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
			ItemStack stack = client.player.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				items.add(stack);
			}
		}
		if (!client.player.getMainHandItem().isEmpty()) {
			items.add(client.player.getMainHandItem());
		}
		if (!client.player.getOffhandItem().isEmpty()) {
			items.add(client.player.getOffhandItem());
		}

		int startX = (graphics.guiWidth() - items.size() * 24) / 2;
		int y = graphics.guiHeight() - 78;
		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
			int x = startX + i * 24;
			graphics.fill(x, y, x + 22, y + 34, PANEL);
			graphics.renderItem(stack, x + 3, y + 2);
			if (stack.isDamageableItem()) {
				int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
				float ratio = (float) remaining / stack.getMaxDamage();
				int color = ratio > 0.5F ? 0xFF55DD88 : ratio > 0.2F ? 0xFFFFC857 : 0xFFFF5C6C;
				graphics.drawCenteredString(client.font, Integer.toString(remaining),
						x + 11, y + 23, color);
			}
		}
	}

	public static void drawKeystrokes(GuiGraphics graphics, Minecraft client) {
		int x = 8;
		int y = graphics.guiHeight() - 72;
		drawKey(graphics, client, x + 24, y, GLFW.GLFW_KEY_W, "W");
		drawKey(graphics, client, x, y + 23, GLFW.GLFW_KEY_A, "A");
		drawKey(graphics, client, x + 24, y + 23, GLFW.GLFW_KEY_S, "S");
		drawKey(graphics, client, x + 48, y + 23, GLFW.GLFW_KEY_D, "D");
		drawMouse(graphics, client, x, y + 46, GLFW.GLFW_MOUSE_BUTTON_LEFT, "L");
		drawMouse(graphics, client, x + 26, y + 46, GLFW.GLFW_MOUSE_BUTTON_RIGHT, "R");
	}

	private static void drawKey(GuiGraphics graphics, Minecraft client, int x, int y, int code, String label) {
		boolean pressed = GLFW.glfwGetKey(client.getWindow().handle(), code) == GLFW.GLFW_PRESS;
		drawKeyBox(graphics, x, y, label, pressed);
	}

	private static void drawMouse(GuiGraphics graphics, Minecraft client, int x, int y, int button, String label) {
		boolean pressed = GLFW.glfwGetMouseButton(client.getWindow().handle(), button) == GLFW.GLFW_PRESS;
		drawKeyBox(graphics, x, y, label, pressed);
	}

	private static void drawKeyBox(GuiGraphics graphics, int x, int y, String label, boolean pressed) {
		graphics.fill(x, y, x + 22, y + 20, pressed ? 0xFF14534E : PANEL);
		graphics.drawCenteredString(Minecraft.getInstance().font, label, x + 11, y + 6,
				pressed ? ACCENT : TEXT);
	}

	public static void drawActiveModules(GuiGraphics graphics) {
		List<Module> active = McModClient.MODULES.getModules().stream()
				.filter(Module::isEnabled)
				.toList();
		int y = 10;
		for (Module module : active) {
			String label = module.getName();
			int textWidth = Minecraft.getInstance().font.width(label);
			int x = graphics.guiWidth() - textWidth - 20;
			graphics.fill(x - 6, y - 3, graphics.guiWidth() - 8, y + 12, PANEL);
			graphics.fill(graphics.guiWidth() - 10, y - 3, graphics.guiWidth() - 8, y + 12, ACCENT);
			graphics.drawString(Minecraft.getInstance().font, label, x, y, TEXT);
			y += 18;
		}
	}

	public static void drawEffects(GuiGraphics graphics, Minecraft client, int color) {
		List<String> effects = client.player.getActiveEffects().stream()
				.map(effect -> effect.getEffect().value().getDisplayName().getString()
						+ " " + (effect.getAmplifier() + 1) + "  " + formatDuration(effect.getDuration()))
				.toList();
		if (effects.isEmpty()) {
			return;
		}

		int width = 142;
		int x = graphics.guiWidth() - width - 8;
		int y = graphics.guiHeight() - 12 - effects.size() * 15;
		graphics.fill(x, y, x + width, y + effects.size() * 15 + 6, PANEL);
		graphics.fill(x, y, x + 2, y + effects.size() * 15 + 6, 0xFF000000 | color);
		for (int i = 0; i < effects.size(); i++) {
			String label = effects.get(i);
			if (client.font.width(label) > width - 12) {
				label = client.font.plainSubstrByWidth(label, width - 16);
			}
			graphics.drawString(client.font, label, x + 8, y + 4 + i * 15, TEXT);
		}
	}

	public static void drawDirection(GuiGraphics graphics, Minecraft client, int color) {
		String[] directions = {"G", "GB", "B", "KB", "K", "KD", "D", "GD"};
		int index = Math.floorMod(Math.round(client.player.getYRot() / 45.0F), directions.length);
		String direction = directions[index];
		int centerX = graphics.guiWidth() / 2;
		graphics.fill(centerX - 28, 8, centerX + 28, 29, PANEL);
		graphics.fill(centerX - 28, 8, centerX + 28, 10, 0xFF000000 | color);
		graphics.drawCenteredString(client.font, direction, centerX, 15, 0xFF000000 | color);
	}

	private static String formatDuration(int ticks) {
		if (ticks < 0) {
			return "∞";
		}
		int seconds = ticks / 20;
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}

}
