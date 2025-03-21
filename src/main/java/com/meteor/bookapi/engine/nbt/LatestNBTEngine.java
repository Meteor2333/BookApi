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
    private static final Constructor<?> NMS_WRITTENBOOKCONTENT_CONSTRUCTOR;
    private static final Method CRAFTBUKKIT_CRAFTCHATMESSAGE_FROMJSON_METHOD;
    private static final Method NMS_FILTERABLE_PASSTHROUGH_METHOD;
    private static final Field NMS_ITEMSTACK_PATCHEDDATACOMPONENTMAP_FIELD;
    private static final Field NMS_PATCHEDDATACOMPONENTMAP_REFERENCE2OBJECTMAP_FIELD;
    private static final Object NMS_DATACOMPONENTTYPE_WRITTENBOOKCONTENT_OBJECT;

    static {
        try {
            Class<?> fClass = ReflectionUtil.getNMSClass("Filterable", "net.minecraft.server.network");
            Class<?> pdcmClass = ReflectionUtil.getNMSClass("PatchedDataComponentMap", "net.minecraft.core.component");
            Class<?> isClass = ReflectionUtil.getNMSClass("ItemStack", "net.minecraft.world.item");
            Class<?> dcClass = ReflectionUtil.getNMSClass("DataComponents", "net.minecraft.core.component");
            Class<?> dctClass = ReflectionUtil.getNMSClass("DataComponentType", "net.minecraft.core.component");
            Class<?> wbcClass = ReflectionUtil.getNMSClass("WrittenBookContent", "net.minecraft.world.item.component");
            NMS_WRITTENBOOKCONTENT_CONSTRUCTOR = ReflectionUtil.getNMSClass("WrittenBookContent", "net.minecraft.world.item.component").getDeclaredConstructor(fClass, String.class, int.class, List.class, boolean.class);
            CRAFTBUKKIT_CRAFTCHATMESSAGE_FROMJSON_METHOD = ReflectionUtil.getOBCClass("util.CraftChatMessage").getDeclaredMethod("fromJSON", String.class);
            NMS_FILTERABLE_PASSTHROUGH_METHOD = fClass.getDeclaredMethod("a", Object.class);

            Field field = null;
            for (Field pdcmField : isClass.getDeclaredFields()) {
                if (Modifier.isStatic(pdcmField.getModifiers())) continue;
                if (pdcmField.getType() == pdcmClass) {
                    field = pdcmField;
                    break;
                }
            } NMS_ITEMSTACK_PATCHEDDATACOMPONENTMAP_FIELD = Objects.requireNonNull(field);

            field = null;
            for (Field mField : pdcmClass.getDeclaredFields()) {
                if (Modifier.isStatic(mField.getModifiers())) continue;
                if (mField.getType() == Reference2ObjectMap.class) {
                    field = mField;
                    break;
                }
            } NMS_PATCHEDDATACOMPONENTMAP_REFERENCE2OBJECTMAP_FIELD = Objects.requireNonNull(field);

            field = null;
            for (Field wbcField : dcClass.getDeclaredFields()) {
                if (!Modifier.isStatic(wbcField.getModifiers())) continue;
                if (wbcField.getType() != dctClass) continue;
                Type generic = wbcField.getGenericType();
                if (generic instanceof ParameterizedType) {
                    Type[] actual = ((ParameterizedType) generic).getActualTypeArguments();
                    if (actual.length == 1 && actual[0] == wbcClass) {
                        wbcField.setAccessible(true);
                        field = wbcField;
                        break;
                    }
                }
            } NMS_DATACOMPONENTTYPE_WRITTENBOOKCONTENT_OBJECT = Objects.requireNonNull(field).get(null);

            NMS_WRITTENBOOKCONTENT_CONSTRUCTOR.setAccessible(true);
            CRAFTBUKKIT_CRAFTCHATMESSAGE_FROMJSON_METHOD.setAccessible(true);
            NMS_FILTERABLE_PASSTHROUGH_METHOD.setAccessible(true);
            NMS_ITEMSTACK_PATCHEDDATACOMPONENTMAP_FIELD.setAccessible(true);
            NMS_PATCHEDDATACOMPONENTMAP_REFERENCE2OBJECTMAP_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setBookNBT(Object nmsItem, String author, String title, List<BaseComponent[]> pages) throws Exception {
        List<Object> list = Lists.newArrayList();
        for (BaseComponent[] page : pages) {
            list.add(NMS_FILTERABLE_PASSTHROUGH_METHOD.invoke(null, CRAFTBUKKIT_CRAFTCHATMESSAGE_FROMJSON_METHOD.invoke(null, ComponentSerializer.toString(page))));
        }

        Reference2ObjectMap<Object, Optional<?>> map = new Reference2ObjectArrayMap<>();
        map.put(NMS_DATACOMPONENTTYPE_WRITTENBOOKCONTENT_OBJECT, Optional.of(NMS_WRITTENBOOKCONTENT_CONSTRUCTOR.newInstance(NMS_FILTERABLE_PASSTHROUGH_METHOD.invoke(null, title), author, 0, list, true)));
        NMS_PATCHEDDATACOMPONENTMAP_REFERENCE2OBJECTMAP_FIELD.set(NMS_ITEMSTACK_PATCHEDDATACOMPONENTMAP_FIELD.get(nmsItem), map);
    }
}
