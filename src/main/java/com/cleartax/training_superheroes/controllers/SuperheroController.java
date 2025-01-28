package com.cleartax.training_superheroes.controllers;
import com.cleartax.training_superheroes.config.SqsConfig;
import com.cleartax.training_superheroes.entities.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.services.SqsService;
import com.cleartax.training_superheroes.services.SuperheroConsumer;
import com.cleartax.training_superheroes.services.SuperheroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

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

    @GetMapping("/update_SuperHero_async")
    public String updateSuperHero(@RequestParam(value = "superHeroName", defaultValue = "thor") String superHeroName) {
        String queueUrl = "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/superhero-queue";

        try {
            List<Message> messages = sqsService.receiveMessages(queueUrl);

            if (messages.isEmpty()) {
                return "No messages in the queue to update!";
            }

            for (Message message : messages)
            {
                System.out.println("Original Message: " + message.body());
                sqsService.deleteMessage(queueUrl, message.receiptHandle());
                System.out.println("Deleted original message with receipt handle: " + message.receiptHandle());
                String updatedMessageBody = message.body() + " - updated with " + superHeroName;
                sqsService.sendMessage(queueUrl, updatedMessageBody);
                System.out.println("Requeued updated message: " + updatedMessageBody);
            }

            return String.format("Updated %d messages in the queue!", messages.size());

        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error processing request: " + e.getMessage();
        }
    }

    @PostMapping("/send_message")
    public String sendMessageToQueue(@RequestBody String superHeroName) {
        String queueUrl = "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/superhero-queue";

        try {
            // Send a message to the SQS queue
            sqsService.sendMessage(queueUrl, superHeroName);
            System.out.println("Message sent to queue: " + superHeroName);

            return String.format("Message '%s' successfully sent to the queue!", superHeroName);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending message: " + e.getMessage();
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
