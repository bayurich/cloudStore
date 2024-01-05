package ru.netology.netologydiplombackend.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {
    @JsonProperty(value = "auth-token")
    String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                '}';
    }
}
