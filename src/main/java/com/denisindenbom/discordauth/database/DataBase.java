package com.denisindenbom.discordauth.database;

import java.sql.*;

public class DataBase
{
    private Connection conn;

    public void connectToDB(String path) throws SQLException
    {
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + path);
        this.conn.setAutoCommit(false);
    }

    public ResultSet executeQuery(String sqlRequest, Object... args) throws SQLException
    {
        PreparedStatement statement = this.conn.prepareStatement(sqlRequest);

        prepare_(statement, args);

        return statement.executeQuery();
    }

    public int executeUpdate(String sqlRequest, Object... args) throws SQLException
    {
        PreparedStatement statement = this.conn.prepareStatement(sqlRequest);

        this.prepare_(statement, args);

        return statement.executeUpdate();
    }

    public void commit() throws SQLException
    {
        this.conn.commit();
    }

    public void rollback() throws SQLException
    {
        this.conn.rollback();
    }

    public void disable() throws SQLException
    {
        this.conn.close();
    }

    private void prepare_(PreparedStatement statement, Object... args) throws SQLException
    {
        for (int i = 0; i < args.length; i++)
        {
            Object obj = args[i];

            int j = i + 1;

            if (obj instanceof Integer) statement.setInt(j, (int) obj);
            else if (obj instanceof Float) statement.setFloat(j, (float) obj);
            else if (obj instanceof Double) statement.setDouble(j, (double) obj);
            else if (obj instanceof String) statement.setString(j, (String) obj);
            else if (obj instanceof Array) statement.setArray(j, (Array) obj);
        }
    }
}
