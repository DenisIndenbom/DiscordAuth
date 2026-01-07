package com.denisindenbom.discordauth.discord;

import com.denisindenbom.discordauth.DiscordAuth;

import com.denisindenbom.discordauth.units.Account;
import com.denisindenbom.discordauth.units.LoginConfirmationRequest;
import com.denisindenbom.discordauth.utils.MessageSender;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class LoginConfirmationHandler extends ListenerAdapter
{
	private final DiscordAuth plugin;

	private final FileConfiguration messagesConfig;

	public LoginConfirmationHandler(@NotNull DiscordAuth plugin)
	{
		this.plugin = plugin;

		this.messagesConfig = plugin.getMessagesConfig();
	}

	public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event)
	{
		String messageId = event.getMessageId();

		// check that user is not null
		if (event.getUser() == null || event.getUser().isBot()) {
			return;
		}
		// check that channel is private
		if (event.getChannelType() != ChannelType.PRIVATE) {
			return;
		}
		// check that reaction is correct
		if (!Emoji.fromUnicode("U+2705").equals(event.getEmoji())) {
			return;
		}

		// get login confirmation request
		LoginConfirmationRequest request = this.plugin.getLoginConfirmationRequestManager().getRequest(messageId);

		if (request == null) {
			return;
		}

		String id = request.id();
		Account account = request.account();
		Player player = this.plugin.getServer().getPlayer(account.name());

		// remove login confirmation
		this.plugin.getLoginConfirmationRequestManager().removeRequest(id);

		// check that user is online
		if (player == null || !request.account().discordId().equals(event.getUserId())) {
			this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.login"), event.getChannel());
			return;
		}

		// add the account to the list of authorized
		this.plugin.getAuthManager().addAccount(account);

		// send message
		this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.login"), event.getChannel());

		// delete login confirmation message
		event.getChannel().deleteMessageById(messageId).queueAfter(2, TimeUnit.SECONDS, null,
		                                                           throwable -> plugin.getLogger().fine(
				                                                           "Failed to delete DM message"));

		// log
		this.plugin.getLogger().info(() -> String.format("%s logged in via Discord", player.getName()));

		// send welcome message in the game
		MessageSender.sendMessage(player, this.messagesConfig.getString("welcome"), "{%username%}", player.getName());
	}
}
