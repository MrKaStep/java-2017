package ru.mipt.java2017.hw3.models;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "books_id_seq")
  @SequenceGenerator(
      name = "books_id_seq",
      sequenceName = "books_id_seq",
      allocationSize = 1)
  @Column(name = "ID")
  private Long id;

  @Column(name = "ISBN")
  private BigDecimal isbn;

  @Column(name = "Title")
  private String title;

  @Column(name = "Cover")
  private String coverLink;

  static public String getIdColumnName() {
    try {
      return Book.class.getDeclaredField("id").getAnnotation(Column.class).name();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return null;
    }
  }

  static public String getIsbnColumnName() {
    try {
      return Book.class.getDeclaredField("isbn").getAnnotation(Column.class).name();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return null;
    }
  }

  static public String getTitleColumnName() {
    try {
      return Book.class.getDeclaredField("title").getAnnotation(Column.class).name();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return null;
    }
  }

  static public String getCoverLinkColumnName() {
    try {
      return Book.class.getDeclaredField("coverLink").getAnnotation(Column.class).name();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Long getId() {
    return id;
  }

  public BigDecimal getIsbn() {
    return isbn;
  }

  public void setIsbn(BigDecimal isbn) {
    this.isbn = isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCoverLink() {
    return coverLink;
  }

  public void setCoverLink(String coverLink) {
    this.coverLink = coverLink;
  }
}
