package com.denisindenbom.discordauth.discord;

import com.denisindenbom.discordauth.units.Account;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import com.denisindenbom.discordauth.DiscordAuth;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;


public class VerificationHandler extends ListenerAdapter
{
    private final DiscordAuth plugin;
    private final String channelId;
    private final int maxNumOfAccounts;
    private final FileConfiguration messagesConfig;

    public VerificationHandler(DiscordAuth plugin)
    {
        this.plugin = plugin;

        this.channelId = this.plugin.getConfig().getString("channel-id");
        this.maxNumOfAccounts = this.plugin.getConfig().getInt("max-num-of-accounts");

        this.messagesConfig = this.plugin.getMessagesConfig();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        String message = event.getMessage().getContentDisplay();
        String authorId = event.getAuthor().getId();
        String channelId = event.getChannel().getId();

        // ignoring unnecessary messages
        if (!(channelId.equals(this.channelId) && message.startsWith("!verify"))) return;

        // check that the user does not exceed the number of maximum accounts
        if (this.plugin.getAuthDB().countAccountsByDiscordId(authorId) >= this.maxNumOfAccounts)
        {
            this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.enough_accounts"),
                                           event.getChannel());
            return;
        }

        // split message
        String[] splitMessage = message.split("\\s+");

        // checking for the presence of an argument
        if (splitMessage.length < 2)
        {
            this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.name_no_set"), event.getChannel());
            return;
        }

        // add user to database
        boolean result = this.plugin.getAuthDB().addAccount(new Account(splitMessage[1], authorId));

        if (!result)
        {
            this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.user_exists"), event.getChannel());
            return;
        }

        this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.verification_successful"), event.getChannel());
    }
}
