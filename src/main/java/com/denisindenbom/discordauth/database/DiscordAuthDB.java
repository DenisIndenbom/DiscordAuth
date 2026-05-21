package com.denisindenbom.discordauth.database;

import com.denisindenbom.discordauth.units.Account;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DiscordAuthDB extends DataBase
{
	private final Logger logger;

	public DiscordAuthDB(String url, String username, String password, Logger logger) throws SQLException
	{
		super(url, username, password);
		this.logger = logger;

		String sql = """
		             CREATE TABLE IF NOT EXISTS users (
		                 name VARCHAR(255) NOT NULL PRIMARY KEY,
		                 discord_id TEXT NOT NULL
		             );
		             """;
		try {
			this.executeUpdate(sql);
			if (this.needsMigration()) {
				this.migrate();
			}
			this.commit();
		}
		catch (SQLException e) {
			this.rollback();
			throw e;
		}
	}

	public boolean addAccount(@NotNull Account account)
	{
		String sql = "INSERT INTO users (name, discord_id) VALUES (?, ?)";
		try {
			this.executeUpdate(sql, account.name(), account.discordId());
			this.commit();
			return true;
		}
		catch (SQLException e) {
			this.logger.severe(e.getMessage());
			this.rollback();
			return false;
		}
	}

	public boolean removeAccount(@NotNull String name)
	{
		if (!this.accountExists(name)) {
			return false;
		}
		String sql = "DELETE FROM users WHERE name = ?";
		try {
			this.executeUpdate(sql, name);
			this.commit();
			return true;
		}
		catch (SQLException e) {
			this.rollback();
			return false;
		}
	}

	public Account getAccount(String name)
	{
		String sql = "SELECT * FROM users WHERE name = ?";
		try {
			return this.executeQuery(sql, rs -> {
				if (rs.next()) {
					return new Account(rs.getString("name"), rs.getString("discord_id"));
				}
				return new Account("", "");
			}, name);
		}
		catch (SQLException e) {
			this.logger.severe(e.getMessage());
			return new Account("", "");
		}
	}

	public long countAccountsByDiscordId(String discordId)
	{
		String sql = "SELECT COUNT(*) AS count FROM users WHERE discord_id = ?";
		try {
			return executeQuery(sql, rs -> rs.next() ? rs.getLong("count") : 0, discordId);
		}
		catch (SQLException e) {
			this.logger.severe(e.getMessage());
			return 0;
		}
	}

	public boolean accountExists(String name)
	{
		String sql = "SELECT 1 FROM users WHERE name = ?";
		try {
			return this.executeQuery(sql, ResultSet::next, name);
		}
		catch (SQLException e) {
			this.logger.severe(e.getMessage());
			return false;
		}
	}

	private boolean needsMigration() throws SQLException
	{
		String sql = "PRAGMA table_info(users)";

		return executeQuery(sql, rs -> {
			while (rs.next()) {
				String columnName = rs.getString("name");
				String columnType = rs.getString("type");

				if ("discord_id".equalsIgnoreCase(columnName)) {
					return "INTEGER".equalsIgnoreCase(columnType);
				}
			}

			return false;
		});
	}

	private void migrate() throws SQLException
	{
		String[] queries = {
				"ALTER TABLE users RENAME TO users_old",
				"""
				CREATE TABLE IF NOT EXISTS users (
				    name VARCHAR(255) NOT NULL PRIMARY KEY,
				    discord_id TEXT NOT NULL
				)
				""",
				"""
				INSERT INTO users (name, discord_id)
				SELECT
				    name,
				    CAST(discord_id AS TEXT)
				FROM users_old
				""",
				"DROP TABLE users_old"
		};

		try {
			for (String sql : queries) {
				this.executeUpdate(sql);
			}
		}
		catch (SQLException e) {
			throw new SQLException("Failed to migrate users table!");
		}
	}
}
