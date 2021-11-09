package com.github.neapovil.motd;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.file.FileConfig;

import net.md_5.bungee.api.ChatColor;

public final class Motd extends JavaPlugin implements Listener
{
    private static Motd instance;
    private FileConfig config;

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.toml", false);

        this.config = FileConfig.builder(new File(this.getDataFolder(), "config.toml"))
                .autoreload()
                .autosave()
                .sync()
                .build();
        this.config.load();

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
    }

    public static Motd getInstance()
    {
        return instance;
    }

    @EventHandler
    private void serverListPing(ServerListPingEvent event)
    {
        final List<String> motd = List.of((String) config.get("motd.string1"), (String) config.get("motd.string2"))
                .stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .map(s -> (boolean) config.get("motd.center") ? StringUtils.center(s, 59) : s)
                .toList();

        event.setMotd(String.join("\n", motd));
    }
}
