package com.denisindenbom.discordauth.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.logging.Logger;

public class Bot
{

	private static final Color ERROR_COLOR = new Color(187, 0, 0);
	private static final Color CONFIRM_COLOR = new Color(204, 189, 25);
	private static final Color INFO_COLOR = new Color(87, 87, 87);
	private static final Color SUCCESS_COLOR = new Color(122, 195, 115);

	private static final String NO_PERMISSION_MSG = "I don't have permissions to send messages to the channel";

	private final JDA jda;
	private final Logger logger;

	public Bot(JDA jda, Logger logger)
	{
		this.jda = jda;
		this.logger = logger;
	}

	public void sendError(String text, MessageChannel channel)
	{
		sendEmbed("Error", text, ERROR_COLOR, channel);
	}

	public void sendInfo(String text, String title, MessageChannel channel)
	{
		sendEmbed(title, text, INFO_COLOR, channel);
	}

	public void sendSuccessful(String text, MessageChannel channel)
	{
		sendEmbed("Successful", text, SUCCESS_COLOR, channel);
	}

	/**
	 * Sends a login confirmation request via DM.
	 *
	 * @return message ID or null if sending failed
	 */
	public String sendLoginConfirmRequest(String text, String userId)
	{
		EmbedBuilder embed = createEmbed("Confirm", text, CONFIRM_COLOR);

		try {
			Message message = this.jda.openPrivateChannelById(userId).flatMap(
					channel -> channel.sendMessageEmbeds(embed.build())).complete();

			message.addReaction(Emoji.fromUnicode("U+2705")).queue();
			return message.getId();
		}
		catch (Exception ignored) {
			return null;
		}
	}

	public void shutdown()
	{
		this.jda.shutdown();
	}

	private void sendEmbed(String title, String text, Color color, @NotNull MessageChannel channel)
	{
		try {
			channel.sendMessageEmbeds(createEmbed(title, text, color).build()).queue();
		}
		catch (InsufficientPermissionException e) {
			logger.warning(NO_PERMISSION_MSG);
		}
	}

	private @NotNull EmbedBuilder createEmbed(String title, String description, Color color)
	{
		return new EmbedBuilder().setTitle(title).setDescription(description).setColor(color);
	}
}
