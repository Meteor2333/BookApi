package com.meteor.bookapi.engine.nbt;

import com.google.common.collect.Lists;
import com.meteor.bookapi.ReflectionUtil;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LatestNBTEngine implements NBTEngine {
    private static final Constructor<?> NMS_Class_WrittenBookContent_Constructor;
    private static final Method CraftBukkit_Class_CraftChatMessage_FromJSON_Method;
    private static final Method NMS_Class_Filterable_PassThrough_Method;
    private static final Field NMS_Class_ItemStack_PatchedDataComponentMap_Field;
    private static final Field NMS_Class_PatchedDataComponentMap_Reference2ObjectMap_Field;
    private static final Object NMS_Class_DataComponentType_WrittenBookContent_Object;

    static {
        try {
            Class<?> fClass = ReflectionUtil.getNMSClass("Filterable", "net.minecraft.server.network");
            Class<?> pdcmClass = ReflectionUtil.getNMSClass("PatchedDataComponentMap", "net.minecraft.core.component");
            Class<?> isClass = ReflectionUtil.getNMSClass("ItemStack", "net.minecraft.world.item");
            Class<?> dcClass = ReflectionUtil.getNMSClass("DataComponents", "net.minecraft.core.component");
            Class<?> wbcClass = ReflectionUtil.getNMSClass("WrittenBookContent", "net.minecraft.world.item.component");
            NMS_Class_WrittenBookContent_Constructor = ReflectionUtil.getNMSClass("WrittenBookContent", "net.minecraft.world.item.component").getConstructor(fClass, String.class, int.class, List.class, boolean.class);
            CraftBukkit_Class_CraftChatMessage_FromJSON_Method = ReflectionUtil.getOBCClass("util.CraftChatMessage").getDeclaredMethod("fromJSON", String.class);
            NMS_Class_Filterable_PassThrough_Method = fClass.getDeclaredMethod("a", Object.class);

            Field field = null;
            for (Field pdcmField : isClass.getDeclaredFields()) {
                if (pdcmField.getType() == pdcmClass) {
                    field = pdcmField;
                    break;
                }
            } NMS_Class_ItemStack_PatchedDataComponentMap_Field = Objects.requireNonNull(field);

            field = null;
            for (Field mapField : pdcmClass.getDeclaredFields()) {
                if (mapField.getType() == Reference2ObjectMap.class) {
                    field = mapField;
                    break;
                }
            } NMS_Class_PatchedDataComponentMap_Reference2ObjectMap_Field = Objects.requireNonNull(field);

            field = null;
            for (Field wbcField : dcClass.getDeclaredFields()) {
                if (!Modifier.isStatic(wbcField.getModifiers())) continue;
                Type generic = wbcField.getGenericType();
                if (generic instanceof ParameterizedType) {
                    Type[] actual = ((ParameterizedType) generic).getActualTypeArguments();
                    if (actual.length >= 1 && actual[0].equals(wbcClass)) {
                        wbcField.setAccessible(true);
                        field = wbcField;
                        break;
                    }
                }
            } NMS_Class_DataComponentType_WrittenBookContent_Object = Objects.requireNonNull(field).get(null);

            LatestNBTEngine.NMS_Class_WrittenBookContent_Constructor.setAccessible(true);
            LatestNBTEngine.CraftBukkit_Class_CraftChatMessage_FromJSON_Method.setAccessible(true);
            LatestNBTEngine.NMS_Class_Filterable_PassThrough_Method.setAccessible(true);
            LatestNBTEngine.NMS_Class_ItemStack_PatchedDataComponentMap_Field.setAccessible(true);
            LatestNBTEngine.NMS_Class_PatchedDataComponentMap_Reference2ObjectMap_Field.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setBookNBT(Object nmsItem, String author, String title, List<BaseComponent[]> pages) throws Exception {
        List<Object> list = Lists.newArrayList();
        for (BaseComponent[] page : pages) {
            list.add(LatestNBTEngine.NMS_Class_Filterable_PassThrough_Method.invoke(null, LatestNBTEngine.CraftBukkit_Class_CraftChatMessage_FromJSON_Method.invoke(null, ComponentSerializer.toString(page))));
        }

        Reference2ObjectMap<Object, Optional<?>> map = new Reference2ObjectArrayMap<>();
        map.put(LatestNBTEngine.NMS_Class_DataComponentType_WrittenBookContent_Object, Optional.of(LatestNBTEngine.NMS_Class_WrittenBookContent_Constructor.newInstance(LatestNBTEngine.NMS_Class_Filterable_PassThrough_Method.invoke(null, title), author, 0, list, true)));
        LatestNBTEngine.NMS_Class_PatchedDataComponentMap_Reference2ObjectMap_Field.set(LatestNBTEngine.NMS_Class_ItemStack_PatchedDataComponentMap_Field.get(nmsItem), map);
    }
}
