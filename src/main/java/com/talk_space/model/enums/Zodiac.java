package com.talk_space.model.enums;

public enum Zodiac {
    ARIES("Aries", 3, 21),
    TAURUS("Taurus", 4, 20),
    GEMINI("Gemini", 5, 21),
    CANCER("Cancer", 6, 21),
    LEO("Leo", 7, 23),
    VIRGO("Virgo", 8, 23),
    LIBRA("Libra", 9, 23),
    SCORPIO("Scorpio", 10, 23),
    SAGITTARIUS("Sagittarius", 11, 22),
    CAPRICORN("Capricorn", 12, 22),
    AQUARIUS("Aquarius", 1, 20),
    PISCES("Pisces", 2, 19);

    private final String name;
    private final int month;
    private final int day;

    Zodiac(String name, int month, int day) {
        this.name = name;
        this.month = month;
        this.day = day;
    }

    public String getName() {
        return name;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public static Zodiac fromMonthAndDay(int month, int day) {
        for (int i = 0; i < Zodiac.values().length; i++) {
            Zodiac current = Zodiac.values()[i];
            Zodiac next = Zodiac.values()[(i + 1) % Zodiac.values().length]; // Get next sign (wrap around at end)

            int currentMonth = current.getMonth();
            int currentDay = current.getDay();
            int nextMonth = next.getMonth();
            int nextDay = next.getDay();

            if ((month == currentMonth && day >= currentDay) || (month == nextMonth && day < nextDay)) {
                return current;
            }
        }
        return null; // Should never reach here
    }

}
