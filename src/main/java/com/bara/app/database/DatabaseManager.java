package com.bara.app.database;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database manager using Flyway for schema migrations.
 * Flyway handles all schema creation and versioning automatically.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:bara.db";
    private static boolean initialized = false;

    /**
     * Get a connection to the SQLite database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initialize the database using Flyway migrations.
     * This method is idempotent - safe to call multiple times.
     */
    public static void initializeDatabase() {
        if (initialized) {
            return;
        }

        try {
            System.out.println("Initializing database with Flyway...");

            // Configure Flyway
            Flyway flyway = Flyway.configure()
                    .dataSource(DB_URL, null, null)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true) // Allow migrating an existing database
                    .validateOnMigrate(true)
                    .load();

            // Run migrations
            int migrationsApplied = flyway.migrate().migrationsExecuted;

            if (migrationsApplied > 0) {
                System.out.println("Database initialized successfully. Applied " + migrationsApplied + " migration(s).");
            } else {
                System.out.println("Database is up to date. No migrations applied.");
            }

            initialized = true;

        } catch (FlywayException e) {
            System.err.println("Error initializing database with Flyway: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Close a database connection.
     */
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Get the database URL (useful for jOOQ DSLContext configuration).
     */
    public static String getDatabaseUrl() {
        return DB_URL;
    }
}
