package com.learnkafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.producer.LibraryEventsProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.learnkafka.util.TestUtil.libraryEventRecord;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
class LibraryEventsControllerUnitTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  LibraryEventsProducer libraryEventsProducer;

  @Test
  void postLibraryEvent() throws Exception {
	//given
	var json = objectMapper.writeValueAsString(libraryEventRecord());
	when(libraryEventsProducer.sendLibraryEventRecordApproach(isA(LibraryEvent.class)))
			.thenReturn(null);

	//when
	var result = mockMvc.perform(MockMvcRequestBuilders.post("/v1/libraryevent")
			.content(json)
			.contentType(MediaType.APPLICATION_JSON));

	result.andExpect(status().isCreated());

	//then
  }

}
