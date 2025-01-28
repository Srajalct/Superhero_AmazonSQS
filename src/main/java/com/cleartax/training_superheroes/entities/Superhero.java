package com.cleartax.training_superheroes.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "superheroes")  // MongoDB collection name
public class Superhero {

    @Id
    private String id;
    private String name;
    private String power;
    private String universe;
}
