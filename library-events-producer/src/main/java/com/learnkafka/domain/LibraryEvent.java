package com.learnkafka.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LibraryEvent(
		Integer id,
		LibraryEventType type,
		LocalDateTime time,
		@NotNull
		@Valid
		Book book) {
}
