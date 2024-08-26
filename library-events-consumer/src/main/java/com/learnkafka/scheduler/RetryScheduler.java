package com.learnkafka.scheduler;

import com.learnkafka.config.LibraryEventsConsumerConfig;
import com.learnkafka.entity.FailureRecord;
import com.learnkafka.jpa.FailureRecordRepository;
import com.learnkafka.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryScheduler {

  private final FailureRecordRepository failureRecordRepository;

  private final LibraryEventsService libraryEventsService;

  public RetryScheduler(FailureRecordRepository failureRecordRepository, LibraryEventsService libraryEventsService) {
	this.failureRecordRepository = failureRecordRepository;
	this.libraryEventsService = libraryEventsService;
  }

  @Scheduled(fixedRate = 10000)
  public void retryFailedRecords() {
//	log.info("Retrying failed records started!");

	failureRecordRepository.findAllByStatus(LibraryEventsConsumerConfig.RETRY)
			.forEach(failureRecord -> {
			  log.info("Retrying failed record : {}", failureRecord);
			  var consumerRecord = buildConsumerRecord(failureRecord);
			  try {
				libraryEventsService.processLibraryEvent(consumerRecord);
				failureRecord.setStatus(LibraryEventsConsumerConfig.SUCCESS);
			  } catch (Exception e) {
				log.error("Exception in retryFailedRecords : {}", e.getMessage(), e);
			  }
			});

//	log.info("Retrying failed records completed!");
  }

  private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {
	return new ConsumerRecord<>(
			failureRecord.getTopic(),
			failureRecord.getPartition(),
			failureRecord.getOffset_value(),
			failureRecord.getKey_value(),
			failureRecord.getErrorRecord()
	);
  }

}
