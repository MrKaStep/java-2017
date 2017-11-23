package ru.mipt.java2017.hw3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mipt.java2017.hw3.models.Author;
import ru.mipt.java2017.hw3.models.Book;
import ru.mipt.java2017.hw3.models.BookAuthorRelation;

public class DatabasePrinter {

  private static final Logger logger = LoggerFactory.getLogger("DBPrinter");

  private final EntityManager entityManager;
  private final CriteriaBuilder criteriaBuilder;

  private FileOutputStream outputStream;

  public DatabasePrinter(EntityManager entityManager, String path) {
    this.entityManager = entityManager;
    this.criteriaBuilder = entityManager.getCriteriaBuilder();
    this.outputStream = null;
    try {
      File outputFile = new File(path);
      outputFile.createNewFile();
      this.outputStream = new FileOutputStream(outputFile);
    } catch (IOException e) {
      logger.error("Cannot initialize DBPrinter: {}", e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }


  void printDatabaseContents() throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    logger.info("Building resulting book ...");
    SheetAdder<Book> bookSheetAdder =
        new SheetAdder<>(workbook, entityManager, criteriaBuilder, Book.class);
    bookSheetAdder.add();

    SheetAdder<Author> authorSheetAdder =
        new SheetAdder<>(workbook, entityManager, criteriaBuilder, Author.class);
    authorSheetAdder.add();

    SheetAdder<BookAuthorRelation> authorBookSheetAdder =
        new SheetAdder<>(workbook, entityManager, criteriaBuilder, BookAuthorRelation.class);
    authorBookSheetAdder.add();
    logger.info("Result built!");

    logger.info("Writing result ...");
    workbook.write(outputStream);
    logger.info("Done!");
  }


}
