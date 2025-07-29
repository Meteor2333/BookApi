package cc.meteormc.bookapi.engine.nbt;

import cc.meteormc.bookapi.ReflectionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

public class LegacyNBTEngine implements NBTEngine {
    private static final Constructor<?> NMS_NBTTAGCOMPOUND_CONSTRUCTOR;
    private static final Constructor<?> NMS_NBTTAGLIST_CONSTRUCTOR;
    private static final Constructor<?> NMS_NBTTAGSTRING_CONSTRUCTOR;
    private static final Method NMS_NBTTAGLIST_ADD_METHOD;
    private static final Method NMS_NBTTAGCOMPOUND_PUT_METHOD;
    private static final Field NMS_ITEMSTACK_NBTTAGCOMPOUND_FIELD;

    static {
        try {
            Class<?> isClass = ReflectionUtil.getNMSClass("ItemStack", "net.minecraft.world.item");
            Class<?> nbClass = ReflectionUtil.getNMSClass("NBTBase", "net.minecraft.nbt");
            Class<?> ntcClass = ReflectionUtil.getNMSClass("NBTTagCompound", "net.minecraft.nbt");
            Class<?> ntlClass = ReflectionUtil.getNMSClass("NBTTagList", "net.minecraft.nbt");
            NMS_NBTTAGCOMPOUND_CONSTRUCTOR = ntcClass.getDeclaredConstructor();
            NMS_NBTTAGLIST_CONSTRUCTOR = ntlClass.getDeclaredConstructor();
            NMS_NBTTAGSTRING_CONSTRUCTOR = ReflectionUtil.getNMSClass("NBTTagString", "net.minecraft.nbt").getDeclaredConstructor(String.class);

            Method method;
            try {
                method = ntlClass.getDeclaredMethod("add", nbClass);
            } catch (NoSuchMethodException e) {
                method = ntlClass.getMethod("add", Object.class);
            } NMS_NBTTAGLIST_ADD_METHOD = Objects.requireNonNull(method);

            method = null;
            for (Method pMethod : ntcClass.getDeclaredMethods()) {
                if (Modifier.isStatic(pMethod.getModifiers())) continue;
                Class<?>[] params = pMethod.getParameterTypes();
                if (params.length == 2 && params[0] == String.class && params[1] == nbClass) {
                    method = pMethod;
                    break;
                }
            } NMS_NBTTAGCOMPOUND_PUT_METHOD = Objects.requireNonNull(method);

            Field field = null;
            for (Field tField : isClass.getDeclaredFields()) {
                if (Modifier.isStatic(tField.getModifiers())) continue;
                if (tField.getType() == ntcClass) {
                    field = tField;
                    break;
                }
            } NMS_ITEMSTACK_NBTTAGCOMPOUND_FIELD = Objects.requireNonNull(field);

            NMS_NBTTAGCOMPOUND_CONSTRUCTOR.setAccessible(true);
            NMS_NBTTAGLIST_CONSTRUCTOR.setAccessible(true);
            NMS_NBTTAGSTRING_CONSTRUCTOR.setAccessible(true);
            NMS_NBTTAGLIST_ADD_METHOD.setAccessible(true);
            NMS_NBTTAGCOMPOUND_PUT_METHOD.setAccessible(true);
            NMS_ITEMSTACK_NBTTAGCOMPOUND_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setBookNBT(Object nmsItem, String author, String title, List<BaseComponent[]> pages) throws Exception {
        Object nbtAuthor = NMS_NBTTAGSTRING_CONSTRUCTOR.newInstance(author);
        Object nbtTitle = NMS_NBTTAGSTRING_CONSTRUCTOR.newInstance(title);
        Object nbtPages = NMS_NBTTAGLIST_CONSTRUCTOR.newInstance();
        Object nbtCompound = NMS_NBTTAGCOMPOUND_CONSTRUCTOR.newInstance();
        for (BaseComponent[] page : pages) {
            NMS_NBTTAGLIST_ADD_METHOD.invoke(nbtPages, NMS_NBTTAGSTRING_CONSTRUCTOR.newInstance(ComponentSerializer.toString(page)));
        }

        NMS_NBTTAGCOMPOUND_PUT_METHOD.invoke(nbtCompound, "author", nbtAuthor);
        NMS_NBTTAGCOMPOUND_PUT_METHOD.invoke(nbtCompound, "title", nbtTitle);
        NMS_NBTTAGCOMPOUND_PUT_METHOD.invoke(nbtCompound, "pages", nbtPages);
        NMS_ITEMSTACK_NBTTAGCOMPOUND_FIELD.set(nmsItem, nbtCompound);
    }
}
