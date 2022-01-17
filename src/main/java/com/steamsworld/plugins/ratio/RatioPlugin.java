package com.steamsworld.plugins.ratio;

import com.steamsworld.plugins.ratio.command.RatioCommand;
import com.steamsworld.plugins.ratio.ratio.RatioHandler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class RatioPlugin extends JavaPlugin {

    @Getter
    private RatioHandler ratioHandler;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        ratioHandler = new RatioHandler();
        new RatioCommand(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.getConfig().getString("messages." + path)));
    }
}
