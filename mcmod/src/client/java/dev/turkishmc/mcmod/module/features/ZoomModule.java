package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class ZoomModule extends Module {
	private boolean zooming;
	private int previousFov = 70;

	public ZoomModule() {
		super("zoom", "Zoom", "C tuşunu basılı tutarak yakınlaştırır.", Category.VISUAL);
		setKeyCode(GLFW.GLFW_KEY_C);
		addIntegerSetting("fov", "Yakınlaştırma FOV", 30, 20, 70, 5);
	}

	@Override
	public void onKeybind(boolean down, boolean pressed, Minecraft client) {
		boolean shouldZoom = isEnabled() && down;
		if (shouldZoom && !zooming) {
			previousFov = client.options.fov().get();
			zooming = true;
		}
		if (shouldZoom) {
			client.options.fov().set(getSetting("fov").getInt());
		} else if (zooming) {
			client.options.fov().set(previousFov);
			zooming = false;
		}
	}

	@Override
	protected void onToggled(boolean enabled) {
		if (!enabled && zooming) {
			Minecraft client = Minecraft.getInstance();
			client.options.fov().set(previousFov);
			zooming = false;
		}
	}
}
