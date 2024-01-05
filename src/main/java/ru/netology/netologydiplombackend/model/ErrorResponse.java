package ru.netology.netologydiplombackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    @JsonProperty(value = "message")
    private String message;
    @JsonProperty(value = "id")
    private int id;

    public ErrorResponse(String message, int id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", id=" + id +
                '}';
    }
}
