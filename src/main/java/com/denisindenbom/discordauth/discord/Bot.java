package com.denisindenbom.discordauth.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.Color;

import java.util.logging.Logger;

public class Bot
{
    private final JDA JDA;
    private final Logger logger;

    public Bot(JDA jda, Logger logger)
    {
        this.JDA = jda;
        this.logger = logger;
    }

    public void sendError(String text, MessageChannel channel)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Error");
        embedBuilder.setDescription(text);
        embedBuilder.setColor(new Color(187, 0, 0));

        try
        {channel.sendMessageEmbeds(embedBuilder.build()).queue();}
        catch (InsufficientPermissionException exception)
        {this.logger.warning("I don't have permissions to send messages to the channel");}
    }

    public String sendLoginConfirmRequest(String text, String userId)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Confirm");
        embedBuilder.setDescription(text);
        embedBuilder.setColor(new Color(204, 189, 25));

        try
        {
            Message message = this.JDA.openPrivateChannelById(userId).
                    flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embedBuilder.build())).complete();

            message.addReaction(Emoji.fromUnicode("U+2705")).queue();
            return message.getId();
        }
        catch (Exception ignored)
        {return null;}
    }

    public void sendInfo(String text, String Title, MessageChannel channel)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(Title);
        embedBuilder.setDescription(text);
        embedBuilder.setColor(new Color(87, 87, 87));

        try
        {channel.sendMessageEmbeds(embedBuilder.build()).queue();}
        catch (InsufficientPermissionException exception)
        {this.logger.warning("I don't have permissions to send messages to the channel");}
    }

    public void sendSuccessful(String text, MessageChannel channel)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Successful");
        embedBuilder.setDescription(text);
        embedBuilder.setColor(new Color(122, 195, 115));

        try
        {channel.sendMessageEmbeds(embedBuilder.build()).queue();}
        catch (InsufficientPermissionException exception)
        {this.logger.warning("I don't have permissions to send messages to the channel");}
    }

    public JDA getJDA()
    {return this.JDA;}
}
