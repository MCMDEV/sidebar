# Description
A library for scoreboard sidebars using the new adventure components

# Download
Since the project consists of only a few classes and is considered done, the easiest way to use it is probably to just copy and paste the source files into your project.
Otherwise, you could use a service like Jitpack to get a Maven dependency from this repo.

# Example code
```java
package de.mcmdev.sidebar;

import de.mcmdev.sidebar.renderer.BukkitSidebarRenderer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestPlugin extends JavaPlugin implements Listener {

    private Sidebar sidebar;

    @Override
    public void onEnable() {
        sidebar = new Sidebar(new BukkitSidebarRenderer());
        sidebar.title(player -> Component.text("Title"));
        sidebar.lines(player -> {
            List<Component> components = new ArrayList<>();
            for (int i = 0; i < ThreadLocalRandom.current().nextInt(5); i++) {
                components.add(Component.text("Line " + i));
            }
            return components;
        });

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            sidebar.render();
        }, 20, 20);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)   {
        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        sidebar.add(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)   {
        sidebar.remove(event.getPlayer());
    }

}

```
