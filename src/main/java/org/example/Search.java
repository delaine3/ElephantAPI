package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
public class Search {
    private String value; // Search value

    @JsonCreator
    public Search(
            @JsonProperty("value") String value) {
        this.value = value;
    }
}


