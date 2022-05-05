package com.github.neapovil.motd;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.file.FileConfig;

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
    private FileConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("config.json", false);

        this.config = FileConfig.builder(new File(this.getDataFolder(), "config.json"))
                .autoreload()
                .autosave()
                .build();
        this.config.load();

        this.getServer().getPluginManager().registerEvents(this, this);

        new CommandAPICommand("motd")
                .withPermission("motd.command")
                .withArguments(new LiteralArgument("set"))
                .withArguments(new MultiLiteralArgument("string1", "string2"))
                .withArguments(new LiteralArgument("text"))
                .withArguments(new GreedyStringArgument("newtext"))
                .executes((sender, args) -> {
                    final String setting = (String) args[0];
                    final String newtext = (String) args[1];

                    this.config.set("config." + setting, newtext);

                    final Component component = Component.text("Motd " + setting + " changed to:\n")
                            .append(this.miniMessage.deserialize(newtext));

                    sender.sendMessage(component);
                })
                .register();

        new CommandAPICommand("motd")
                .withPermission("motd.command")
                .withArguments(new LiteralArgument("set"))
                .withArguments(new LiteralArgument("centered"))
                .withArguments(new BooleanArgument("bool"))
                .executes((sender, args) -> {
                    final boolean bool = (boolean) args[0];

                    this.config.set("config.centered", bool);

                    sender.sendMessage("Motd centered status changed to: " + bool);
                })
                .register();
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
        final List<Component> motd = List.of((String) this.config.get("config.string1"), (String) this.config.get("config.string2"))
                .stream()
                .map(s -> this.miniMessage.deserialize(s))
                .map(s -> (boolean) this.config.get("config.centered") ? s.replaceText(b -> {
                    b.match(((TextComponent) s).content())
                            .replacement((b1, c) -> Component.text(StringUtils.center(c.content(), 59)));
                }) : s)
                .toList();

        event.motd(Component.join(JoinConfiguration.separator(Component.text("\n")), motd));
    }
}
