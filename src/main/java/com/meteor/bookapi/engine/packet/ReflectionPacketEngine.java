package com.meteor.bookapi.engine.packet;

import com.meteor.bookapi.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;

public class ReflectionPacketEngine implements PacketEngine {
    private static final Constructor<?> NMS_PACKETPLAYOUTSETSLOT_CONSTRUCTOR;
    private static final Method CRAFTBUKKIT_CRAFTPLAYER_GETHANDLE_METHOD;
    private static final Method NMS_PLAYERCONNECTION_SENDPACKET_METHOD;
    private static final Method NMS_ENTITYPLAYER_OPENBOOK_METHOD;
    private static final Field NMS_ENTITYPLAYER_PLAYERCONNECTION_FIELD;
    private static final Enum<?> NMS_ENUMHAND_MAINHAND_ENUM;
    private static final Integer NMS_ENTITYPLAYER_OPENBOOK_METHOD_PARAMETERCOUNT;

    static {
        try {
            Class<?> isClass = ReflectionUtil.getNMSClass("ItemStack", "net.minecraft.world.item");
            Class<?> pcClass = ReflectionUtil.getNMSClass("PlayerConnection", "net.minecraft.server.network");
            Class<?> pClass = ReflectionUtil.getNMSClass("Packet", "net.minecraft.network.protocol");
            Class<?> epClass = ReflectionUtil.getNMSClass("EntityPlayer", "net.minecraft.server.level");
            Class<?> eheClass = null;
            try {
                eheClass = ReflectionUtil.getNMSClass("EnumHand", "net.minecraft.world");
            } catch (ClassNotFoundException ignored) { }

            NMS_PACKETPLAYOUTSETSLOT_CONSTRUCTOR = ReflectionUtil.getNMSClass("PacketPlayOutSetSlot", "net.minecraft.network.protocol.game").getDeclaredConstructor(int.class, int.class, isClass);
            CRAFTBUKKIT_CRAFTPLAYER_GETHANDLE_METHOD = ReflectionUtil.getOBCClass("entity.CraftPlayer").getDeclaredMethod("getHandle");

            Method method = null;
            for (Method pMethod : pcClass.getDeclaredMethods()) {
                if (Modifier.isStatic(pMethod.getModifiers())) continue;
                Class<?>[] params = pMethod.getParameterTypes();
                if (params.length == 1 && params[0] == pClass) {
                    method = pMethod;
                    break;
                }
            } NMS_PLAYERCONNECTION_SENDPACKET_METHOD = Objects.requireNonNull(method);

            method = null;
            for (Method obMethod : epClass.getDeclaredMethods()) {
                if (Modifier.isStatic(obMethod.getModifiers())) continue;
                Class<?>[] params = obMethod.getParameterTypes();
                if ((params.length == 1 && params[0] == isClass) || (params.length == 2 && eheClass != null && params[1] == eheClass)) {
                    method = obMethod;
                    break;
                }
            } NMS_ENTITYPLAYER_OPENBOOK_METHOD = Objects.requireNonNull(method);

            Field field = null;
            for (Field pcField : epClass.getDeclaredFields()) {
                if (Modifier.isStatic(pcField.getModifiers())) continue;
                if (pcField.getType() == pcClass) {
                    field = pcField;
                    break;
                }
            } NMS_ENTITYPLAYER_PLAYERCONNECTION_FIELD = Objects.requireNonNull(field);

            NMS_ENUMHAND_MAINHAND_ENUM = Optional.ofNullable(eheClass).map(clazz -> clazz.asSubclass(Enum.class).getEnumConstants()[0]).orElse(null);
            NMS_ENTITYPLAYER_OPENBOOK_METHOD_PARAMETERCOUNT = method.getParameterCount();

            NMS_PACKETPLAYOUTSETSLOT_CONSTRUCTOR.setAccessible(true);
            CRAFTBUKKIT_CRAFTPLAYER_GETHANDLE_METHOD.setAccessible(true);
            NMS_PLAYERCONNECTION_SENDPACKET_METHOD.setAccessible(true);
            NMS_ENTITYPLAYER_OPENBOOK_METHOD.setAccessible(true);
            NMS_ENTITYPLAYER_PLAYERCONNECTION_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void openBook(Player player, ItemStack bukkitItem, Object nmsItem) throws Exception {
        try {
            Object[] args = new Object[NMS_ENTITYPLAYER_OPENBOOK_METHOD_PARAMETERCOUNT];
            args[0] = nmsItem;
            if (NMS_ENTITYPLAYER_OPENBOOK_METHOD_PARAMETERCOUNT == 2) {
                args[1] = NMS_ENUMHAND_MAINHAND_ENUM;
            }

            Object nmsPlayer = CRAFTBUKKIT_CRAFTPLAYER_GETHANDLE_METHOD.invoke(player);
            Object connection = NMS_ENTITYPLAYER_PLAYERCONNECTION_FIELD.get(nmsPlayer);
            Object packet = NMS_PACKETPLAYOUTSETSLOT_CONSTRUCTOR.newInstance(0, player.getInventory().getHeldItemSlot() + 36, nmsItem);
            NMS_PLAYERCONNECTION_SENDPACKET_METHOD.invoke(connection, packet);
            NMS_ENTITYPLAYER_OPENBOOK_METHOD.invoke(nmsPlayer, args);
        } finally {
            player.updateInventory();
        }
    }
}
