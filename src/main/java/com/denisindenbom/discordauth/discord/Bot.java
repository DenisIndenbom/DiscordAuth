package com.denisindenbom.discordauth.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.Color;

public class Bot
{
    private final JDA JDA;

    public Bot(JDA jda)
    {this.JDA = jda;}

    public void sendError(String text, MessageChannel channel)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Error");
        embedBuilder.setDescription(text);
        embedBuilder.setColor(new Color(187, 0, 0));

        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public String sendLoginConfirmRequest(String text, String userId)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Confirm");
        embedBuilder.setDescription(text);
        embedBuilder.setColor(new Color(204, 189, 25));

        Message message = this.JDA.openPrivateChannelById(userId).
                flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embedBuilder.build())).complete();

        message.addReaction("✅").queue();

        return message.getId();
    }

    public void sendSuccessful(String text, MessageChannel channel)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Successful");
        embedBuilder.setDescription(text);
        embedBuilder.setColor(new Color(122, 195, 115));

        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public JDA getJDA()
    {return this.JDA;}
}
