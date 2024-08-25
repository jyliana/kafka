package com.learnkafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.Book;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.entity.LibraryEventType;
import com.learnkafka.jpa.LibraryEventsRepository;
import com.learnkafka.service.LibraryEventsService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events", "library-events.RETRY", "library-events.DLT"}, partitions = 3)
@TestPropertySource(properties = {
		"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
public class LibraryEventsConsumerIntegrationTest {

  @Value("${topics.retry}")
  private String retryTopic;

  @Value("${topics.dlt}")
  private String deadLetterTopic;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired
  private KafkaTemplate<Integer, String> kafkaTemplate;

  @Autowired
  private KafkaListenerEndpointRegistry endpointRegistry;

  @Autowired
  private LibraryEventsRepository repository;

  @SpyBean
  private LibraryEventsConsumer consumerSpy;

  @SpyBean
  private LibraryEventsService serviceSpy;

  private Consumer<Integer, String> consumer;

  @BeforeEach
  void setUp() {
	for (MessageListenerContainer container : endpointRegistry.getListenerContainers()) {
	  ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
	}
  }

  @AfterEach
  void tearDown() {
	repository.deleteAll();
  }

  @Test
  void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
	//given
	String json = """
			{
			   "id":null,
			   "type":"NEW",
			   "time":"2024-08-23T23:16:43",
			   "book":{
			      "id":456,
			      "name":"Kafka Using Spring Boot v1",
			      "author":"Inna"
			   }
			}
			""";
	kafkaTemplate.sendDefault(json).get();

	//when
	var latch = new CountDownLatch(1);
	latch.await(3, TimeUnit.SECONDS);

	//then
	verify(consumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
	verify(serviceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

	List<LibraryEvent> list = (List<LibraryEvent>) repository.findAll();
	assertEquals(1, list.size());
	list.forEach(event -> {
			  assertNotNull(event.getId());
			  assertEquals(1, event.getId());
			}
	);
  }

  @Test
  void publishUpdatedLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
	//given
	String json = """
			{
			   "id":null,
			   "type":"NEW",
			   "time":"2024-08-23T23:16:43",
			   "book":{
			      "id":456,
			      "name":"Kafka Using Spring Boot v1",
			      "author":"Inna"
			   }
			}
			""";
	var libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
	libraryEvent.getBook().setLibraryEvent(libraryEvent);
	repository.save(libraryEvent);

	var updatedBook = Book.builder()
			.id(456)
			.name("Kafka Using Spring Boot v3")
			.author("Inna L.")
			.build();

	libraryEvent.setType(LibraryEventType.UPDATE);
	libraryEvent.setBook(updatedBook);
	var updatedJson = objectMapper.writeValueAsString(libraryEvent);
	kafkaTemplate.sendDefault(libraryEvent.getId(), updatedJson).get();

	//when
	var latch = new CountDownLatch(1);
	latch.await(3, TimeUnit.SECONDS);

	//then
	var savedLibraryEvent = repository.findById(libraryEvent.getId()).get();
	assertEquals("Kafka Using Spring Boot v3", savedLibraryEvent.getBook().getName());

	verify(consumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
	verify(serviceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
  }

  @Test
  void publishUpdatedLibraryEvent_null_LibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
	//given
	String json = """
			{
			   "id":null,
			   "type":"UPDATE",
			   "time":"2024-08-23T23:16:43",
			   "book":{
			      "id":456,
			      "name":"Kafka Using Spring Boot v1",
			      "author":"Inna"
			   }
			}
			""";
	kafkaTemplate.sendDefault(json).get();

	//when
	var latch = new CountDownLatch(1);
	latch.await(3, TimeUnit.SECONDS);

	//then
	verify(consumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
	verify(serviceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
  }

  @Test
  void publishModifyLibraryEvent_999_LibraryEventId() throws JsonProcessingException, InterruptedException, ExecutionException {
	//given
	String json = """
			{
			   "id":999,
			   "type":"UPDATE",
			   "time":"2024-08-23T23:16:43",
			   "book":{
			      "id":456,
			      "name":"Kafka Using Spring Boot v1",
			      "author":"Inna"
			   }
			}
			""";
	kafkaTemplate.sendDefault(json).get();
	//when
	CountDownLatch latch = new CountDownLatch(1);
	latch.await(3, TimeUnit.SECONDS);


	verify(consumerSpy, times(2)).onMessage(isA(ConsumerRecord.class));
	verify(serviceSpy, times(2)).processLibraryEvent(isA(ConsumerRecord.class));

	var configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
	configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
	consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
			.createConsumer();
	embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, retryTopic);

	var consumerRecord = KafkaTestUtils.getSingleRecord(consumer, retryTopic);
	System.out.println("consumerRecord is : " + consumerRecord.value());
	assertEquals(json, consumerRecord.value());
  }

}
