package com.denisindenbom.discordauth.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.denisindenbom.discordauth.DiscordAuth;

public class CommandUtils
{
	public static boolean checkSenderPermissions(DiscordAuth plugin, CommandSender sender)
	{
		if (!(sender instanceof ConsoleCommandSender)) {
			if (sender instanceof Player player) {
				if (!plugin.getAuthManager().accountExists(player.getName())) {
					MessageSender.sendMessage(player, plugin.getMessagesConfig().getString("error.not_logged_in"));
					return false;
				}

				if (!player.isOp()) {
					MessageSender.sendMessage(player, plugin.getMessagesConfig().getString("error.permissions"));
					return false;
				}
			}
			else {
				return false;
			}
		}
		return true;
	}
}
