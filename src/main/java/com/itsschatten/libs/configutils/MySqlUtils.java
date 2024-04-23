package com.itsschatten.libs.configutils;

import com.itsschatten.libs.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.*;

public class MySqlUtils {

    private final String line;
    private final String user;
    private final String pass;

    @Getter
    protected Connection connection;

    public MySqlUtils(String host, int port, String database, String user, String password) {
        this("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false", user, password);
    }

    private MySqlUtils(String line, String user, String password) {
        this.line = line;
        this.user = user;
        this.pass = password;

        connect();
    }

    private void connect() {
        try {
            this.connection = DriverManager.getConnection(line, this.user, this.pass);
        } catch (final SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to connect to MySQL Database! Check above for details.");
            this.connection = null;
        }
    }


    public final void close() {
        try {
            if (this.connection != null)
                this.connection.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();

            System.out.println("Failed to close connection with MySQL Database! Check above for details.");
        }
    }

    public final void update(String query) {
        openIfClosed();

        try {
            final Statement statement = this.connection.createStatement();

            statement.executeUpdate(query);
        } catch (final SQLException ex) {
            ex.printStackTrace();

            System.out.println("Failed to update MySQL with query: " + query + " Check above for details.");
        }
    }

    public final WrappedResultSet query(String query) {
        openIfClosed();

        ResultSet set = null;

        try {
            final Statement statement = this.connection.createStatement();

            set = statement.executeQuery(query);
        } catch (final SQLException ex) {
            ex.printStackTrace();

            System.out.println("Failed to query MySQL with: " + query + " Check above for details.");
        }

        return set == null ? null : new WrappedResultSet(set);
    }

    private void openIfClosed() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(0)) {
                Utils.debugLog("Connection to the database is either closed or never existed in the first place, attempting to establish connection...");
                connect();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();

            System.out.println("Failed to re-connect to MySQL! Check above for details.");
        }
    }

    @RequiredArgsConstructor
    public static class WrappedResultSet {
        @Getter
        private final ResultSet resultSet;

        public final boolean next() throws SQLException {
            return resultSet.next();
        }

        public final boolean valueExists() throws SQLException, NullPointerException {
            return resultSet.next();
        }

        public final boolean hasColumn(String name) throws SQLException {
            final ResultSetMetaData meta = resultSet.getMetaData();

            for (int i = 1; i <= meta.getColumnCount(); i++)
                if (name.equals(meta.getColumnName(i)))
                    return true;

            return false;
        }

    }
}
