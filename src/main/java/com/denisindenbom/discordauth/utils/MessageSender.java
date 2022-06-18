package com.denisindenbom.discordauth.utils;

import org.bukkit.command.CommandSender;

public class MessageSender
{
    private final FormatText formatText = new FormatText();

    public void sendMessage(CommandSender sender, String message)
    {
        String newMessage = this.formatText.format(message);
        try
        {sender.sendMessage(newMessage);}
        catch (Exception ignored)
        {sender.getServer().getLogger().info(newMessage);}
    }

    public void sendMessage(CommandSender sender, String message, String target, String replacement)
    {
        String newMessage = this.formatText.format(message, target, replacement);

        try
        {sender.sendMessage(newMessage);}
        catch (Exception ignored)
        {sender.getServer().getLogger().info(newMessage);}
    }
}
