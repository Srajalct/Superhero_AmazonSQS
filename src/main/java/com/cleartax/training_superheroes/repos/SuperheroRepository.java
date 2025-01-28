package com.cleartax.training_superheroes.repos;

import com.cleartax.training_superheroes.entities.Superhero;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SuperheroRepository extends MongoRepository<Superhero, String> {
}
