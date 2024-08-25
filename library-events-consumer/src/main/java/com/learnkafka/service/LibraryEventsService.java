package com.learnkafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.jpa.LibraryEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LibraryEventsService {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private LibraryEventsRepository libraryEventsRepository;

  public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
	var libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
	log.info("libraryEvent : {}", libraryEvent);

	switch (libraryEvent.getType()) {
	  case NEW -> save(libraryEvent);
	  case UPDATE -> {
		validate(libraryEvent);
		save(libraryEvent);
	  }
	  default -> log.info("Invalid Library Event Type");
	}
  }

  private void validate(LibraryEvent libraryEvent) {
	if (null == libraryEvent.getId()) {
	  throw new IllegalArgumentException("LibraryEvent id is missing");
	}
	Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(libraryEvent.getId());

	if (libraryEventOptional.isEmpty()) {
	  throw new IllegalArgumentException("Not a valid library event");
	}
	log.info("Validation is successful for the library Event: {}", libraryEvent);
  }

  private void save(LibraryEvent libraryEvent) {
	libraryEvent.getBook().setLibraryEvent(libraryEvent);
	libraryEventsRepository.save(libraryEvent);
	log.info("Successfully persisted the library Event {} :", libraryEvent);
  }

}
