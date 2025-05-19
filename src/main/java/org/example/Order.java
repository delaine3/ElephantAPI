package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
public class Order {
    private int column; // Index of the column to order by
    private String dir; // Order direction ("ASC" or "DESC")

    // Constructor with JSON properties
    @JsonCreator
    public Order(
            @JsonProperty("column") int column,
            @JsonProperty("dir") String dir) {
        this.column = column;
        this.dir = dir;
    }
}