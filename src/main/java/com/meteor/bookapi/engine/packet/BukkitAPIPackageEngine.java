package com.meteor.bookapi.engine.packet;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BukkitAPIPackageEngine implements PackageEngine {
    @Override
    public void openBook(Player player, ItemStack bukkitItem, Object nmsItem) {
        player.openBook(bukkitItem);
    }
}
