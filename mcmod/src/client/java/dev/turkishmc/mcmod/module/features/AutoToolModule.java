package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public final class AutoToolModule extends Module {
	public AutoToolModule() {
		super("auto_tool", "Auto Tool", "Hedeflenen blok için en etkili hotbar aletini seçer.", Category.UTILITY);
	}

	@Override
	protected void tick(Minecraft client) {
		if (client.player == null || !client.options.keyAttack.isDown()
				|| !(client.hitResult instanceof BlockHitResult hit)) {
			return;
		}

		var state = client.level.getBlockState(hit.getBlockPos());
		int bestSlot = client.player.getInventory().getSelectedSlot();
		float bestSpeed = client.player.getMainHandItem().getDestroySpeed(state);

		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = client.player.getInventory().getItem(slot);
			float speed = stack.getDestroySpeed(state);
			if (speed > bestSpeed) {
				bestSpeed = speed;
				bestSlot = slot;
			}
		}
		client.player.getInventory().setSelectedSlot(bestSlot);
	}
}
