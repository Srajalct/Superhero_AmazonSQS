package com.cleartax.training_superheroes.controllers;
import com.cleartax.training_superheroes.config.SqsConfig;
import com.cleartax.training_superheroes.entities.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.services.SqsService;
import com.cleartax.training_superheroes.services.SuperheroConsumer;
import com.cleartax.training_superheroes.services.SuperheroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SuperheroController {

    private final SuperheroService superheroService;
    private final SqsClient sqsClient;
    private final SqsConfig sqsConfig;
    private final SuperheroConsumer superheroConsumer;
    private final SqsService sqsService;

    @Autowired
    public SuperheroController(SuperheroService superheroService, SqsClient sqsClient, SqsConfig sqsConfig, SuperheroConsumer superheroConsumer, SqsService sqsService) {
        this.superheroService = superheroService;
        this.sqsClient = sqsClient;
        this.sqsConfig = sqsConfig;
        this.superheroConsumer = superheroConsumer;
        this.sqsService=sqsService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "username", defaultValue = "World") String username) {
        SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(sqsConfig.getQueueUrl())
                .messageBody("SpiderMan")
                .build());

        return String.format("Hello %s! Message sent with ID: %s", username, response.messageId());
    }

    @GetMapping("/update_superhero_async")
    public String updateSuperhero(@RequestParam(value = "superHeroName", defaultValue = "ironMan") String superHeroName)
    {
        SendMessageResponse result = sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(sqsConfig.getQueueUrl())
                .messageBody(superHeroName)
                .build());

        return String.format("Message sent to queue with message id %s and superHero %s", result.messageId(), superHeroName);
    }

    @PostMapping("/push_superheroes")
    public String pushAllSuperheroes()
    {
        superheroService.pushAllSuperheroesToQueue(sqsConfig.getQueueUrl());
        return "All superheroes have been pushed to the queue.";
    }



    @PostMapping("/add_superhero")
    public String addSuperhero(@RequestBody Superhero superhero) {
        try {
            String messageBody = new ObjectMapper().writeValueAsString(superhero);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(sqsConfig.getQueueUrl())
                    .messageBody(messageBody)
                    .build());

            return String.format("Superhero %s added to the queue!", superhero.getName());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }




    @GetMapping("/get_message_from_queue")
    public List<String> getMessage() {
        List<String> messages_output=new ArrayList<>();
        List<Message> res = sqsService.receiveMessages("http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/superhero-queue");
        for(Message message:res){
            System.out.println(message.body());
            messages_output.add(message.body());
        }
        return messages_output;
    }


    @PostMapping("/superhero")
    public Superhero persistSuperhero(@RequestBody SuperheroRequestBody superheroRequestBody) {
        return superheroService.persistSuperhero(superheroRequestBody, superheroRequestBody.getUniverse());
    }
}
