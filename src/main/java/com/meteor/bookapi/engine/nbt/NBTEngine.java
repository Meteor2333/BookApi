package com.meteor.bookapi.engine.nbt;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.List;

public interface NBTEngine {
    void setBookNBT(Object nmsItem, String author, String title, List<BaseComponent[]> pages) throws Exception;
}
