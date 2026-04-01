package com.denisindenbom.discordauth.database;

import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class DataBase implements AutoCloseable
{
	private Connection conn;
	private final String url;
	private final String username;
	private final String password;
	private boolean isClosed = false;

	public DataBase(String url, String username, String password) throws SQLException
	{
		// Store connection parameters for reconnection
		this.url = url;
		this.username = username;
		this.password = password;

		// Manually load postgresql driver
		try {
			Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException e) {
			// ignore (this should be provided my shading)
		}

		this.conn = DriverManager.getConnection("jdbc:" + url, username, password);
		this.conn.setAutoCommit(false);
	}

	public <T> T executeQuery(String sql, @NotNull SQLFunction<ResultSet, T> handler,
	                          Object... params) throws SQLException
	{
		return withConnectionRetry((db) ->
		                           {
			                           try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				                           setParameters(stmt, params);
				                           try (ResultSet rs = stmt.executeQuery()) {
					                           return handler.apply(rs);
				                           }
			                           }
		                           });
	}

	public int executeUpdate(String sql, Object... params) throws SQLException
	{
		return withConnectionRetry((db) ->
		                           {
			                           try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				                           setParameters(stmt, params);
				                           return stmt.executeUpdate();
			                           }
		                           });
	}

	public void commit() throws SQLException
	{
		withConnectionRetry((db) ->
		                    {
			                    conn.commit();
			                    return null;
		                    });
	}

	public void rollback()
	{
		try {
			withConnectionRetry((db) ->
			                    {
				                    conn.rollback();
				                    return null;
			                    });
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws SQLException
	{
		isClosed = true;
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
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

	// Decorator methods for reconnection
	private <T> T withConnectionRetry(@NotNull DatabaseOperation<T> operation) throws SQLException
	{
		if (this.isClosed) {
			throw new SQLException("Database connection is closed");
		}

		for (int i = 0; i < 3; ++i) {
			try {
				if (this.conn == null || this.conn.isClosed()) {
					reconnect();
				}
				return operation.execute(this);
			}
			catch (SQLException e) {
				if (!isConnectionError(e)) {
					throw e; // Non-connection error, don't retry
				}
			}
		}

		throw new SQLException("Database is not reachable", new Throwable());
	}

	private synchronized void reconnect() throws SQLException
	{
		if (isClosed) {
			throw new SQLException("Cannot reconnect: Database connection is closed");
		}

		// Close existing connection if it exists
		if (conn != null && !conn.isClosed()) {
			try {
				conn.close();
			}
			catch (SQLException e) {
				// Log but continue with reconnection
				e.printStackTrace();
			}
		}

		// Create new connection
		this.conn = DriverManager.getConnection("jdbc:" + url, username, password);
		this.conn.setAutoCommit(false);
	}

	private boolean isConnectionError(@NotNull SQLException e)
	{
		// Check for common connection-related SQL states
		String sqlState = e.getSQLState();
		if (sqlState == null) {
			return false;
		}

		// Connection errors typically start with "08"
		return sqlState.startsWith("08") ||
				// Network or I/O errors
				sqlState.equals("08001") || // SQLClientUnableToEstablishSQLConnection
				sqlState.equals("08003") || // ConnectionDoesNotExist
				sqlState.equals("08006") || // ConnectionFailure
				sqlState.equals("08007") || // TransactionResolutionUnknown
				sqlState.equals("08501") || // SQLServerRejectedEstablishmentOfSQLConnection
				sqlState.equals("08502") || // ConnectionFailureInTransactionProcessing
				// Also check for common Postgres connection errors
				e.getMessage() != null && (e.getMessage().contains("connection") || e.getMessage().contains(
						"socket") || e.getMessage().contains("network") || e.getMessage().contains(
						"timeout") || e.getMessage().contains("closed"));
	}

	// Decorator interfaces
	@FunctionalInterface
	private interface DatabaseOperation<T>
	{
		T execute(DataBase db) throws SQLException;
	}

	@FunctionalInterface
	public interface SQLFunction<T, R>
	{
		R apply(T t) throws SQLException;
	}
}