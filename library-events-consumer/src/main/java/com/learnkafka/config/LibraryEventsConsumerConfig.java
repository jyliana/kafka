package com.learnkafka.config;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
//@EnableKafka
public class LibraryEventsConsumerConfig {

  @Bean
  ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
		  ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
		  ConsumerFactory<Object, Object> kafkaConsumerFactory) {
	var factory = new ConcurrentKafkaListenerContainerFactory<>();
	configurer.configure(factory, kafkaConsumerFactory);
	factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
	return factory;
  }
}
