package com.cleartax.training_superheroes.services;

import com.cleartax.training_superheroes.config.SqsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Service
public class SuperheroConsumer {

  @Autowired
  private SqsConfig sqsConfig;

  @Autowired
  private SqsClient sqsClient;

  @Scheduled(fixedDelay = 5000) // Poll every 2 seconds
  public void pollMessages() {
    System.out.println("Polling for messages...");

    ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(sqsConfig.getQueueUrl())
            .maxNumberOfMessages(10)
            .waitTimeSeconds(10)
            .build());

    List<Message> messages = receiveMessageResponse.messages();

    if (messages.isEmpty()) {
      System.out.println("No messages received");
      return;
    }

    for (Message message : messages) {
      processMessage(message);
    }
  }

  private void processMessage(Message message) {
    try {
      // Process the message body
      System.out.println("Processing message: " + message.body());

      // Delete the message after successful processing
      deleteMessage(message.receiptHandle());
    } catch (Exception e) {
      System.err.println("Error processing message: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void deleteMessage(String receiptHandle) {
    try {
      DeleteMessageResponse deleteMessageResponse = sqsClient.deleteMessage(DeleteMessageRequest.builder()
              .queueUrl(sqsConfig.getQueueUrl())
              .receiptHandle(receiptHandle)
              .build());
      System.out.println("Message deleted: " + deleteMessageResponse);
    } catch (Exception e) {
      System.err.println("Error deleting message: " + e.getMessage());
      e.printStackTrace();
    }
  }

}
