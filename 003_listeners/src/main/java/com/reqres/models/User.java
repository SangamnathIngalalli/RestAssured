package com.reqres.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonProperty("data")
    private UserData userData;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("how_to_get_one")
    private String howToGetOne;

    public UserData getUserData() {
        return userData;
    }

    public String getError() {
        return error;
    }

    public String getHowToGetOne() {
        return howToGetOne;
    }

    public boolean hasError() {
        return error != null;
    }
} 