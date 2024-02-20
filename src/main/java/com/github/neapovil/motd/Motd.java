package com.github.neapovil.motd;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Motd extends JavaPlugin implements Listener
{
    private static Motd instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private Config config;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.json", false);

        this.load();

        this.getServer().getPluginManager().registerEvents(this, this);

        new CommandAPICommand("motd")
                .withPermission("motd.command")
                .withArguments(new LiteralArgument("set"))
                .withArguments(new MultiLiteralArgument("setting", "string1", "string2"))
                .withArguments(new GreedyStringArgument("newtext"))
                .executes((sender, args) -> {
                    final String setting = (String) args.get("setting");
                    final String newtext = (String) args.get("newtext");

                    if (setting.equals("string1"))
                    {
                        this.config.string1 = newtext;
                    }

                    if (setting.equals("string2"))
                    {
                        this.config.string2 = newtext;
                    }

                    try
                    {
                        this.save();
                        sender.sendRichMessage("Motd " + setting + " text changed to: " + newtext);
                    }
                    catch (IOException e)
                    {
                        this.getLogger().severe(e.getMessage());
                        throw CommandAPI.failWithString("Unable to save.");
                    }
                })
                .register();

        new CommandAPICommand("motd")
                .withPermission("motd.command")
                .withArguments(new LiteralArgument("set"))
                .withArguments(new LiteralArgument("centered"))
                .withArguments(new BooleanArgument("bool"))
                .executes((sender, args) -> {
                    final boolean bool = (boolean) args.get("bool");

                    try
                    {
                        this.save();
                        sender.sendMessage("Motd centered status changed to: " + bool);
                    }
                    catch (IOException e)
                    {
                        this.getLogger().severe(e.getMessage());
                        throw CommandAPI.failWithString("Unable to save.");
                    }
                })
                .register();
    }

    @Override
    public void onDisable()
    {
    }

    public static Motd instance()
    {
        return instance;
    }

    private void load()
    {
        try
        {
            final String string = Files.readString(this.getDataFolder().toPath().resolve("config.json"));
            this.config = this.gson.fromJson(string, Config.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void save() throws IOException
    {
        final String string = this.gson.toJson(this.config);
        Files.write(this.getDataFolder().toPath().resolve("config.json"), string.getBytes());
    }

    @EventHandler
    private void serverListPing(ServerListPingEvent event)
    {
        final List<Component> motd = List.of(this.config.string1, this.config.string2)
                .stream()
                .map(s -> this.miniMessage.deserialize(s))
                .map(s -> this.config.centered ? s.replaceText(b -> {
                    b.match(((TextComponent) s).content())
                            .replacement((b1, c) -> Component.text(StringUtils.center(c.content(), 59)));
                }) : s)
                .toList();

        event.motd(Component.join(JoinConfiguration.separator(Component.text("\n")), motd));
    }

    class Config
    {
        public boolean centered;
        public String string1;
        public String string2;
    }
}
