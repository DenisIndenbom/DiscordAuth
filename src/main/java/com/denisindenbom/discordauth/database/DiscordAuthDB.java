package com.denisindenbom.discordauth.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.denisindenbom.discordauth.units.Account;
import org.jetbrains.annotations.NotNull;

public class DiscordAuthDB extends DataBase
{
    public DiscordAuthDB(String path) throws SQLException
    {
        this.connectToDB(path);
    }

    public void createDefaultDB()
    {
        // create a default database if the database is not initialized
        try
        {
            // create table
            this.executeUpdate("create table users (name char(50) not null, discord_id integer not null, PRIMARY KEY (name))");
            this.commit();
        }
        catch (SQLException ignored)
        {
            try
            {
                this.rollback();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        } // we ignore the exception because we believe that the database has already been created
    }

    public boolean addAccount(@NotNull Account account)
    {
        String sqlRequest = "insert into users (name, discord_id) values (?, ?)";

        try
        {   // add user to db
            this.executeUpdate(sqlRequest, account.getName(), account.getDiscordId());
            // commit
            this.commit();
        }
        catch (SQLException e)
        {
            return false;
        }

        return true;
    }

    public boolean removeAccount(@NotNull String name)
    {
        if (!this.accountExists(name)) return false;

        String sqlRequest = "delete from users where name = ?";

        try
        {   // add user to db
            this.executeUpdate(sqlRequest, name);
            // commit
            this.commit();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Account getAccount(String name)
    {
        String sqlRequest = "select * from users where name = ?";
        try
        {
            // make a sql query to get player data from the database
            ResultSet resultSet = this.executeQuery(sqlRequest, name);

            return new Account(resultSet.getString("name"), resultSet.getString("discord_id"));
        }
        catch (SQLException e)
        {
            return new Account("", "");
        }
    }

    public long countAccountsByDiscordId(String discordId)
    {
        String sqlRequest = "select count(*) as count from users where discord_id=?";
        try
        {
            // make a sql query to get player data from the database
            ResultSet resultSet = this.executeQuery(sqlRequest, discordId);

            return resultSet.getLong("count");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return 0;
        }
    }
    public boolean accountExists(String name)
    {
        String sqlRequest = "select * from users where name=?";
        try
        {
            // make a sql query to get player data from the database
            ResultSet resultSet = this.executeQuery(sqlRequest, name);
            return resultSet.next();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
