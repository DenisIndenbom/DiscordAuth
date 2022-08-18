package com.denisindenbom.discordauth.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import com.denisindenbom.discordauth.DiscordAuth;
import com.denisindenbom.discordauth.units.Account;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;


public class DiscordCommandsHandler extends ListenerAdapter
{
    private final DiscordAuth plugin;
    private final String channelId;
    private final int maxNumOfAccounts;
    private final FileConfiguration messagesConfig;

    public DiscordCommandsHandler(DiscordAuth plugin)
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
        if (!(channelId.equals(this.channelId) && (message.startsWith("!")))) return;

        // split message
        String[] splitMessage = message.split("\\s+");

        // create new account
        if (message.startsWith("!add"))
        {
            // check that the user does not exceed the number of maximum accounts
            if (this.plugin.getAuthDB().countAccountsByDiscordId(authorId) >= this.maxNumOfAccounts)
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.enough_accounts"),
                        event.getChannel());
                return;
            }

            // checking for the presence of an argument
            if (splitMessage.length < 2)
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.name_no_set"), event.getChannel());
                return;
            }

            // add user to database
            if (!this.plugin.getAuthDB().addAccount(new Account(splitMessage[1], authorId)))
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.user_exists"), event.getChannel());
                return;
            }
            // send message
            this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.verification_successful"), event.getChannel());
        }
        // delete account
        if (message.startsWith("!delete"))
        {
            if (!this.plugin.getConfig().getBoolean("allow-delete-accounts"))
            {
                this.plugin.getBot().sendError(this.plugin.getMessagesConfig().getString("bot_error.account_deletion_is_not_allowed"), event.getChannel());
                return;
            }

            // checking for the presence of an argument
            if (splitMessage.length < 2)
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.name_no_set"), event.getChannel());
                return;
            }

            // check that account is exits
            if (!this.plugin.getAuthDB().accountExists(splitMessage[1]))
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.account_not_exits"), event.getChannel());
                return;
            }

            Account account = this.plugin.getAuthDB().getAccount(splitMessage[1]);

            // check that user is the account owner
            if (!account.getDiscordId().equals(event.getAuthor().getId()))
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.account_owner"), event.getChannel());
                return;
            }

            // handle error
            if (!this.plugin.getAuthDB().removeAccount(account.getName()))
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.not_expected_error"), event.getChannel());
                return;
            }

            this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.deletion_successful"), event.getChannel());
        }
        // send help
        if (message.startsWith("!help"))
        {this.plugin.getBot().sendInfo(this.messagesConfig.getString("bot.help"), "Commands", event.getChannel());}
    }
}
