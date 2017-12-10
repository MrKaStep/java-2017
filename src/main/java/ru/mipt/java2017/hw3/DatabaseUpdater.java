package ru.mipt.java2017.hw3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mipt.java2017.hw3.ExcelDataSource.BookWithAuthors;
import ru.mipt.java2017.hw3.models.Author;
import ru.mipt.java2017.hw3.models.Book;
import ru.mipt.java2017.hw3.models.BookAuthorRelation;

public class DatabaseUpdater {

  private static final Logger logger = LoggerFactory.getLogger("DBUpdater");

  protected final EntityManager entityManager;
  protected final CriteriaBuilder criteriaBuilder;

  DatabaseUpdater(DatabaseAccess databaseAccess) {
    entityManager = databaseAccess.entityManager;
    criteriaBuilder = this.entityManager.getCriteriaBuilder();
  }

  protected Map<String, Author> addAuthors(List<String> authorNames) {
    Map<String, Author> idByName = new HashMap<>();
    List<Author> authors = new ArrayList<>(authorNames.size());
    logger.info("Adding authors ...");
    entityManager.getTransaction().begin();
    authorNames.forEach(authorName -> {
      Author author = new Author();
      author.setName(authorName);
      entityManager.persist(author);
      authors.add(author);
    });
    entityManager.getTransaction().commit();
    logger.info("Done!");
    authors.forEach(author -> idByName.put(author.getName(), author));
    return idByName;
  }

  protected Map<BigDecimal, Book> addBooks(List<Book> books) {
    Map<BigDecimal, Book> idByIsbn = new HashMap<>();
    logger.info("Adding books ...");
    entityManager.getTransaction().begin();
//    books.forEach(this::addBook);
    books.forEach(book -> {
      CriteriaQuery<Book> query = criteriaBuilder.createQuery(Book.class);
      Root<Book> root = query.from(Book.class);
      Predicate predicate = criteriaBuilder.equal(root.get("isbn"), book.getIsbn());
      query.where(predicate);
      try {
        Book foundBook = entityManager.createQuery(query).getSingleResult();
        foundBook.setTitle(book.getTitle());
        foundBook.setCoverLink(book.getCoverLink());
        entityManager.merge(foundBook);
        idByIsbn.put(foundBook.getIsbn(), foundBook);
      } catch (NoResultException e) {
        entityManager.persist(book);
        idByIsbn.put(book.getIsbn(), book);
      }
    });
    entityManager.getTransaction().commit();
    logger.info("Done!");
    return idByIsbn;
  }

  protected void addBookAuthorRelations(
      Map<String, Author> authorByName,
      Map<BigDecimal, Book> bookByIsbn,
      List<BookWithAuthors> booksWithAuthors) {
    logger.info("Adding author-book entries ...");
    booksWithAuthors.forEach(bookWithAuthors -> {

      logger.debug("Adding authors for book ISBN {} ...", bookWithAuthors.getBookIsbn());
      entityManager.getTransaction().begin();

      long order = 0;
      for (String name : bookWithAuthors.getAuthorNames()) {
        ++order;

        BookAuthorRelation relation = new BookAuthorRelation();
        relation.setBook(bookByIsbn.get(bookWithAuthors.getBookIsbn()));
        relation.setAuthor(authorByName.get(name));
        relation.setOrder(order);
        entityManager.persist(relation);
      }

      entityManager.getTransaction().commit();
      logger.debug("Done!");
    });
    logger.info("Done!");
  }

  protected void updateDatabase(List<BookWithAuthors> booksWithAuthors) {
    Set<String> authorNames = new HashSet<>();
    List<Book> books = new ArrayList<>(booksWithAuthors.size());

    booksWithAuthors.forEach(bookWithAuthors -> {
      authorNames.addAll(bookWithAuthors.getAuthorNames());
      Book book = new Book();
      book.setIsbn(bookWithAuthors.getBookIsbn());
      book.setTitle(bookWithAuthors.getBookTitle());
      books.add(book);
    });

    Map<String, Author> authorIdByName = addAuthors(new ArrayList<>(authorNames));
    Map<BigDecimal, Book> bookIdByIsbn = addBooks(books);
    addBookAuthorRelations(authorIdByName, bookIdByIsbn, booksWithAuthors);
  }

  protected void printDatabase(String outputFile) {

    DatabasePrinter printer = new DatabasePrinter(entityManager, outputFile);
    try {
      printer.printDatabaseContents();
    } catch (IOException e) {
      logger.error("Cannot print DB to excel file: {}", e.getMessage());
      e.printStackTrace();
    }
  }

  public void close() {
    entityManager.close();
  }

  public static void main(String[] args) {
    DatabaseAccess databaseAccess = new DatabaseAccess(args[0]);
    DatabaseUpdater updater = new DatabaseUpdater(databaseAccess);
    ExcelDataSource excelDataSource = null;
    try {
      excelDataSource = ExcelDataSource.createExcelDataSource(
          new FileInputStream(new File(args[1])));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    updater.updateDatabase(excelDataSource.getEntries());
    updater.printDatabase(args[2]);
    updater.close();
    databaseAccess.close();
    System.exit(0);
  }
}
