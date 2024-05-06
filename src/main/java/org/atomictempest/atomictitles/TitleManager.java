package org.atomictempest.atomictitles;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TitleManager {
    private final JavaPlugin plugin;
    private final Map<String, Title> titles;
    private final Map<UUID, Set<String>> grantedTitles;
    private final Map<UUID, String> lastUsedTitles;
    private final Map<UUID, ChatColor> playerColors;
    private FileConfiguration titlesConfig;
    private File titlesFile;
    private FileConfiguration playerTitlesConfig;
    private File playerTitlesFile;

    public TitleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.titles = new HashMap<>();
        this.grantedTitles = new HashMap<>();
        this.lastUsedTitles = new HashMap<>();
        this.playerColors = new HashMap<>();
        this.titlesFile = new File(plugin.getDataFolder(), "titles.yml");
        this.playerTitlesFile = new File(plugin.getDataFolder(), "player_titles.yml");

        // Ensure the data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        // Load titles from configuration
        loadTitles();
        loadPlayerTitles();
    }

    private void loadTitles() {
        titlesConfig = YamlConfiguration.loadConfiguration(titlesFile);

        // Create default titles configuration if it doesn't exist or is empty
        if (!titlesFile.exists() || titlesConfig.getConfigurationSection("titles") == null) {
            createDefaultTitlesConfig();
            // Reload configuration after creating defaults
            titlesConfig = YamlConfiguration.loadConfiguration(titlesFile);
        }

        titles.clear();

        // Load titles from the configuration file
        ConfigurationSection titlesSection = titlesConfig.getConfigurationSection("titles");
        if (titlesSection != null) {
            for (String key : titlesSection.getKeys(false)) {
                String prefix = titlesSection.getString(key + ".prefix");
                String suffix = titlesSection.getString(key + ".suffix");
                Title title = new Title(key, prefix, suffix);
                titles.put(key.toLowerCase(), title);
            }
        }
    }

    private void loadPlayerTitles() {
        playerTitlesConfig = YamlConfiguration.loadConfiguration(playerTitlesFile);

        // Create default player titles configuration if it doesn't exist or is empty
        if (!playerTitlesFile.exists()) {
            createDefaultPlayerTitlesConfig();
            // Reload configuration after creating defaults
            playerTitlesConfig = YamlConfiguration.loadConfiguration(playerTitlesFile);
        }

        grantedTitles.clear();
        lastUsedTitles.clear();
        playerColors.clear();

        // Load granted titles from the configuration file
        ConfigurationSection grantedTitlesSection = playerTitlesConfig.getConfigurationSection("granted_titles");
        if (grantedTitlesSection != null) {
            for (String uuidString : grantedTitlesSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                Set<String> playerTitles = new HashSet<>(grantedTitlesSection.getStringList(uuidString));
                grantedTitles.put(uuid, playerTitles);
            }
        }

        // Load last used titles from the configuration file
        ConfigurationSection lastUsedTitlesSection = playerTitlesConfig.getConfigurationSection("last_used_titles");
        if (lastUsedTitlesSection != null) {
            for (String uuidString : lastUsedTitlesSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                String lastUsedTitle = lastUsedTitlesSection.getString(uuidString);
                lastUsedTitles.put(uuid, lastUsedTitle);
            }
        }

        // Load player colors from the configuration file
        ConfigurationSection playerColorsSection = playerTitlesConfig.getConfigurationSection("player_colors");
        if (playerColorsSection != null) {
            for (String uuidString : playerColorsSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                String colorString = playerColorsSection.getString(uuidString);
                ChatColor color = ChatColor.valueOf(colorString);
                playerColors.put(uuid, color);
            }
        }
    }

    private void createDefaultTitlesConfig() {
        titlesConfig.set("titles.warrior.prefix", "&4[Warrior] ");
        titlesConfig.set("titles.warrior.suffix", "");
        titlesConfig.set("titles.moody.prefix", "&b[Moody] ");
        titlesConfig.set("titles.moody.suffix", "");
        titlesConfig.set("titles.king.prefix", "&6[King] ");
        titlesConfig.set("titles.king.suffix", " &6[Ruler]");

        // Save the default configuration to the file
        saveTitlesConfig();
    }
    public void setPlayerPreferredColor(Player player, ChatColor color) {
        UUID uuid = player.getUniqueId();
        playerColors.put(uuid, color);
        savePlayerTitlesConfig(); // Save player colors to persistent storage
    }

    public ChatColor getPlayerPreferredColor(Player player) {
        UUID uuid = player.getUniqueId();
        return playerColors.getOrDefault(uuid, ChatColor.WHITE);
    }
    private void createDefaultPlayerTitlesConfig() {
        // Save the default configuration to the file
        savePlayerTitlesConfig();
    }

    public void addTitle(String name, String prefix, String suffix) {
        Title title = new Title(name, prefix, suffix);
        titles.put(name.toLowerCase(), title);
        saveTitlesConfig(); // Save titles after adding a new one
    }

    public void removeTitle(String name) {
        titles.remove(name.toLowerCase());
        saveTitlesConfig(); // Save titles after removing one
    }

    public Map<String, Title> getTitles() {
        return titles;
    }

    public void grantTitle(Player player, String name) {
        UUID uuid = player.getUniqueId();
        if (titles.containsKey(name.toLowerCase())) {
            Set<String> playerTitles = grantedTitles.computeIfAbsent(uuid, k -> new HashSet<>());
            playerTitles.add(name.toLowerCase());
            savePlayerTitlesConfig(); // Save granted titles to persistent storage
        } else {
            player.sendMessage(ChatColor.RED + "Title not found: " + name);
        }
    }

    public void revokeTitle(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Set<String> playerTitles = grantedTitles.get(uuid);
        if (playerTitles != null && playerTitles.remove(name.toLowerCase())) {
            // If the title was successfully removed from the player's granted titles
            savePlayerTitlesConfig(); // Save granted titles to persistent storage

            // Check and update lastUsedTitle if necessary
            String lastUsedTitle = lastUsedTitles.get(uuid);
            if (lastUsedTitle != null && lastUsedTitle.equalsIgnoreCase(name.toLowerCase())) {
                setLastUsedTitle(player, ""); // Clear last used title for the player
            }
        }
    }

    public Set<String> getGrantedTitles(Player player) {
        UUID uuid = player.getUniqueId();
        return grantedTitles.getOrDefault(uuid, Collections.emptySet());
    }

    public Title getTitle(String name) {
        return titles.get(name.toLowerCase());
    }

    public List<String> getAvailableTitles() {
        return new ArrayList<>(titles.keySet());
    }

    public void setLastUsedTitle(Player player, String title) {
        UUID uuid = player.getUniqueId();
        lastUsedTitles.put(uuid, title.toLowerCase());
        savePlayerTitlesConfig(); // Save last used title to persistent storage
    }

    public String getLastUsedTitle(Player player) {
        UUID uuid = player.getUniqueId();
        return lastUsedTitles.getOrDefault(uuid, "");
    }

    private void saveTitlesConfig() {
        // Save the configuration to file
        try {
            titlesConfig.save(titlesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save titles.yml!");
            e.printStackTrace();
        }
    }

    public void setPlayerColor(Player player, ChatColor color) {
        UUID uuid = player.getUniqueId();
        playerColors.put(uuid, color);
        savePlayerTitlesConfig();
    }

    public ChatColor getPlayerColor(Player player) {
        UUID uuid = player.getUniqueId();
        return playerColors.getOrDefault(uuid, ChatColor.WHITE);
    }

    private void savePlayerTitlesConfig() {
        try {
            playerTitlesConfig.set("granted_titles", null); // Clear existing data
            grantedTitles.forEach((uuid, titles) -> {
                List<String> titleList = new ArrayList<>(titles);
                playerTitlesConfig.set("granted_titles." + uuid.toString(), titleList);
            });

            // Save last used titles
            lastUsedTitles.forEach((uuid, title) -> {
                playerTitlesConfig.set("last_used_titles." + uuid.toString(), title);
            });

            // Save player colors
            playerColors.forEach((uuid, color) -> {
                playerTitlesConfig.set("player_colors." + uuid.toString(), color.name());
            });

            playerTitlesConfig.save(playerTitlesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player_titles.yml!");
            e.printStackTrace();
        }
    }

    public void updateAndSavePlayerTitles() {
        savePlayerTitlesConfig();
    }
}
