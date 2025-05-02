package com.reqres.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Support {

    @JsonProperty("url")
    private String url;

    @JsonProperty("text")
    private String text;
}