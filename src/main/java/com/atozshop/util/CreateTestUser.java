package com.atozshop.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CreateTestUser {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/atozshop";
        String username = "atozshop";
        String password = "atozshop123";

        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Connect to database
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to database successfully!");

            // Generate password hash
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String adminPasswordHash = encoder.encode("admin123");
            String customerPasswordHash = encoder.encode("customer123");

            // Check if admin user already exists
            String checkSql = "SELECT id, email FROM users WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, "admin@atozshop.com");
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("⚠️  Admin user already exists with ID: " + rs.getLong("id"));
                System.out.println("   Updating password...");

                // Update password
                String updateSql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE email = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, adminPasswordHash);
                updateStmt.setString(2, "admin@atozshop.com");
                updateStmt.executeUpdate();
                System.out.println("✅ Admin password updated!");
            } else {
                System.out.println("Creating admin user...");

                // Insert admin user
                String insertSql = "INSERT INTO users (tenant_id, email, password, first_name, last_name, role, is_active, created_at, updated_at) " +
                                 "VALUES (1, ?, ?, 'Admin', 'User', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, "admin@atozshop.com");
                insertStmt.setString(2, adminPasswordHash);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    System.out.println("✅ Admin user created with ID: " + generatedKeys.getLong(1));
                }
            }

            // Check if customer user exists
            checkStmt.setString(1, "customer@atozshop.com");
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("⚠️  Customer user already exists with ID: " + rs.getLong("id"));
                System.out.println("   Updating password...");

                String updateSql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE email = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, customerPasswordHash);
                updateStmt.setString(2, "customer@atozshop.com");
                updateStmt.executeUpdate();
                System.out.println("✅ Customer password updated!");
            } else {
                System.out.println("Creating customer user...");

                String insertSql = "INSERT INTO users (tenant_id, email, password, first_name, last_name, role, is_active, created_at, updated_at) " +
                                 "VALUES (1, ?, ?, 'Customer', 'User', 'CUSTOMER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, "customer@atozshop.com");
                insertStmt.setString(2, customerPasswordHash);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    System.out.println("✅ Customer user created with ID: " + generatedKeys.getLong(1));
                }
            }

            // Verify users
            System.out.println("\n📋 All users in database:");
            String selectSql = "SELECT id, email, role, is_active FROM users ORDER BY id";
            Statement stmt = conn.createStatement();
            ResultSet allUsers = stmt.executeQuery(selectSql);

            while (allUsers.next()) {
                System.out.printf("   ID: %d | Email: %s | Role: %s | Active: %s%n",
                    allUsers.getLong("id"),
                    allUsers.getString("email"),
                    allUsers.getString("role"),
                    allUsers.getBoolean("is_active") ? "Yes" : "No"
                );
            }

            System.out.println("\n✅ Test users ready!");
            System.out.println("\n📝 Login Credentials:");
            System.out.println("   Admin:");
            System.out.println("     Email: admin@atozshop.com");
            System.out.println("     Password: admin123");
            System.out.println();
            System.out.println("   Customer:");
            System.out.println("     Email: customer@atozshop.com");
            System.out.println("     Password: customer123");

            conn.close();
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
