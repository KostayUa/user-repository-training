package org.example.user.repository.training.model;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {
    private final String value;
    private final static Pattern patternEmail = Pattern.compile("^(.+)@(\\S+)$");

    private Email(String value) {
        this.value = value;
    }

    public static Email create(String value) {
        final String valueTrimmed = value.trim();
        if (isValidEmail(valueTrimmed)) {
            return new Email(valueTrimmed);
        } else {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static boolean isValidEmail(String value) {
        return patternEmail.matcher(value).matches();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "Email(value='" + value + "')";
    }
}
