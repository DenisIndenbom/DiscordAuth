package com.denisindenbom.discordauth.commands;

import com.denisindenbom.discordauth.utils.FormatText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.denisindenbom.discordauth.DiscordAuth;
import com.denisindenbom.discordauth.utils.MessageSender;

import org.jetbrains.annotations.NotNull;

public class Remove implements CommandExecutor
{
    private final DiscordAuth plugin;
    private final FileConfiguration messages;

    private final MessageSender messageSender = new MessageSender();

    public Remove(DiscordAuth plugin, FileConfiguration messages)
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

        if (args.length < 1) return false;
        
        String username = args[0];
        
        boolean result = this.plugin.getAuthDB().removeAccount(username);

        if (!result)
        {
            this.messageSender.sendMessage(sender, this.messages.getString("error.user_not_exist"), "{%username%}", username);
            return true;
        }

        Player player = this.plugin.getServer().getPlayer(username);

        if (player != null && player.isOnline())
            player.kickPlayer(new FormatText().format(this.messages.getString("error.not_authorized")));

        this.plugin.getAuthManager().removeAccountByName(username);

        this.messageSender.sendMessage(sender, this.messages.getString("remove_user.user_removed"), "{%username%}", username);

        return true;
    }
}
