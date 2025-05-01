package com.reqres.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class User {

    @JsonProperty("data")
    private UserData userData;

    @JsonProperty("support")
    private Support support;
}