package com.denisindenbom.discordauth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.denisindenbom.discordauth.DiscordAuth;
import com.denisindenbom.discordauth.utils.MessageSender;
import com.denisindenbom.discordauth.utils.CommandUtils;

import org.jetbrains.annotations.NotNull;

public class Reload implements CommandExecutor
{
	private final DiscordAuth plugin;

	public Reload(DiscordAuth plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args)
	{
		if (!CommandUtils.checkSenderPermissions(plugin, sender)) {
			return true;
		}

		this.plugin.reloadPlugin();

		MessageSender.sendMessage(sender, "<c5>DiscordAuth<cf> is reload!");

		return true;
	}
}
