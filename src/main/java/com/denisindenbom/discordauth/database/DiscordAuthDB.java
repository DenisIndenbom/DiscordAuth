package com.denisindenbom.discordauth.database;

import com.denisindenbom.discordauth.units.Account;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DiscordAuthDB extends DataBase
{

	public DiscordAuthDB(String url, String username, String password) throws SQLException
	{
		super(url, username, password);
	}

	public void createDefaultDB()
	{
		String sql = """
				CREATE TABLE IF NOT EXISTS users (
				    name TEXT NOT NULL PRIMARY KEY,
				    discord_id TEXT NOT NULL
				)
				""";
		try {
			executeUpdate(sql);
			commit();
		}
		catch (SQLException e) {
			rollback();
		}
	}

	public boolean addAccount(@NotNull Account account)
	{
		String sql = "INSERT INTO users (name, discord_id) VALUES (?, ?)";
		try {
			executeUpdate(sql, account.name(), account.discordId());
			commit();
			return true;
		}
		catch (SQLException e) {
			rollback();
			return false;
		}
	}

	public boolean removeAccount(@NotNull String name)
	{
		if (!accountExists(name)) {
			return false;
		}
		String sql = "DELETE FROM users WHERE name = ?";
		try {
			executeUpdate(sql, name);
			commit();
			return true;
		}
		catch (SQLException e) {
			rollback();
			return false;
		}
	}

	public Account getAccount(String name)
	{
		String sql = "SELECT * FROM users WHERE name = ?";
		try {
			return executeQuery(sql, rs ->
			{
				if (rs.next()) {
					return new Account(rs.getString("name"), rs.getString("discord_id"));
				}
				return new Account("", "");
			}, name);
		}
		catch (SQLException e) {
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
			return 0;
		}
	}

	public boolean accountExists(String name)
	{
		String sql = "SELECT 1 FROM users WHERE name = ?";
		try {
			return executeQuery(sql, ResultSet::next, name);
		}
		catch (SQLException e) {
			return false;
		}
	}
}
