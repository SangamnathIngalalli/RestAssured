package com.example.two.model;

import com.fasterxml.jackson.annotation.JsonProperty;


@Data
public class Address {

    
        
        @JsonProperty("street")
        private String street;
        
        @JsonProperty("city")
        private String city;
        
        @JsonProperty("zipCode")
        private String zipCode;
    }

