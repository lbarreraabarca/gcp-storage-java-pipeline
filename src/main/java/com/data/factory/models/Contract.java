package com.data.factory.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Contract {

    @JsonProperty
    private String landingPath;

    @JsonProperty
    private String localPath;

}
