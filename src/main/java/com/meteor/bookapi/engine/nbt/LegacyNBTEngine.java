package com.meteor.bookapi.engine.nbt;

import com.meteor.bookapi.ReflectionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LegacyNBTEngine implements NBTEngine {
    private static final Constructor<?> NMS_Class_NBTTagCompound_Constructor;
    private static final Constructor<?> NMS_Class_NBTTagList_Constructor;
    private static final Constructor<?> NMS_Class_NBTTagString_Constructor;
    private static final Method NMS_Class_NBTTagList_Add_Method;
    private static final Field NMS_Class_NBTTagCompound_Map_Field;
    private static final Field NMS_Class_ItemStack_NBTTagCompound_Field;

    static {
        try {
            Class<?> isClass = ReflectionUtil.getNMSClass("ItemStack", "net.minecraft.world.item");
            Class<?> nbClass = ReflectionUtil.getNMSClass("NBTBase", "net.minecraft.nbt");
            Class<?> ntcClass = ReflectionUtil.getNMSClass("NBTTagCompound", "net.minecraft.nbt");
            Class<?> ntlClass = ReflectionUtil.getNMSClass("NBTTagList", "net.minecraft.nbt");
            NMS_Class_NBTTagCompound_Constructor = ntcClass.getDeclaredConstructor();
            NMS_Class_NBTTagList_Constructor = ntlClass.getDeclaredConstructor();
            NMS_Class_NBTTagString_Constructor = ReflectionUtil.getNMSClass("NBTTagString", "net.minecraft.nbt").getDeclaredConstructor(String.class);

            Method method;
            try {
                method = ntlClass.getDeclaredMethod("add", nbClass);
            } catch (NoSuchMethodException e) {
                method = ntlClass.getMethod("add", Object.class);
            } NMS_Class_NBTTagList_Add_Method = method;

            Field field = null;
            for (Field mapField : ntcClass.getDeclaredFields()) {
                if (mapField.getType() == Map.class) {
                    field = mapField;
                    break;
                }
            } NMS_Class_NBTTagCompound_Map_Field = Objects.requireNonNull(field);

            field = null;
            for (Field tagField : isClass.getDeclaredFields()) {
                if (tagField.getType() == ntcClass) {
                    field = tagField;
                    break;
                }
            } NMS_Class_ItemStack_NBTTagCompound_Field = Objects.requireNonNull(field);

            LegacyNBTEngine.NMS_Class_NBTTagCompound_Constructor.setAccessible(true);
            LegacyNBTEngine.NMS_Class_NBTTagList_Constructor.setAccessible(true);
            LegacyNBTEngine.NMS_Class_NBTTagString_Constructor.setAccessible(true);
            LegacyNBTEngine.NMS_Class_NBTTagList_Add_Method.setAccessible(true);
            LegacyNBTEngine.NMS_Class_NBTTagCompound_Map_Field.setAccessible(true);
            LegacyNBTEngine.NMS_Class_ItemStack_NBTTagCompound_Field.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setBookNBT(Object nmsItem, String author, String title, List<BaseComponent[]> pages) throws Exception {
        Object nbtAuthor = LegacyNBTEngine.NMS_Class_NBTTagString_Constructor.newInstance(author);
        Object nbtTitle = LegacyNBTEngine.NMS_Class_NBTTagString_Constructor.newInstance(title);
        Object nbtPages = LegacyNBTEngine.NMS_Class_NBTTagList_Constructor.newInstance();
        Object nbtCompound = LegacyNBTEngine.NMS_Class_NBTTagCompound_Constructor.newInstance();
        for (BaseComponent[] page : pages) {
            LegacyNBTEngine.NMS_Class_NBTTagList_Add_Method.invoke(nbtPages, LegacyNBTEngine.NMS_Class_NBTTagString_Constructor.newInstance(ComponentSerializer.toString(page)));
        }

        //noinspection unchecked
        Map<String, Object> map = (Map<String, Object>) LegacyNBTEngine.NMS_Class_NBTTagCompound_Map_Field.get(nbtCompound);
        map.put("author", nbtAuthor);
        map.put("title", nbtTitle);
        map.put("pages", nbtPages);
        LegacyNBTEngine.NMS_Class_ItemStack_NBTTagCompound_Field.set(nmsItem, nbtCompound);
    }
}
