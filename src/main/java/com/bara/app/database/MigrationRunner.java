package com.bara.app.database;

/**
 * Simple runner to execute Flyway migrations.
 * Used for initial database setup and jOOQ code generation.
 */
public class MigrationRunner {
    public static void main(String[] args) {
        System.out.println("Running database migrations...");
        DatabaseManager.initializeDatabase();
        System.out.println("Migrations completed successfully.");
    }
}
