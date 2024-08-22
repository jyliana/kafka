package com.learnkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.domain.LibraryEvent;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@Slf4j
@RestController
public class LibraryEventsController {

  @PostMapping("/v1/libraryevent")
  public ResponseEntity<?> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) {
	//invoke kafka producer
	log.info("libraryEvent : {}", libraryEvent);

	return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
  }

}
