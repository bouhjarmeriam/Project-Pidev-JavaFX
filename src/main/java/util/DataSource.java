package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private String url = "jdbc:mysql://localhost:3306/appclinique ";
    private String username = "root";
    private String password = "";
    private Connection connection;
    private static DataSource instance;

    private DataSource() {
        try {
            // 1. Load the JDBC driver (important for older Java versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Establish connection
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed! Check output console");
            e.printStackTrace();
            throw new RuntimeException("Failed to create database connection", e);
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            synchronized (DataSource.class) {
                if (instance == null) {
                    instance = new DataSource();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Verify connection is still valid
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}