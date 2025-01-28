package com.cleartax.training_superheroes.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class SqsConfig {

  @Value("${aws.sqs.endpoint}")
  private String sqsEndpoint;

  @Value("${aws.sqs.queueName}")
  private String queueName;

  public String getQueueUrl() {
    return String.format("%s/000000000000/%s", sqsEndpoint, queueName);
  }

}

