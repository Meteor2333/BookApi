# BookAPI

[![Java](https://img.shields.io/badge/Java-8-blue)](https://shields.io)
[![JitPack](https://img.shields.io/badge/JitPack-v1.0.1-brightgreen?logo=jitpack)](https://jitpack.io/#Meteor2333/BookApi)

### Introduction

The BookAPI is a lightweight and easy-to-use library designed for Spigot plugins in Minecraft. It allows developers to create and display custom books to players.

This BookAPI is particularly useful for plugins that need to present information in a book format, such as tutorials, guides, or interactive menus.

Release announcements for new versions will be posted on [SpigotMC](https://www.spigotmc.org).

### How It Works

In Minecraft, the logic for opening a book is primarily handled on the client side. The server can only send a packet to open the book in the player's main hand, but this packet cannot contain the book's data. To work around this limitation, the BookAPI follows these steps:

1. **Set a custom book with the required data** to the player's main hand in the form of a fake item.
2. **Send a packet** to the client to open the book.
3. **Update the player's inventory** to restore the main hand to the original item.

This approach ensures that the player's inventory remains unchanged while still allowing them to interact with the custom book.

### Usage

To use the BookAPI in your Spigot plugin, you need to add it as a dependency in your project. Below are the instructions for both Maven and Gradle.

#### Maven

Add the following repository and dependency to your `pom.xml`:

```
<repositories>
    <repository>
        <id>jitpack.io</id>
	<url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Meteor2333</groupId>
        <artifactId>BookApi</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

#### Gradle

Add the following repository and dependency to your `build.gradle`:

```
repositories {
    maven { url 'https://jitpack.io/' }
}

dependencies {
    implementation 'com.github.Meteor2333:BookApi:1.0.1'
}
```

#### Shading

Do not extract the JAR into your project. You have to shade the library, otherwise your plugin or other plugins will break due to version mismatch. To shade the library, add the following under your maven plugins:

[Apache Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/)
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.0</version>
    <configuration>
        <relocations>
            <relocation>
                <pattern>com.meteor.bookapi</pattern>
                <shadedPattern>myplugin.libs.bookapi</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

[Gradle Shadow plugin](https://imperceptiblethoughts.com/shadow/)
```
plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

tasks {
    shadowJar {
        relocate("com.meteor.bookapi", "myplugin.libs.bookapi")
    }
}
```

#### Example

Using BookAPI is very simple. Here are some examples:

```
// Create a new Book instance
Book book = new Book();

// Set the content of the first page using String.
book.addPage("This is the first page of the book.");
// Or...
book.setPage(1, "This is the first page of the book.");

TextComponent text = new TextComponent("This is the second page of the book.");
text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/threads/bookapi.683632/"));
text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Cilck to open URL").create()));
// Set the content of the second page using BaseComponent, and this method supports multiple components.
book.addPage(text, text);
// Or...
book.setPage(2, text, text);

// If you want to get the content of the book, you can use
BaseComponent[] page = book.getPage(index);
// Or...
List<BaseComponent[]> pages = book.getPages();

// Open the book for a Player
BookApi.openBook(player, book);
// Or for Players...
BookApi.openBook(players, book);
```

### Conclusion

The BookAPI is a simple yet powerful tool for Spigot plugin developers who need to display custom books to players. If you have any suggestions, ideas, or encounter any bugs, please feel free to open an issue on the GitHub repository. Your feedback is highly appreciated!
