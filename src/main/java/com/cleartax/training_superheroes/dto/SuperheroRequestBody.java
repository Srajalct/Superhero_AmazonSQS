package com.cleartax.training_superheroes.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuperheroRequestBody {
    private String superheroName;
    private String power;
    private String universe;
}