package dev.turkishmc.mcmod.module.features;

import dev.turkishmc.mcmod.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public final class AutoTotemModule extends Module {
	public AutoTotemModule() {
		super("auto_totem", "Auto Totem", "Yan elde totem yoksa envanterden otomatik taşır.", Category.UTILITY);
	}

	@Override
	protected void tick(Minecraft client) {
		if (client.player == null || client.gameMode == null
				|| client.player.containerMenu != client.player.inventoryMenu
				|| !client.player.containerMenu.getCarried().isEmpty()
				|| client.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
			return;
		}

		var menu = client.player.inventoryMenu;
		for (int slot = 9; slot < 45; slot++) {
			if (slot == 45 || !menu.getSlot(slot).getItem().is(Items.TOTEM_OF_UNDYING)) {
				continue;
			}
			client.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.PICKUP, client.player);
			client.gameMode.handleInventoryMouseClick(menu.containerId, 45, 0, ClickType.PICKUP, client.player);
			client.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.PICKUP, client.player);
			return;
		}
	}
}
