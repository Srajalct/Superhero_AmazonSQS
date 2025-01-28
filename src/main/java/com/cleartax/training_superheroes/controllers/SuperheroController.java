package com.cleartax.training_superheroes.controllers;


import com.cleartax.training_superheroes.config.SqsConfig;
import com.cleartax.training_superheroes.entities.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.services.SuperheroConsumer;
import com.cleartax.training_superheroes.services.SuperheroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@RestController
public class SuperheroController {

    private SuperheroService superheroService;

    @Autowired
    private SqsConfig sqsConfig;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private SuperheroConsumer superheroConsumer;

    @Autowired
    public SuperheroController(SuperheroService superheroService, SqsClient sqsClient){
        this.superheroService = superheroService;
        this.sqsClient = sqsClient;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "username", defaultValue = "World") String username) {
        sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(sqsConfig.getQueueUrl())
            .messageBody("SpiderMan")
            .build());


        return String.format("Hello %s!, %s", username);
    }

    @GetMapping("/update_superhero_async")
    public String updateSuperhero(@RequestParam(value = "superHeroName", defaultValue = "ironMan") String superHeroName) {
        SendMessageResponse result = sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(sqsConfig.getQueueUrl())
            .messageBody(superHeroName)
            .build());

        return String.format("Message sent to queue with message id %s and superHero %s", result.messageId(), superHeroName);
    }


    @GetMapping("/get_message_from_queue")
    public String getMessage() {
       return superheroConsumer.consumeSuperhero();
    }

//    @GetMapping("/superhero")
//    public Superhero getSuperhero(@RequestParam(value = "name", defaultValue = "Batman") String name,
//                                  @RequestParam(value = "universe", defaultValue = "DC") String universe){
//        return superheroService.getSuperhero(name, universe);
//    }

    @PostMapping("/superhero")
    public Superhero persistSuperhero(@RequestBody SuperheroRequestBody superheroRequestBody){
        return superheroService.persistSuperhero(superheroRequestBody, superheroRequestBody.getUniverse());
    }
}
