package org.atomictempest.atomictitles;

import org.atomictempest.atomictitles.commands.TitleCommandExecutor;
import org.atomictempest.atomictitles.listeners.PlayerListeners;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class AtomicTitles extends JavaPlugin {
    private TitleManager titleManager;
    private Logger logger; // Logger instance for plugin
    @Override
    public void onEnable() {
        logger = getLogger();

        logger.info("AtomicTitles are starting!");

        // Initialize TitleManager
        titleManager = new TitleManager(this);

        // Register TitleCommandExecutor for the "title" command
        getCommand("title").setExecutor(new TitleCommandExecutor(this, titleManager));


        // Register PlayerListeners
        new PlayerListeners(this, titleManager);


    }

    @Override
    public void onDisable() {
        logger.info("AtomicTitles are shutting down!");

        // Perform any cleanup tasks if needed
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }
}

