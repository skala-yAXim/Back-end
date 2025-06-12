package com.yaxim.statics.entity;

import lombok.Getter;

@Getter
public enum Weekday {
    FRIDAY(0),
    SATURDAY(1),
    SUNDAY(2),
    MONDAY(3),
    TUESDAY(4),
    WEDNESDAY(5),
    THURSDAY(6);

    private final int code;

    Weekday(int code) {
        this.code = code;
    }

    public static Weekday of(int code) {
        for (Weekday w : Weekday.values()) {
            if (w.code == code) {
                return w;
            }
        }
        throw new IllegalArgumentException("Invalid weekday code: " + code);
    }
}
