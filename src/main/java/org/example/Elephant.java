package org.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class Elephant {
    private int id;
    private String name;
    private int age;
    private String species;
    private String location;
    private double weight;
    private double height;
    private String healthStatus;
    private Date lastHealthCheckDate;
    private Date birthday;
}
