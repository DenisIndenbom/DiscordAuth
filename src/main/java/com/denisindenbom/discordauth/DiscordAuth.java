package com.denisindenbom.discordauth;

import com.denisindenbom.discordauth.units.Account;
import com.denisindenbom.discordauth.units.LoginConfirmationRequest;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import com.denisindenbom.discordauth.listeners.PlayerListener;

import com.denisindenbom.discordauth.managers.AccountAuthManager;
import com.denisindenbom.discordauth.managers.LoginConfirmationRequestManager;

import com.denisindenbom.discordauth.database.DiscordAuthDB;
import com.denisindenbom.discordauth.commands.*;

import org.bukkit.event.HandlerList;
import com.denisindenbom.discordauth.discord.DiscordCommandsHandler;
import com.denisindenbom.discordauth.discord.LoginConfirmationHandler;

import com.denisindenbom.discordauth.discord.Bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.sql.SQLException;

import javax.security.auth.login.LoginException;

public class DiscordAuth extends JavaPlugin
{
    private FileConfiguration messagesConfig;

    private AccountAuthManager authManager;
    private LoginConfirmationRequestManager loginConfirmationRequestManager;

    private DiscordAuthDB authDB;

    private PlayerListener playerListener;

    private Bot bot;

    @Override
    public void onLoad()
    {}

    @Override
    public void onEnable()
    {
        try
        {
            this.loadPlugin();
            this.getLogger().info("Plugin is enable!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.getLogger().warning("DiscordAuth is not running! Plugin don't work! Please, check file config.yml!");
        }
    }

    @Override
    public void onDisable()
    {this.disablePlugin();}

    public void loadPlugin()
    {
        // save default configs
        this.saveDefaultConfig();
        this.saveDefaultMessages();

        this.loadMessages();

        // load db
        try
        {
            this.authDB = new DiscordAuthDB(this.getDataFolder().getPath() + "/" + "DiscordAuth.db");
            this.authDB.createDefaultDB();
        }
        catch (SQLException e)
        {
            this.getLogger().warning("Failed to load database! Please, check file config.yml or delete DiscordAuth.db");
            return;
        }

        // init managers
        this.authManager = new AccountAuthManager();
        this.loginConfirmationRequestManager = new LoginConfirmationRequestManager(this.getConfig().getLong("auth-time"));

        // register commands executors
        this.getCommand("reload_discordauth").setExecutor(new Reload(this, this.messagesConfig));
        this.getCommand("remove_user").setExecutor(new Remove(this, this.messagesConfig));

        // create player listener
        this.playerListener = new PlayerListener(this);
        // register player listener
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);

        // init jda
        try
        {
            this.initDiscordBot();
        }
        catch (LoginException loginException)
        {
            this.getLogger().warning("Failed to connect to discord! Please, check bot token!");
            this.disablePlugin();
        }

        // register login confirmation requests for all players
        for (Player player : this.getServer().getOnlinePlayers()) this.registerLoginConfirmationRequest(player);
    }

    private void disablePlugin()
    {
        try
        {
            // disable database
            this.authDB.disable();
        }
        catch (SQLException e)
        {
            this.getLogger().warning("Failed to close database connection!");
        }

        HandlerList.unregisterAll(this.playerListener);
        this.playerListener = null;

        this.bot.getJDA().shutdown();
    }

    public void registerLoginConfirmationRequest(Player player)
    {
        // get player account
        Account account = this.getAuthDB().getAccount(player.getName());

        if (!this.getLoginConfirmationRequestManager().accountHasRequest(account))
        {
            // format message
            String message = this.messagesConfig.getString("bot.authorization").replace("{%username%}", player.getName());
            // send login confirm request and get message id
            String messageId = this.getBot().sendLoginConfirmRequest(message, account.getDiscordId());
            // register login confirmation
            if (messageId != null)
                this.getLoginConfirmationRequestManager().registerRequest(new LoginConfirmationRequest(messageId, account));
        }
    }

    public void reloadPlugin()
    {
        // reload config
        this.reloadConfig();
        // disable plugin
        this.disablePlugin();
        // load plugin
        this.loadPlugin();
    }

    public AccountAuthManager getAuthManager()
    {return this.authManager;}

    public LoginConfirmationRequestManager getLoginConfirmationRequestManager()
    {return this.loginConfirmationRequestManager;}

    public DiscordAuthDB getAuthDB()
    {return this.authDB;}

    public Bot getBot()
    {return this.bot;}

    public FileConfiguration getMessagesConfig()
    {return this.messagesConfig;}

    private void initDiscordBot() throws LoginException
    {
        // build discord bot
        JDABuilder jdaBuilder = JDABuilder.createDefault(this.getConfig().getString("bot-token"),
                                                         GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                                                         GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                                                         GatewayIntent.MESSAGE_CONTENT);

        String activityText = this.getConfig().getString("activity.text");

        Activity activity = switch (this.getConfig().getString("activity.type"))
                {
                    case "WATCHING" -> Activity.watching(activityText);
                    case "LISTENING" -> Activity.listening(activityText);
                    case "PLAYING" -> Activity.playing(activityText);
                    case "COMPETING" -> Activity.competing(activityText);
                    default -> null;
                };

        if (activity != null) jdaBuilder.setActivity(activity);

        jdaBuilder.addEventListeners(new DiscordCommandsHandler(this),
                                     new LoginConfirmationHandler(this));

        this.bot = new Bot(jdaBuilder.build(), this.getLogger());
    }

    private void saveDefaultMessages()
    {
        File messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) saveResource("messages.yml", false);
    }

    private void loadMessages()
    {
        File messagesFile = new File(getDataFolder(), "messages.yml");

        this.messagesConfig = new YamlConfiguration();
        try
        {
            this.messagesConfig.load(messagesFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}