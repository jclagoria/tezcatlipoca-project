package com.random.data.adapter.outbound.provider;

public final class Person {

    private final String name;
    private final String email;
    private final String locale;

    public Person(String name, String email, String locale) {
        this.name = name;
        this.email = email;
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLocale() {
        return locale;
    }
}
