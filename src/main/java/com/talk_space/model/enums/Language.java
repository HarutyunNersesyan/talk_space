package com.talk_space.model.enums;

public enum Language {

    ARMENIAN("arm"),
    ENGLISH("en"),
    SPANISH("es"),
    FRENCH("fr"),
    GERMAN("de"),
    CHINESE("zh"),
    JAPANESE("ja"),
    RUSSIAN("ru"),
    ARABIC("ar"),
    PORTUGUESE("pt"),
    HINDI("hi"),
    KOREAN("ko"),
    ITALIAN("it"),
    PERSIAN("fa"),
    SWEDISH("sv"),
    POLISH("pl"),
    DUTCH("nl"),
    NORWEGIAN("no"),
    DANISH("da"),
    FINNISH("fi"),
    CZECH("cs"),
    HUNGARIAN("hu"),
    GREEK("el"),
    ROMANIAN("ro"),
    BULGARIAN("bg"),
    SERBIAN("sr"),
    CROATIAN("hr"),
    SLOVENE("sl"),
    SLOVAK("sk"),
    ALBANIAN("sq"),
    ESTONIAN("et"),
    LATVIAN("lv"),
    LITHUANIAN("lt"),
    MALAY("ms"),
    INDONESIAN("id"),
    TAGALOG("tl"),
    THAI("th"),
    URDU("ur"),
    BENGALI("bn"),
    TELUGU("te"),
    MARATHI("mr"),
    GUJARATI("gu"),
    KANADA("kn"),
    MALAYALAM("ml"),
    ORIYA("or"),
    ASSAMESE("as"),
    PUNJABI("pa"),
    SINDHI("sd"),
    SINHALA("si"),
    TAMIL("ta"),
    MALDIVIAN("dv"),
    UNKNOWN("unknown");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
