package com.meteor.bookapi;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the content of a {@link Material#WRITTEN_BOOK}.
 */
public class Book {
    final List<BaseComponent[]> pages;

    public Book() {
        this.pages = Lists.newArrayList();
    }

    public Book(@NotNull String... text) {
        this();
        for (String page : text) this.addPage(page);
    }

    public Book(@NotNull BaseComponent[]... text) {
        this();
        for (BaseComponent[] page : text) this.addPage(page);
    }

    /**
     * Adds new pages to the end of the book. The data can be up to 320 characters in length.
     *
     * @param text the String text to be added to the page
     * @return this object, for chaining
     */
    public Book addPage(@Nullable String text) {
        this.addPage(new TextComponent(ChatColor.translateAlternateColorCodes('&', this.validateText(text))));
        return this;
    }

    /**
     * Adds new pages to the end of the book. The data can be up to 320 characters in length.
     *
     * @param text the BaseComponent text to be added to the page
     * @return this object, for chaining
     */
    public Book addPage(@NotNull BaseComponent... text) {
        this.pages.add(text);
        return this;
    }

    /**
     * Sets the specified page in the book. The data can be up to 320 characters in length.
     *
     * @param page the page number to set
     * @param text the String text to be set for that page
     * @return this object, for chaining
     * @throws IndexOutOfBoundsException if the page number is invalid
     */
    public Book setPage(int page, @Nullable String text) throws IndexOutOfBoundsException {
        this.setPage(page, new TextComponent(ChatColor.translateAlternateColorCodes('&', this.validateText(text))));
        return this;
    }
    /**
     * Sets the specified page in the book. The data can be up to 320 characters in length.
     *
     * @param page the page number to set
     * @param text the BaseComponent text to be set for that page
     * @return this object, for chaining
     * @throws IndexOutOfBoundsException if the page number is invalid
     */
    public Book setPage(int page, @NotNull BaseComponent... text) throws IndexOutOfBoundsException {
        int index = page - 1;
        if (index < 0) throw new IndexOutOfBoundsException("Invalid page number " + page);
        while (this.pages.size() <= index) this.pages.add(new TextComponent[]{ new TextComponent() });
        this.pages.set(page, text);
        return this;
    }

    public @NotNull BaseComponent[] getPage(int page) {
        return this.pages.get(page);
    }

    public @NotNull List<BaseComponent[]> getPages() {
        return this.pages;
    }

    private String validateText(String text) {
        if (text == null) return "";
        else if (text.length() > 320) return text.substring(0, 320);
        else return text;
    }
}
