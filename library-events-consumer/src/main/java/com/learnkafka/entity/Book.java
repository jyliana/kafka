package com.learnkafka.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {

  @Id
  private Integer id;
  private String name;
  private String author;

  @OneToOne
  @JoinColumn(name = "libraryEvent_id")
  private LibraryEvent libraryEvent;

}
