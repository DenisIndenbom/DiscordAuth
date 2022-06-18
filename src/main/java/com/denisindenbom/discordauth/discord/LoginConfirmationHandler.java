package com.denisindenbom.discordauth.discord;

import com.denisindenbom.discordauth.DiscordAuth;

import com.denisindenbom.discordauth.units.Account;
import com.denisindenbom.discordauth.units.LoginConfirmationRequest;
import net.dv8tion.jda.api.entities.ChannelType;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public class LoginConfirmationHandler extends ListenerAdapter
{
    private final DiscordAuth plugin;

    private final FileConfiguration messagesConfig;

    public LoginConfirmationHandler(DiscordAuth plugin)
    {
        this.plugin = plugin;

        this.messagesConfig = plugin.getMessagesConfig();
    }

    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event)
    {
        ChannelType channelType = event.getChannelType();
        String reactionName = event.getReactionEmote().getName();
        String messageId = event.getMessageId();

        // check that channel is private and the user put a reaction
        if (channelType != ChannelType.PRIVATE || event.getUser().isBot()) return;

        if (reactionName.equals("✅"))
        {
            // get login confirmation request
            LoginConfirmationRequest loginConfirmationRequest =
                    this.plugin.getLoginConfirmationRequestManager().getLoginConfirmationRequest(messageId);

            if (loginConfirmationRequest == null) return;

            String id = loginConfirmationRequest.getId();
            Account account = loginConfirmationRequest.getAccount();
            Player player = this.plugin.getServer().getPlayer(account.getName());

            // remove login confirmation
            this.plugin.getLoginConfirmationRequestManager().removeRequest(id);

            // check that user is online
            if (player == null)
            {
                this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.login"), event.getPrivateChannel());
                return;
            }

            // add the account to the list of authorized
            this.plugin.getAuthManager().addAccount(account);

            // send message
            this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.login"), event.getPrivateChannel());

            this.plugin.getLogger().info(player.getName() + " logged in!");
        }
    }
}
