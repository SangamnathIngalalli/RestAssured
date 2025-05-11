package com.example.model;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    
        
        @JsonProperty("street")
        private String street;
        
        @JsonProperty("city")
        private String city;
        
        @JsonProperty("zipCode")
        private String zipCode;
    }

