package com.yaxim.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum UserRole {
    MEMBER("MEMBER"),
    LEADER("LEADER"),
    USER("USER"),
    ADMIN(combine("ADMIN", "USER", "MEMBER", "LEADER"));

    @JsonValue
    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    public static String combine(String... names) {
        return String.join(",", names);
    }

    private static final Map<String, UserRole> BY_LABEL =
            Stream.of(values()).collect(Collectors.toMap(UserRole::getName, e -> e));

    public static UserRole of(String name) {
        return BY_LABEL.get(name);
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isLeader() {
        return this == LEADER;
    }

    @JsonCreator
    public static UserRole from(String val) {
        for (UserRole role : UserRole.values()) {
            if (role.name().equals(val)) {
                return role;
            }
        }
        return null;
    }
}
