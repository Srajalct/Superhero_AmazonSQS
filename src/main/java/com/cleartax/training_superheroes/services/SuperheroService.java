package com.cleartax.training_superheroes.services;

import com.cleartax.training_superheroes.config.SqsClientConfig;
import com.cleartax.training_superheroes.entities.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.repos.SuperheroRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;

@Service
public class SuperheroService {

    private final SqsClient sqsClient;
    private final SuperheroRepository superheroRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public SuperheroService(SuperheroRepository superheroRepository, SqsClient sqsClient){
        this.superheroRepository = superheroRepository;
        this.sqsClient=sqsClient;
    }

    public Superhero persistSuperhero(SuperheroRequestBody requestBody, String universe)
    {

        Superhero superhero = new Superhero();
        superhero.setName(requestBody.getSuperheroName().toUpperCase());
        superhero.setPower(requestBody.getPower());
        if(isDC(universe)){
            superhero.setUniverse("Bahubali");
        } else {
            superhero.setUniverse("Marvel");
        }
        Superhero superhero1 =  superheroRepository.save(superhero);
        return superhero1;
    }

    private boolean isDC(String universe)
    {
        if(universe.equals("DC"))
        {
            return true;
        }
        else {
            return false;
        }
    }

    public void pushAllSuperheroesToQueue(String queueUrl)
    {
        List<Superhero> superheroes = superheroRepository.findAll();
        for (Superhero superhero : superheroes)
        {
            try
            {
                String messageBody = objectMapper.writeValueAsString(superhero);
                SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build();
                sqsClient.sendMessage(sendMessageRequest);
            }
            catch (JsonProcessingException e)
            {
                System.err.println("Error serializing superhero: " + e.getMessage());
            }
        }
    }
}
