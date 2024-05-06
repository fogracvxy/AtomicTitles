package org.atomictempest.atomictitles.commands;

import org.atomictempest.atomictitles.AtomicTitles;
import org.atomictempest.atomictitles.Title;
import org.atomictempest.atomictitles.TitleManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class TitleCommandExecutor implements CommandExecutor {
    private final TitleManager titleManager;
    private final AtomicTitles plugin;

    public TitleCommandExecutor(AtomicTitles plugin, TitleManager titleManager) {
        this.plugin = plugin;
        this.titleManager = titleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                displayAvailableTitles(player);
                break;
            case "use":
                if (args.length < 2) {
                    player.sendMessage("Usage: /title use <title_name>");
                    return true;
                }
                String selectedTitle = args[1].toLowerCase();
                useTitle(player, selectedTitle);
                break;
            case "grant":
                if (args.length < 3) {
                    player.sendMessage("Usage: /title grant <title_name> <player_name>");
                    return true;
                }
                String titleName = args[1].toLowerCase();
                String targetPlayerName = args[2];
                grantTitleToPlayer(player, titleName, targetPlayerName);
                break;
            case "revoke":
                if (args.length < 3) {
                    player.sendMessage("Usage: /title revoke <title_name> <player_name>");
                    return true;
                }
                String revokeTitle = args[1].toLowerCase();
                String revokePlayerName = args[2];
                revokeTitleFromPlayer(player, revokeTitle, revokePlayerName);
                break;
            case "color":
                if (args.length < 2) {
                    player.sendMessage("Usage: /title color <color_name>");
                    player.sendMessage("Available colors: " + String.join(", ", getAllColorNames()));
                    return true;
                }
                String colorName = args[1].toUpperCase();
                ChatColor color;
                try {
                    color = ChatColor.valueOf(colorName);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid color name: " + colorName);
                    player.sendMessage("Available colors: " + String.join(", ", getAllColorNames()));
                    return true;
                }
                setPlayerColor(player, color);
                player.sendMessage("Your display name color has been set to " + color.name());
                break;
            default:
                return false;
        }

        return true;
    }

    private void displayAvailableTitles(Player player) {
        Set<String> grantedTitles = titleManager.getGrantedTitles(player);
        if (grantedTitles.isEmpty()) {
            player.sendMessage("You have no granted titles.");
        } else {
            player.sendMessage("Your granted titles:");
            for (String titleName : grantedTitles) {
                player.sendMessage("- " + titleName);
            }
        }
    }

    private void useTitle(Player player, String titleName) {
        Set<String> grantedTitles = titleManager.getGrantedTitles(player);
        if (grantedTitles.contains(titleName)) {
            Title title = titleManager.getTitle(titleName);
            if (title != null) {
                String prefix = ChatColor.translateAlternateColorCodes('&', title.getPrefix());
                player.setDisplayName(prefix + " " + player.getName());
                player.sendMessage("Title set to: " + ChatColor.stripColor(prefix) + titleName);
                titleManager.setLastUsedTitle(player, titleName);
            }
        } else {
            player.sendMessage("You do not have the title: " + titleName);
        }
    }

    private void grantTitleToPlayer(Player player, String titleName, String targetPlayerName) {
        // Check if the player has the permission to grant titles
        if (!player.hasPermission("atomictitles.grant")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to grant titles.");
            return;
        }

        Player targetPlayer = player.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage("Player not found: " + targetPlayerName);
            return;
        }

        titleManager.grantTitle(targetPlayer, titleName);
        player.sendMessage("Title granted: " + titleName + " to " + targetPlayerName);
    }

    private void revokeTitleFromPlayer(Player player, String titleName, String targetPlayerName) {
        // Check if the player has the permission to revoke titles
        if (!player.hasPermission("atomictitles.revoke")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to revoke titles.");
            return;
        }

        Player targetPlayer = player.getServer().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage("Player not found: " + targetPlayerName);
            return;
        }

        titleManager.revokeTitle(targetPlayer, titleName);
        player.sendMessage("Title revoked: " + titleName + " from " + targetPlayerName);
    }

    private void setPlayerColor(Player player, ChatColor color) {
        titleManager.setPlayerColor(player, color);
        updatePlayerDisplayName(player);
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

    private Set<String> getAllColorNames() {
        Set<String> colorNames = new HashSet<>();
        for (ChatColor color : ChatColor.values()) {
            if (color.isColor()) {
                colorNames.add(color.name());
            }
        }
        return colorNames;
    }
}