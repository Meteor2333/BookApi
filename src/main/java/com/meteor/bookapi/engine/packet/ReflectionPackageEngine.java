package com.meteor.bookapi.engine.packet;

import com.meteor.bookapi.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.lang.reflect.Method;
import java.util.Objects;

public class ReflectionPackageEngine implements PackageEngine {
    private static final Method CraftBukkit_Class_CraftPlayer_GetHandle_Method;
    private static final Method NMS_Class_EntityPlayer_OpenBook_Method;
    private static final Enum<?> NMS_Class_EnumHand_MainHand_Enum;

    static {
        try {
            Class<?> epClass = ReflectionUtil.getNMSClass("EntityPlayer", "net.minecraft.server.level");
            Class<?> isClass = ReflectionUtil.getNMSClass("ItemStack", "net.minecraft.world.item");
            Class<?> ehClass;
            try {
                ehClass = ReflectionUtil.getNMSClass("EnumHand", "net.minecraft.world");
            } catch (ClassNotFoundException e) {
                ehClass = null;
            }

            CraftBukkit_Class_CraftPlayer_GetHandle_Method = ReflectionUtil.getOBCClass("entity.CraftPlayer").getDeclaredMethod("getHandle");

            Method method = null;
            for (Method openbookMethod : epClass.getDeclaredMethods()) {
                Class<?>[] params = openbookMethod.getParameterTypes();
                if ((params.length == 1 && params[0] == isClass) || (params.length == 2 && ehClass != null && params[1] == ehClass)) {
                    method = openbookMethod;
                    break;
                }
            } NMS_Class_EntityPlayer_OpenBook_Method = Objects.requireNonNull(method);

            if (ehClass != null) NMS_Class_EnumHand_MainHand_Enum = ehClass.asSubclass(Enum.class).getEnumConstants()[0];
            else NMS_Class_EnumHand_MainHand_Enum = null;

            ReflectionPackageEngine.CraftBukkit_Class_CraftPlayer_GetHandle_Method.setAccessible(true);
            ReflectionPackageEngine.NMS_Class_EntityPlayer_OpenBook_Method.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void openBook(Player player, ItemStack bukkitItem, Object nmsItem) throws Exception {
        PlayerInventory inventory = player.getInventory();
        int slot = inventory.getHeldItemSlot();
        ItemStack before = inventory.getItem(slot);
        try {
            int length = ReflectionPackageEngine.NMS_Class_EntityPlayer_OpenBook_Method.getParameterTypes().length;
            Object[] args;
            if (length == 1) args = new Object[]{ nmsItem };
            else if (length == 2) args = new Object[]{ nmsItem, ReflectionPackageEngine.NMS_Class_EnumHand_MainHand_Enum };
            else throw new IllegalArgumentException("wrong number of arguments");
            inventory.setItem(slot, bukkitItem);
            ReflectionPackageEngine.NMS_Class_EntityPlayer_OpenBook_Method.invoke(ReflectionPackageEngine.CraftBukkit_Class_CraftPlayer_GetHandle_Method.invoke(player), args);
        } finally {
            inventory.setItem(slot, before);
            player.updateInventory();
        }
    }
}
