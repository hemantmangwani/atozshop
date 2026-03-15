package com.atozshop.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=== Password Hash Generator ===");
        System.out.println();

        String adminHash = encoder.encode("admin123");
        String customerHash = encoder.encode("customer123");

        System.out.println("admin123 hash:");
        System.out.println(adminHash);
        System.out.println();

        System.out.println("customer123 hash:");
        System.out.println(customerHash);
        System.out.println();

        System.out.println("SQL UPDATE statements:");
        System.out.println("UPDATE users SET password_hash = '" + adminHash + "' WHERE email = 'admin@atozshop.com';");
        System.out.println("UPDATE users SET password_hash = '" + customerHash + "' WHERE email = 'customer@atozshop.com';");
    }
}
