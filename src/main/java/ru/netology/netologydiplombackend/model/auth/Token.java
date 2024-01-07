package ru.netology.netologydiplombackend.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {
    @JsonProperty(value = "auth-token")
    String tokenValue;

    public Token() {
    }

    public Token(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenValue='" + tokenValue + '\'' +
                '}';
    }
}
