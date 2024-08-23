package com.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.LibraryEventsProducerApplication;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.util.TestUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static com.learnkafka.util.TestUtil.libraryEventRecord;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
@SpringBootTest(classes = LibraryEventsProducerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryEventsControllerIntegrationTest {

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  EmbeddedKafkaBroker kafkaBroker;

  @Autowired
  ObjectMapper objectMapper;

  private Consumer<Integer, String> consumer;

  @BeforeEach
  void setUp() {
	var configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", kafkaBroker));
	configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
	consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
			.createConsumer();
	kafkaBroker.consumeFromAllEmbeddedTopics(consumer);
  }

  @AfterEach
  void tearDown() {
	consumer.close();
  }

  @Test
  void postLibraryEvent() {
	//given
	var httpHeaders = new HttpHeaders();
	httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
	var httpEntity = new HttpEntity<>(libraryEventRecord(), httpHeaders);

	//when
	var responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, httpEntity, LibraryEvent.class);

	//then
	var consumerRecords = KafkaTestUtils.getRecords(consumer);

	assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	assertEquals(1, consumerRecords.count());
	consumerRecords.forEach(record -> {
	  var libraryEventActual = TestUtil.parseLibraryEventRecord(objectMapper, record.value());
	  System.out.println("libraryEventActual: " + libraryEventActual);
	  assertEquals(libraryEventActual, libraryEventRecord());
	});
  }

}
