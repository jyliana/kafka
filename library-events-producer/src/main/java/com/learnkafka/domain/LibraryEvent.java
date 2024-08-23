package com.learnkafka.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LibraryEvent(
		Integer libraryEventId,
		LibraryEventType libraryEventType,
		LocalDateTime localDateTime,
		@NotNull
		@Valid
		Book book) {
}
