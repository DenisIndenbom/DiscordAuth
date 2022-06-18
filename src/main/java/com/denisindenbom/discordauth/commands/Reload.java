package com.denisindenbom.discordauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.denisindenbom.discordauth.DiscordAuth;
import com.denisindenbom.discordauth.utils.MessageSender;

import org.jetbrains.annotations.NotNull;

public class Reload implements CommandExecutor
{
    private final DiscordAuth plugin;
    private final FileConfiguration messages;

    private final MessageSender messageSender = new MessageSender();

    public Reload(DiscordAuth plugin, FileConfiguration messages)
    {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args)
    {
        if (!(sender instanceof ConsoleCommandSender))
        {
            if (sender instanceof Player)
            {
                // check that the player is logged in
                if (!this.plugin.getAuthManager().accountExists(sender.getName()))
                {
                    this.messageSender.sendMessage(sender, this.messages.getString("error.not_logged_in"));
                    return true;
                }

                if (!sender.isOp())
                {
                    this.messageSender.sendMessage(sender, this.messages.getString("error.permissions"));
                    return true;
                }
            }
            else return true;
        }

        this.plugin.reloadPlugin();

        this.messageSender.sendMessage(sender, "<c5>DiscordAuth<cf> is reload!");

        return true;
    }
}
