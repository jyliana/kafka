package com.learnkafka.config;

import com.learnkafka.service.FailureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Configuration
@Slf4j
public class LibraryEventsConsumerConfig {

  public static final String RETRY = "RETRY";
  public static final String DEAD = "DEAD";
  public static final String SUCCESS = "SUCCESS";
  @Autowired
  private KafkaTemplate kafkaTemplate;

  @Autowired
  private FailureService failureService;

  @Value("${topics.retry}")
  private String retryTopic;

  @Value("${topics.dlt}")
  private String deadLetterTopic;

  public DeadLetterPublishingRecoverer publishingRecoverer() {

	DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {
	  log.error("Exception in publishingRecoverer : {} ", e.getMessage(), e);
	  if (e.getCause() instanceof RecoverableDataAccessException) {
		return new TopicPartition(retryTopic, r.partition());
	  } else {
		return new TopicPartition(deadLetterTopic, r.partition());
	  }
	});

	return recoverer;
  }

  ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, e) -> {
	log.error("Exception in publishingRecoverer : {} ", e.getMessage(), e);
	var record = (ConsumerRecord<Integer, String>) consumerRecord;
	if (e.getCause() instanceof RecoverableDataAccessException) {
	  log.info("Inside Recovery");
	  failureService.saveFailedRecord(record, e, RETRY);
	} else {
	  log.info("Inside Non-Recovery");
	  failureService.saveFailedRecord(record, e, DEAD);
	}
  };


  public DefaultErrorHandler errorHandler() {
	var exceptionsToIgnoreList = List.of(IllegalArgumentException.class);
	var exceptionsToRetryList = List.of(RecoverableDataAccessException.class);

	var fixedBackOff = new FixedBackOff(1000L, 2);

	var expBackOff = new ExponentialBackOffWithMaxRetries(2);
	expBackOff.setInitialInterval(1_000L);
	expBackOff.setMultiplier(2.0);
	expBackOff.setMaxInterval(2_000L);

	var errorHandler = new DefaultErrorHandler(
//			publishingRecoverer(),
			consumerRecordRecoverer,
			expBackOff);

	exceptionsToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);
	exceptionsToRetryList.forEach(errorHandler::addRetryableExceptions);

	errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
			log.info("Failed record in Retry Listener, exception : {} , deliveryAttempt : {}",
					ex.getMessage(), deliveryAttempt));

	return errorHandler;
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
		  ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
		  ConsumerFactory<Object, Object> kafkaConsumerFactory) {
	var factory = new ConcurrentKafkaListenerContainerFactory<>();
	configurer.configure(factory, kafkaConsumerFactory);
//	factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
	factory.setConcurrency(3);
	factory.setCommonErrorHandler(errorHandler());
	return factory;
  }
}
