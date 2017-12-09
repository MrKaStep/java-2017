package ru.mipt.java2017.hw3.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Books_Authors")
public class BookAuthorRelation {

  @Id
  @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
  @Column(name = "ID")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "books_id")
  private Book book;

  @ManyToOne
  @JoinColumn(name = "authors_id")
  private Author author;

  @Column(name = "num")
  private Long order;

  public Long getId() {
    return id;
  }

  public Long getOrder() {
    return order;
  }

  public void setOrder(Long order) {
    this.order = order;
  }

  public Author getAuthor() {
    return author;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }

  public Book getBook() {
    return book;
  }

  public void setBook(Book book) {
    this.book = book;
  }
}
