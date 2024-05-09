package com.korea.sbb1.user.account;

import lombok.Getter;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

@Getter
public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    UserRole(String value) {
        this.value = value;
    }

    private String value;
}

