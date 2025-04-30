package com.reqres.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserData {
    private int id;
    
    @JsonProperty("first_name")
    private String first_name;
    
    @JsonProperty("last_name")
    private String last_name;
    private String email;

    public int getId() {
        return id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getEmail() {
        return email;
    }
} 