package com.atozshop.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=== Password Hash Generator ===");
        System.out.println();
        System.out.println("admin123 hash: " + encoder.encode("admin123"));
        System.out.println("customer123 hash: " + encoder.encode("customer123"));
        System.out.println();
        System.out.println("Use these hashes in your database INSERT statements");
    }
}
