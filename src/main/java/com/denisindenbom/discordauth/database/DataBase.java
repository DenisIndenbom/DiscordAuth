package com.denisindenbom.discordauth.database;

import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class DataBase implements AutoCloseable
{

	private final Connection conn;

	public DataBase(String path) throws SQLException
	{
		this.conn = DriverManager.getConnection("jdbc:sqlite:" + path);
		this.conn.setAutoCommit(false);
	}

	public <T> T executeQuery(String sql, @NotNull SQLFunction<ResultSet, T> handler,
	                          Object... params) throws SQLException
	{
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameters(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				return handler.apply(rs);
			}
		}
	}

	public int executeUpdate(String sql, Object... params) throws SQLException
	{
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameters(stmt, params);
			return stmt.executeUpdate();
		}
	}

	public void commit() throws SQLException
	{
		conn.commit();
	}

	public void rollback()
	{
		try {
			conn.rollback();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws SQLException
	{
		conn.close();
	}

	private void setParameters(PreparedStatement stmt, Object @NotNull ... params) throws SQLException
	{
		for (int i = 0; i < params.length; i++) {
			Object obj = params[i];
			int index = i + 1;
			if (obj instanceof Integer) {
				stmt.setInt(index, (Integer) obj);
			}
			else if (obj instanceof Long) {
				stmt.setLong(index, (Long) obj);
			}
			else if (obj instanceof Float) {
				stmt.setFloat(index, (Float) obj);
			}
			else if (obj instanceof Double) {
				stmt.setDouble(index, (Double) obj);
			}
			else if (obj instanceof String) {
				stmt.setString(index, (String) obj);
			}
			else if (obj == null) {
				stmt.setNull(index, Types.NULL);
			}
			else {
				stmt.setObject(index, obj);
			}
		}
	}

	@FunctionalInterface
	public interface SQLFunction<T, R>
	{
		R apply(T t) throws SQLException;
	}
}
