package cc.meteormc.bookapi;

import cc.meteormc.bookapi.engine.nbt.LatestNBTEngine;
import cc.meteormc.bookapi.engine.nbt.LegacyNBTEngine;
import cc.meteormc.bookapi.engine.nbt.NBTEngine;
import cc.meteormc.bookapi.engine.packet.BukkitAPIPacketEngine;
import cc.meteormc.bookapi.engine.packet.PacketEngine;
import cc.meteormc.bookapi.engine.packet.ReflectionPacketEngine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main API class.
 */
public final class BookApi {
    private static Boolean isLoaded = false;
    private static final Logger logger;
    private static final NBTEngine nbtEngine;
    private static final PacketEngine packetEngine;

    private static final Method CRAFTBUKKIT_CRAFTITEMSTACK_ASCRAFTCOPY_METHOD;
    private static final Field CRAFTBUKKIT_CRAFTITEMSTACK_HANDLE_FIELD;

    static {
        try {
            try (InputStream stream = BookApi.class.getClassLoader().getResourceAsStream("plugin.yml")) {
                Plugin plugin = null;
                if (stream != null) {
                    try (InputStreamReader reader = new InputStreamReader(stream)) {
                        String name = YamlConfiguration.loadConfiguration(reader).getString("name", "");
                        assert name != null;
                        plugin = Bukkit.getPluginManager().getPlugin(name);
                    }
                }

                if (plugin != null) logger = plugin.getLogger();
                else logger = Logger.getLogger("BookApi");
            }

            if (ReflectionUtil.supportedClass("net.minecraft.core.component.DataComponentHolder")) nbtEngine = new LatestNBTEngine();
            else nbtEngine = new LegacyNBTEngine();

            if (ReflectionUtil.supportedMethod(Player.class, "openBook", ItemStack.class)) packetEngine = new BukkitAPIPacketEngine();
            else packetEngine = new ReflectionPacketEngine();

            Class<?> cisClass = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
            CRAFTBUKKIT_CRAFTITEMSTACK_ASCRAFTCOPY_METHOD = cisClass.getDeclaredMethod("asCraftCopy", ItemStack.class);
            CRAFTBUKKIT_CRAFTITEMSTACK_HANDLE_FIELD = cisClass.getDeclaredField("handle");

            CRAFTBUKKIT_CRAFTITEMSTACK_ASCRAFTCOPY_METHOD.setAccessible(true);
            CRAFTBUKKIT_CRAFTITEMSTACK_HANDLE_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Cannot run on the current version of Minecraft server: " + ReflectionUtil.getVersion() + "!", e);
        } BookApi.isLoaded = true;
    }

    private BookApi() {
        throw new IllegalStateException("Instantiation is not allowed!");
    }

    /**
     * Open a {@link Material#WRITTEN_BOOK} for a Player.
     *
     * @param player the target player
     * @param book the book to open for this player
     */
    public static void openBook(@NotNull Player player, @NotNull Book book) {
        if (!isLoaded) return;
        try {
            Object bukkitItem = CRAFTBUKKIT_CRAFTITEMSTACK_ASCRAFTCOPY_METHOD.invoke(null, new ItemStack(Material.WRITTEN_BOOK));
            Object nmsItem = CRAFTBUKKIT_CRAFTITEMSTACK_HANDLE_FIELD.get(bukkitItem);
            nbtEngine.setBookNBT(nmsItem, BookApi.class.getName(), "WrittenBook", book.pages);
            packetEngine.openBook(player, (ItemStack) bukkitItem, nmsItem);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "A fatal error occurred when trying to open the book!", e);
        }
    }

    /**
     * Open a {@link Material#WRITTEN_BOOK} for Players.
     *
     * @param players the target players
     * @param book the book to open for these players
     */
    public static void openBook(@NotNull List<Player> players, @NotNull Book book) {
        if (!isLoaded) return;
        try {
            Object bukkitItem = CRAFTBUKKIT_CRAFTITEMSTACK_ASCRAFTCOPY_METHOD.invoke(null, new ItemStack(Material.WRITTEN_BOOK));
            Object nmsItem = CRAFTBUKKIT_CRAFTITEMSTACK_HANDLE_FIELD.get(bukkitItem);
            nbtEngine.setBookNBT(nmsItem, BookApi.class.getName(), "WrittenBook", book.pages);
            for (Player player : players) packetEngine.openBook(player, (ItemStack) bukkitItem, nmsItem);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "A fatal error occurred when trying to open the book!", e);
        }
    }
}
