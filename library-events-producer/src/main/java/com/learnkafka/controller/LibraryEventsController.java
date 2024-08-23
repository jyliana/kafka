package com.learnkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.producer.LibraryEventsProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
public class LibraryEventsController {

  private final LibraryEventsProducer libraryEventsProducer;

  public LibraryEventsController(LibraryEventsProducer libraryEventsProducer) {
	this.libraryEventsProducer = libraryEventsProducer;
  }

  @PostMapping("/v1/libraryevent")
  public ResponseEntity<?> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
	log.info("libraryEvent : {}", libraryEvent);

	//invoke kafka producer
	libraryEventsProducer.sendLibraryEventRecordApproach(libraryEvent);

	log.info("After sending libraryEvent :");

	return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
  }

}
