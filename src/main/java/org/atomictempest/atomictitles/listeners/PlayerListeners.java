package org.atomictempest.atomictitles.listeners;

import org.atomictempest.atomictitles.Title;
import org.atomictempest.atomictitles.TitleManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class PlayerListeners implements Listener {
    private final TitleManager titleManager;
    private final JavaPlugin plugin;

    public PlayerListeners(JavaPlugin plugin, TitleManager titleManager) {
        this.plugin = plugin;
        this.titleManager = titleManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Reset player's display name to default
        resetPlayerDisplayName(player);

        // Update player's display name with granted titles
        updatePlayerDisplayName(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Reset player's display name to default
        resetPlayerDisplayName(player);
    }

    private void updatePlayerDisplayName(Player player) {
        String lastUsedTitle = titleManager.getLastUsedTitle(player);
        Title title = titleManager.getTitle(lastUsedTitle);
        ChatColor playerColor = titleManager.getPlayerColor(player);

        if (title != null) {
            String prefix = ChatColor.translateAlternateColorCodes('&', title.getPrefix());
            String suffix = ChatColor.translateAlternateColorCodes('&', title.getSuffix());
            String displayName = prefix + playerColor + player.getName() + suffix;
            player.setDisplayName(displayName);
        } else {
            String displayName = playerColor + player.getName();
            player.setDisplayName(displayName);
        }
    }

    private void resetPlayerDisplayName(Player player) {
        // Reset player's display name to default (just the player's name)
        player.setDisplayName(player.getName());
    }
}
