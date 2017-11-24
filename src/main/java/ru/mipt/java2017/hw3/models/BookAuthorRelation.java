package ru.mipt.java2017.hw3.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "books_authors")
public class BookAuthorRelation {

  @Id
  @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY )
  @Column(name = "ID")
  private Long id;

  @Column(name = "books_id")
  private Long bookId;

  @Column(name = "authors_id")
  private Long authorId;

  @Column(name = "num")
  private Long order;

  public Long getId() {
    return id;
  }

  public Long getBookId() {
    return bookId;
  }

  public void setBookId(Long bookId) {
    this.bookId = bookId;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  public Long getOrder() {
    return order;
  }

  public void setOrder(Long order) {
    this.order = order;
  }
}
