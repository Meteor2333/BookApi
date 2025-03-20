package com.meteor.bookapi;

import com.meteor.bookapi.engine.nbt.LegacyNBTEngine;
import com.meteor.bookapi.engine.nbt.LatestNBTEngine;
import com.meteor.bookapi.engine.nbt.NBTEngine;
import com.meteor.bookapi.engine.packet.BukkitAPIPackageEngine;
import com.meteor.bookapi.engine.packet.PackageEngine;
import com.meteor.bookapi.engine.packet.ReflectionPackageEngine;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.*;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BookApi {
    private static Boolean isLoaded = false;
    private static Logger logger = Logger.getLogger("BookApi");

    private static final NBTEngine nbtEngine;
    private static final PackageEngine packageEngine;

    private static final Method CraftBukkit_Class_CraftItemStack_AsCraftCopy_Method;
    private static final Field CraftBukkit_Class_CraftItemStack_Handle_Field;

    static {
        try {
            if (ReflectionUtil.supportedClass("net.minecraft.core.component.DataComponentHolder")) nbtEngine = new LatestNBTEngine();
            else nbtEngine = new LegacyNBTEngine();

            if (ReflectionUtil.supportedMethod(Player.class, "openBook", ItemStack.class)) packageEngine = new BukkitAPIPackageEngine();
            else packageEngine = new ReflectionPackageEngine();

            Class<?> cisClass = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
            CraftBukkit_Class_CraftItemStack_AsCraftCopy_Method = cisClass.getDeclaredMethod("asCraftCopy", ItemStack.class);
            CraftBukkit_Class_CraftItemStack_Handle_Field = cisClass.getDeclaredField("handle");
        } catch (Exception e) {
            throw new UnsupportedOperationException("Cannot run on the current version of Minecraft server: " + ReflectionUtil.getVersion() + "!", e);
        }

        BookApi.CraftBukkit_Class_CraftItemStack_AsCraftCopy_Method.setAccessible(true);
        BookApi.CraftBukkit_Class_CraftItemStack_Handle_Field.setAccessible(true);
        BookApi.isLoaded = true;
    }

    private BookApi() {
        throw new IllegalAccessError("BookApi class");
    }

    /**
     * Open a {@link Material#WRITTEN_BOOK} for a Player
     *
     * @param player the target player
     * @param book the book to open for this player
     */
    public static void openBook(@Nonnull Player player, @Nonnull Book book) {
        if (!BookApi.isLoaded) return;
        try {
            Object bukkitItem = BookApi.CraftBukkit_Class_CraftItemStack_AsCraftCopy_Method.invoke(null, new ItemStack(Material.WRITTEN_BOOK));
            Object nmsItem = BookApi.CraftBukkit_Class_CraftItemStack_Handle_Field.get(bukkitItem);
            BookApi.nbtEngine.setBookNBT(nmsItem, BookApi.class.getName(), "Written Book", book.pages);
            BookApi.packageEngine.openBook(player, (ItemStack) bukkitItem, nmsItem);
        } catch (Throwable e) {
            BookApi.logger.log(Level.SEVERE, "A fatal error occurred when trying to open the book!", e);
        }
    }

    /**
     * Open a {@link Material#WRITTEN_BOOK} for Players
     *
     * @param players the target players
     * @param book the book to open for these players
     */
    public static void openBook(@Nonnull List<Player> players, @Nonnull Book book) {
        if (!BookApi.isLoaded) return;
        try {
            Object bukkitItem = BookApi.CraftBukkit_Class_CraftItemStack_AsCraftCopy_Method.invoke(null, new ItemStack(Material.WRITTEN_BOOK));
            Object nmsItem = BookApi.CraftBukkit_Class_CraftItemStack_Handle_Field.get(bukkitItem);
            BookApi.nbtEngine.setBookNBT(nmsItem, BookApi.class.getName(), "Written Book", book.pages);
            for (Player player : players) BookApi.packageEngine.openBook(player, (ItemStack) bukkitItem, nmsItem);
        } catch (Throwable e) {
            BookApi.logger.log(Level.SEVERE, "A fatal error occurred when trying to open the book!", e);
        }
    }

    /**
     * Replace the existing Logger
     *
     * @param logger the Logger instance to be replaced
     */
    public static void setLogger(@Nonnull Logger logger) {
        BookApi.logger = Objects.requireNonNull(logger);
    }
}
