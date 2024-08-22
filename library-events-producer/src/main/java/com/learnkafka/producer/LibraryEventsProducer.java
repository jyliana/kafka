package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class LibraryEventsProducer {
  @Value("${spring.kafka.topic}")
  public String topic;

  private final KafkaTemplate<Integer, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public LibraryEventsProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
	this.kafkaTemplate = kafkaTemplate;
	this.objectMapper = objectMapper;
  }

  public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
	var key = libraryEvent.libraryEventId();
	var value = objectMapper.writeValueAsString(libraryEvent);

	var completableFuture = kafkaTemplate.send(topic, key, value);

	return completableFuture.whenComplete((sendResult, throwable) -> {
	  if (throwable != null) {
		handleFailure(key, value, throwable);
	  } else {
		handleSuccess(key, value, sendResult);
	  }
	});
  }

  private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
	log.info("Message sent successfully for the key : {} and the value is {} , partition is {}",
			key, value, result.getRecordMetadata().partition());
  }

  private void handleFailure(Integer key, String value, Throwable ex) {
	log.error("Error sending the message and the exception is {} :", ex.getMessage(), ex);
  }

}
