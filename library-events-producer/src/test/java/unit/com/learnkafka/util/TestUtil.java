package com.learnkafka.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;

import java.time.LocalDateTime;

public class TestUtil {

  private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now().withNano(0);

  public static Book bookRecord() {
	return new Book(123, "Test name", "Test author");
  }

  public static Book bookRecordWithInvalidValues() {
	return new Book(null, "", "Test author");
  }

  public static LibraryEvent libraryEventRecord() {
	return new LibraryEvent(null, LibraryEventType.NEW, LOCAL_DATE_TIME, bookRecord());
  }

  public static LibraryEvent newLibraryEventRecordWithLibraryEventId() {
	return new LibraryEvent(123, LibraryEventType.NEW, LOCAL_DATE_TIME, bookRecord());
  }

  public static LibraryEvent libraryEventRecordUpdate() {
	return new LibraryEvent(123, LibraryEventType.UPDATE, LOCAL_DATE_TIME, bookRecord());
  }

  public static LibraryEvent libraryEventRecordUpdateWithNullLibraryEventId() {
	return new LibraryEvent(null, LibraryEventType.UPDATE, LOCAL_DATE_TIME, bookRecord());
  }

  public static LibraryEvent libraryEventRecordWithInvalidBook() {
	return new LibraryEvent(null, LibraryEventType.NEW, LOCAL_DATE_TIME, bookRecordWithInvalidValues());
  }

  public static LibraryEvent parseLibraryEventRecord(ObjectMapper objectMapper, String json) {
	try {
	  return objectMapper.readValue(json, LibraryEvent.class);
	} catch (JsonProcessingException e) {
	  throw new RuntimeException(e);
	}
  }

}
