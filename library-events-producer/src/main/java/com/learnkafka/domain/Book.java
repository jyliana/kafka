package com.learnkafka.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Book(
		Integer bookId,
		String bookName,
		String bookAuthor) {
}
