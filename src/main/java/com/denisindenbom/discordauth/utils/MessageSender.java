package com.denisindenbom.discordauth.utils;

import org.bukkit.command.CommandSender;

public class MessageSender
{
	public static void sendMessage(CommandSender sender, String message)
	{
		String newMessage = FormatText.format(message);
		try {
			sender.sendMessage(newMessage);
		}
		catch (Exception ignored) {
			sender.getServer().getLogger().info(newMessage);
		}
	}

	public static void sendMessage(CommandSender sender, String message, String target, String replacement)
	{
		String newMessage = FormatText.format(message, target, replacement);

		try {
			sender.sendMessage(newMessage);
		}
		catch (Exception ignored) {
			sender.getServer().getLogger().info(newMessage);
		}
	}
}
