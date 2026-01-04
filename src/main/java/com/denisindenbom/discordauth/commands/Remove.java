package com.denisindenbom.discordauth.commands;

import com.denisindenbom.discordauth.utils.CommandUtils;
import com.denisindenbom.discordauth.utils.FormatText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.denisindenbom.discordauth.DiscordAuth;
import com.denisindenbom.discordauth.utils.MessageSender;

import org.jetbrains.annotations.NotNull;

public class Remove implements CommandExecutor
{
	private final DiscordAuth plugin;
	private final FileConfiguration messages;

	public Remove(@NotNull DiscordAuth plugin)
	{
		this.plugin = plugin;
		this.messages = plugin.getMessagesConfig();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args)
	{
		if (!CommandUtils.checkSenderPermissions(plugin, sender)) {
			return true;
		}

		if (args.length < 1) {
			return false;
		}

		String username = args[0];

		boolean result = this.plugin.getAuthDB().removeAccount(username);

		if (!result) {
			MessageSender.sendMessage(sender, this.messages.getString("error.user_not_exist"), "{%username%}",
			                          username);
			return true;
		}

		Player player = this.plugin.getServer().getPlayer(username);

		if (player != null && player.isOnline()) {
			player.kickPlayer(FormatText.format(this.messages.getString("error.not_authorized")));
		}

		this.plugin.getAuthManager().removeAccountByName(username);

		MessageSender.sendMessage(sender, this.messages.getString("remove_user.user_removed"), "{%username%}",
		                          username);

		return true;
	}
}
