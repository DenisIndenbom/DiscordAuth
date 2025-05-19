@Override
public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
    // Tắt toàn bộ logic xác thực qua emoji
    /*
    String messageId = event.getMessageId();
    if (event.getUser() == null) return;
    if (event.getChannelType() != ChannelType.PRIVATE || event.getUser().isBot()) return;

    if (Emoji.fromUnicode("U+2705").equals(event.getEmoji())) {
        LoginConfirmationRequest loginConfirmationRequest =
                this.plugin.getLoginConfirmationRequestManager().getLoginConfirmationRequest(messageId);

        if (loginConfirmationRequest == null) return;

        String id = loginConfirmationRequest.getId();
        Account account = loginConfirmationRequest.getAccount();
        Player player = this.plugin.getServer().getPlayer(account.getName());

        this.plugin.getLoginConfirmationRequestManager().removeRequest(id);

        if (player == null) {
            this.plugin.getBot().sendError(this.messagesConfig.getString("bot_error.login"), event.getChannel());
            return;
        }

        this.plugin.getAuthManager().addAccount(account);

        this.plugin.getBot().sendSuccessful(this.messagesConfig.getString("bot.login"), event.getChannel());

        event.getChannel().deleteMessageById(messageId).queueAfter(15, TimeUnit.SECONDS);

        this.plugin.getLogger().info(player.getName() + " logged in!");

        this.messageSender.sendMessage(player, this.messagesConfig.getString("welcome"), "{%username%}", player.getName());
    }
    */
}
