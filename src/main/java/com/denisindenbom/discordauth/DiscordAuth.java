package com.denisindenbom.discordauth;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;

import com.denisindenbom.discordauth.units.Account;
import com.denisindenbom.discordauth.units.LoginConfirmationRequest;
import com.denisindenbom.discordauth.listeners.PlayerListener;

import com.denisindenbom.discordauth.managers.AccountAuthManager;
import com.denisindenbom.discordauth.managers.LoginConfirmationRequestManager;

import com.denisindenbom.discordauth.database.DiscordAuthDB;
import com.denisindenbom.discordauth.commands.*;

import com.denisindenbom.discordauth.discord.DiscordCommandsHandler;
import com.denisindenbom.discordauth.discord.LoginConfirmationHandler;

import com.denisindenbom.discordauth.discord.Bot;
import com.denisindenbom.discordauth.utils.Config;

import java.io.File;
import java.sql.SQLException;

import javax.security.auth.login.LoginException;

public class DiscordAuth extends JavaPlugin
{
	private DiscordAuthDB authDB;
	private AccountAuthManager authManager;
	private LoginConfirmationRequestManager loginConfirmationRequestManager;
	private PlayerListener playerListener;
	private FileConfiguration messagesConfig;
	private Bot bot;

	@Override
	public void onLoad()
	{}

	@Override
	public void onEnable()
	{
		try {
			this.loadPlugin();
			this.getLogger().info("Plugin is enable!");
		}
		catch (Exception e) {
			e.printStackTrace();
			this.getLogger().warning("DiscordAuth is not running! Plugin don't work! Please, check file config.yml!");
		}
	}

	@Override
	public void onDisable()
	{
		this.disablePlugin();
	}

	public void loadPlugin()
	{
		// save default configs
		this.saveDefaultConfig();
		this.saveDefaultMessages();

		this.loadMessages();

		// load db
		try {
			this.initDatabase();
		}
		catch (SQLException e) {
			this.getLogger().severe(
					"Failed to connect to the database! Please, check the database settings in config.yml!");
			return;
		}
		catch (IllegalArgumentException e) {
			this.getLogger().severe(e.getMessage());
			return;
		}

		// init managers
		this.authManager = new AccountAuthManager();
		this.loginConfirmationRequestManager = new LoginConfirmationRequestManager(
				this.getConfig().getLong("auth-time", 90));

		// register commands executors
		this.getCommand("reload_discordauth").setExecutor(new Reload(this));
		this.getCommand("remove_user").setExecutor(new Remove(this));

		// create and register player listener
		this.playerListener = new PlayerListener(this);
		this.getServer().getPluginManager().registerEvents(this.playerListener, this);

		// init jda
		try {
			this.initDiscordBot();
		}
		catch (LoginException e) {
			this.getLogger().warning("Failed to connect to discord! Please, check bot token!");
			this.disablePlugin();
		}
		catch (IllegalArgumentException e) {
			this.getLogger().severe(e.getMessage());
			this.disablePlugin();
		}

		// register login confirmation requests for all players
		for (Player player : this.getServer().getOnlinePlayers()) {
			this.registerLoginConfirmationRequest(player);
		}
	}

	private void disablePlugin()
	{
		try {
			// close connection to database
			this.authDB.close();
		}
		catch (SQLException e) {
			this.getLogger().warning("Failed to close database connection!");
		}
		catch (NullPointerException e) {
			// ignore this exception (something went wrong on enable)
		}

		if (this.playerListener != null) {
			HandlerList.unregisterAll(this.playerListener);
			this.playerListener = null;
		}

		if (this.bot != null) {
			this.bot.shutdown();
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

	public void registerLoginConfirmationRequest(@NotNull Player player)
	{
		// get player account
		Account account = this.getAuthDB().getAccount(player.getName());

		if (!this.getLoginConfirmationRequestManager().accountHasRequest(account)) {
			// format message
			String message = this.messagesConfig.getString("bot.authorization").replace("{%username%}",
			                                                                            player.getName());
			// send login confirm request and get message id
			String messageId = this.getBot().sendLoginConfirmRequest(message, account.discordId());
			// register login confirmation
			if (messageId != null) {
				this.getLoginConfirmationRequestManager().registerRequest(
						new LoginConfirmationRequest(messageId, account));
			}
		}
	}

	public AccountAuthManager getAuthManager()
	{
		return this.authManager;
	}

	public LoginConfirmationRequestManager getLoginConfirmationRequestManager()
	{
		return this.loginConfirmationRequestManager;
	}

	public DiscordAuthDB getAuthDB()
	{
		return this.authDB;
	}

	public Bot getBot()
	{
		return this.bot;
	}

	public FileConfiguration getMessagesConfig()
	{
		return this.messagesConfig;
	}

	private void initDatabase() throws SQLException, IllegalArgumentException
	{
		FileConfiguration config = getConfig();

		String type = Config.require(config, "database.type");
		String name = Config.require(config, "database.name");
		String host = Config.require(config, "database.host");
		String port = Config.require(config, "database.port");
		String username = Config.require(config, "database.username");
		String password = Config.require(config, "database.password");
		boolean ssl = config.getBoolean("database.ssl", false);

		String url = switch (type.toLowerCase()) {
			case "sqlite" -> "sqlite:" + getDataFolder().getPath() + '/' + name;
			case "postgres", "postgresql" -> String.format("postgresql://%s:%s/%s?ssl=%b", host, port, name, ssl);
			case "mysql" ->
					String.format("mysql://%s:%s/%s?useSSL=%b&requireSSL=%b&allowPublicKeyRetrieval=%b", host, port,
					              name, ssl, ssl, !ssl);
			default -> throw new IllegalArgumentException("Unexpected value: " + type.toLowerCase());
		};

		this.authDB = new DiscordAuthDB(url, username, password, this.getLogger());
		this.authDB.createDefaultDB();
	}

	private void initDiscordBot() throws LoginException, IllegalArgumentException
	{
		FileConfiguration config = getConfig();

		// build discord bot
		JDABuilder jdaBuilder = JDABuilder.createDefault(Config.require(config, "bot-token"),
		                                                 GatewayIntent.DIRECT_MESSAGES,
		                                                 GatewayIntent.DIRECT_MESSAGE_REACTIONS,
		                                                 GatewayIntent.GUILD_MESSAGES,
		                                                 GatewayIntent.GUILD_MESSAGE_REACTIONS,
		                                                 GatewayIntent.MESSAGE_CONTENT);

		String activityText = config.getString("activity.text", "._.");

		Activity activity = switch (this.getConfig().getString("activity.type")) {
			case "WATCHING" -> Activity.watching(activityText);
			case "LISTENING" -> Activity.listening(activityText);
			case "PLAYING" -> Activity.playing(activityText);
			case "COMPETING" -> Activity.competing(activityText);
			default -> null;
		};

		if (activity != null) {
			jdaBuilder.setActivity(activity);
		}

		jdaBuilder.addEventListeners(new DiscordCommandsHandler(this), new LoginConfirmationHandler(this));
		jdaBuilder.disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS);

		this.bot = new Bot(jdaBuilder.build(), this.getLogger());
	}

	private void saveDefaultMessages()
	{
		File messagesFile = new File(this.getDataFolder(), "messages.yml");

		if (!messagesFile.exists()) {
			this.saveResource("messages.yml", false);
		}
	}

	private void loadMessages()
	{
		File messagesFile = new File(this.getDataFolder(), "messages.yml");

		this.messagesConfig = new YamlConfiguration();
		try {
			this.messagesConfig.load(messagesFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}