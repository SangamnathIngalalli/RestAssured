package com.reqres.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {

    @JsonProperty("data")
    private UserData userData;  // Map the 'data' field to the UserData object

    @JsonProperty("support")
    private Support support;  // Map the 'support' field to the Support object

}


@Data
class Support {
    private String url;
    private String text;
}
