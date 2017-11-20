package ru.mipt.java2017.hw3.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "Authors")
public class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authors_id_seq")
  @SequenceGenerator(
      name = "authors_id_seq",
      sequenceName = "authors_id_seq",
      allocationSize = 1)
  @Column(name = "ID")
  private Long id;

  @Column(name = "Name")
  private String name;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
