package com.meteor.bookapi;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import java.util.List;

public class Book {
    final List<BaseComponent[]> pages;

    public Book(@Nonnull String... text) {
        this.pages = Lists.newArrayList();
        for (String page : text) this.addPage(page);
    }

    public Book addPage(@Nonnull String text) {
        this.addPage(new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
        return this;
    }

    public Book addPage(@Nonnull BaseComponent... text) {
        this.pages.add(text);
        return this;
    }

    public Book setPage(int page, @Nonnull String text) {
        this.setPage(page, new TextComponent(ChatColor.translateAlternateColorCodes('&', text)));
        return this;
    }

    public Book setPage(int page, @Nonnull BaseComponent... text) {
        int index = page - 1;
        if (index < 0) throw new IndexOutOfBoundsException("Invalid page number " + page);
        while (this.pages.size() <= index) this.pages.add(new TextComponent[]{ new TextComponent() });
        this.pages.set(page, text);
        return this;
    }

    public BaseComponent[] getPage(int page) {
        return this.pages.get(page);
    }

    public List<BaseComponent[]> getPages() {
        return this.pages;
    }
}
