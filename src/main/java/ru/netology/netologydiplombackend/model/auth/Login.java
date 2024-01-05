package ru.netology.netologydiplombackend.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Login {
    @JsonProperty(value = "login")
    String login;
    @JsonProperty(value = "password")
    String password;

    public Login() {
    }

    public Login(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Login{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
