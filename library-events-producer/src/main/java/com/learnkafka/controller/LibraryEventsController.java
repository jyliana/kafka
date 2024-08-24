package com.learnkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import com.learnkafka.producer.LibraryEventsProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
public class LibraryEventsController {

  private final LibraryEventsProducer libraryEventsProducer;

  public LibraryEventsController(LibraryEventsProducer libraryEventsProducer) {
	this.libraryEventsProducer = libraryEventsProducer;
  }

  @PostMapping("/v1/libraryevent")
  public ResponseEntity<?> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
	log.info("libraryEvent : {}", libraryEvent);

	//invoke kafka producer
	libraryEventsProducer.sendLibraryEventRecordApproach(libraryEvent);

	log.info("After sending libraryEvent :");

	return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
  }

  @PutMapping("/v1/libraryevent")
  public ResponseEntity<?> updateLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
	log.info("libraryEvent : {}", libraryEvent);

	var badRequest = validateLibraryEvent(libraryEvent);
	if (badRequest.isPresent()) {
	  return badRequest.get();
	}

	libraryEventsProducer.sendLibraryEventRecordApproach(libraryEvent);

	return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
  }

  private Optional<ResponseEntity<String>> validateLibraryEvent(LibraryEvent libraryEvent) {
	if (libraryEvent.id() == null) {
	  return Optional.of(ResponseEntity
			  .status(HttpStatus.BAD_REQUEST)
			  .body("Please pass the libraryEvent's id."));
	}

	if (!LibraryEventType.UPDATE.equals(libraryEvent.type())) {
	  return Optional.of(ResponseEntity
			  .status(HttpStatus.BAD_REQUEST)
			  .body("Only UPDATE event type is supported."));
	}
	return Optional.empty();
  }

}
