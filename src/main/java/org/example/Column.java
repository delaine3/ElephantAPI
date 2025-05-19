package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
public class Column {
    private String data;
    private String name;
    private boolean searchable;
    private boolean orderable;
    private Search search;
    

    @JsonCreator
    public Column(
            @JsonProperty("data") String data,
            @JsonProperty("name") String name,
            @JsonProperty("searchable") boolean searchable,
            @JsonProperty("orderable") boolean orderable,
            @JsonProperty("search") Search search) {
        this.data = data;
        this.name = name;
        this.searchable = searchable;
        this.orderable = orderable;
        this.search = search;
    }
}