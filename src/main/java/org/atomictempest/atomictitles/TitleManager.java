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

        // Create default titles configuration if it doesn't exist
        if (!titlesFile.exists()) {
            createDefaultTitlesConfig();
        }

        titles.clear();

        // Load titles from the configuration file
        if (titlesConfig.isConfigurationSection("titles")) {
            ConfigurationSection titlesSection = titlesConfig.getConfigurationSection("titles");
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

        // Create default player titles configuration if it doesn't exist
        if (!playerTitlesFile.exists()) {
            createDefaultPlayerTitlesConfig();
        }

        grantedTitles.clear();
        lastUsedTitles.clear();
        playerColors.clear();

        // Load granted titles from the configuration file
        if (playerTitlesConfig.isConfigurationSection("granted_titles")) {
            ConfigurationSection grantedTitlesSection = playerTitlesConfig.getConfigurationSection("granted_titles");
            for (String uuidString : grantedTitlesSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                Set<String> playerTitles = new HashSet<>(grantedTitlesSection.getStringList(uuidString));
                grantedTitles.put(uuid, playerTitles);
            }
        }

        // Load last used titles from the configuration file
        if (playerTitlesConfig.isConfigurationSection("last_used_titles")) {
            ConfigurationSection lastUsedTitlesSection = playerTitlesConfig.getConfigurationSection("last_used_titles");
            for (String uuidString : lastUsedTitlesSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                String lastUsedTitle = lastUsedTitlesSection.getString(uuidString);
                lastUsedTitles.put(uuid, lastUsedTitle);
            }
        }

        // Load player colors from the configuration file
        if (playerTitlesConfig.isConfigurationSection("player_colors")) {
            ConfigurationSection playerColorsSection = playerTitlesConfig.getConfigurationSection("player_colors");
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
        Set<String> playerTitles = grantedTitles.computeIfAbsent(uuid, k -> new HashSet<>());
        playerTitles.add(name.toLowerCase());
        savePlayerTitlesConfig(); // Save granted titles to persistent storage
    }

    public void revokeTitle(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Set<String> playerTitles = grantedTitles.get(uuid);
        if (playerTitles != null) {
            playerTitles.remove(name.toLowerCase());
            savePlayerTitlesConfig(); // Save granted titles to persistent storage
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
        titlesConfig.set("titles", null);
        for (Map.Entry<String, Title> entry : titles.entrySet()) {
            String key = entry.getKey();
            Title title = entry.getValue();
            titlesConfig.set("titles." + key + ".prefix", title.getPrefix());
            titlesConfig.set("titles." + key + ".suffix", title.getSuffix());
        }

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
        playerTitlesConfig.set("granted_titles", null);
        for (Map.Entry<UUID, Set<String>> entry : grantedTitles.entrySet()) {
            UUID uuid = entry.getKey();
            Set<String> playerTitles = entry.getValue();
            playerTitlesConfig.set("granted_titles." + uuid.toString(), new ArrayList<>(playerTitles));
        }

        playerTitlesConfig.set("last_used_titles", null);
        for (Map.Entry<UUID, String> entry : lastUsedTitles.entrySet()) {
            UUID uuid = entry.getKey();
            String lastUsedTitle = entry.getValue();
            playerTitlesConfig.set("last_used_titles." + uuid.toString(), lastUsedTitle);
        }

        playerTitlesConfig.set("player_colors", null);
        for (Map.Entry<UUID, ChatColor> entry : playerColors.entrySet()) {
            UUID uuid = entry.getKey();
            ChatColor color = entry.getValue();
            playerTitlesConfig.set("player_colors." + uuid.toString(), color.name());
        }

        // Save the configuration to file
        try {
            playerTitlesConfig.save(playerTitlesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player_titles.yml!");
            e.printStackTrace();
        }
    }
}