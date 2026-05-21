package com.denisindenbom.discordauth.discord;

import com.denisindenbom.discordauth.DiscordAuth;
import com.denisindenbom.discordauth.units.Account;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class DiscordCommandsHandler extends ListenerAdapter
{
	private static final String COMMAND_PREFIX = "!";

	private final DiscordAuth plugin;
	private final String allowedChannelId;
	private final int maxNumOfAccounts;
	private final FileConfiguration messages;

	public DiscordCommandsHandler(@NotNull DiscordAuth plugin)
	{
		this.plugin = plugin;
		this.allowedChannelId = plugin.getConfig().getString("channel-id");
		this.maxNumOfAccounts = plugin.getConfig().getInt("max-num-of-accounts");
		this.messages = plugin.getMessagesConfig();
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event)
	{
		String content = event.getMessage().getContentDisplay();
		String channelId = event.getChannel().getId();

		if (!isValidCommand(content, channelId)) {
			return;
		}

		String[] args = content.split("\\s+");
		String command = args[0].toLowerCase();

		switch (command) {
		case "!add" -> handleAdd(event, args);
		case "!delete" -> handleDelete(event, args);
		case "!help" -> handleHelp(event);
		}
	}

	private boolean isValidCommand(String message, @NotNull String channelId)
	{
		return channelId.equals(allowedChannelId) && message.startsWith(COMMAND_PREFIX);
	}

	private void handleAdd(@NotNull MessageReceivedEvent event, String[] args)
	{
		String discordId = event.getAuthor().getId();

		if (plugin.getAuthDB().countAccountsByDiscordId(discordId) >= maxNumOfAccounts) {
			sendError("bot_error.enough_accounts", event);
			return;
		}

		if (args.length < 2) {
			sendError("bot_error.name_no_set", event);
			return;
		}

		boolean added = plugin.getAuthDB().addAccount(new Account(args[1], discordId));

		if (!added) {
			sendError("bot_error.user_exists", event);
			return;
		}

		sendSuccess("bot.verification_successful", event);
	}

	private void handleDelete(MessageReceivedEvent event, String[] args)
	{
		if (!plugin.getConfig().getBoolean("allow-delete-accounts")) {
			sendError("bot_error.account_deletion_is_not_allowed", event);
			return;
		}

		if (args.length < 2) {
			sendError("bot_error.name_no_set", event);
			return;
		}

		Account account = plugin.getAuthDB().getAccount(args[1]);

		if (account == null) {
			sendError("bot_error.account_not_exits", event);
			return;
		}

		if (!account.discordId().equals(event.getAuthor().getId())) {
			sendError("bot_error.account_owner", event);
			return;
		}

		if (!plugin.getAuthDB().removeAccount(account.name())) {
			sendError("bot_error.not_expected_error", event);
			return;
		}

		sendSuccess("bot.deletion_successful", event);
	}

	private void handleHelp(@NotNull MessageReceivedEvent event)
	{
		plugin.getBot().sendInfo(messages.getString("bot.help"), "Commands", event.getChannel());
	}

	private void sendError(String key, @NotNull MessageReceivedEvent event)
	{
		plugin.getBot().sendError(messages.getString(key), event.getChannel());
	}

	private void sendSuccess(String key, @NotNull MessageReceivedEvent event)
	{
		plugin.getBot().sendSuccessful(messages.getString(key), event.getChannel());
	}
}
