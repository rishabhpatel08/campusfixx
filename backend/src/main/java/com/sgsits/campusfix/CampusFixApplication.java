package com.sgsits.campusfix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CampusFixApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusFixApplication.class, args);
        System.out.println("""
            ╔══════════════════════════════════════════════════╗
            ║  CampusFix — SGSITS Indore                       ║
            ║  Campus Complaint Management System v7           ║
            ║  Auth: Shadow Table + OTP (Passwordless)         ║
            ║  http://localhost:8080                            ║
            ╚══════════════════════════════════════════════════╝
            """);
    }
}
