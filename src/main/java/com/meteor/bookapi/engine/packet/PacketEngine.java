package com.meteor.bookapi.engine.packet;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PacketEngine {
    void openBook(Player player, ItemStack bukkitItem, Object nmsItem) throws Exception;
}
