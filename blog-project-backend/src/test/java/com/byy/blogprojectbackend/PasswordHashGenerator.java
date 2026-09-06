package com.byy.blogprojectbackend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String rawPassword = System.getenv("BINSPACE_OWNER_PASSWORD");

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException(
                    "请先配置环境变量 BINSPACE_OWNER_PASSWORD"
            );
        }

        String passwordHash =
                encoder.encode(rawPassword);

        System.out.println("BCrypt 哈希：" + passwordHash);
    }
}
