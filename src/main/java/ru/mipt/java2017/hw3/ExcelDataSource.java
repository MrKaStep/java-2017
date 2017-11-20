package ru.mipt.java2017.hw3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mipt.java2017.hw3.models.Book;

public class ExcelDataSource {
  private static final Logger logger = LoggerFactory.getLogger("excel");
  private final Workbook workbook;

  private final int titleColumn;
  private final int authorsColumn;
  private final int isbnColumn;
  private final int rowsCount;

  private ExcelDataSource(InputStream inputStream) throws IOException {
    workbook = new XSSFWorkbook(inputStream);
    Sheet sheet = workbook.getSheetAt(0);
    Row row = sheet.getRow(0);
    int titleColumn = -1;
    int authorsColumn = -1;
    int isbnColumn = -1;
    for (int i = 0; i < 3; ++i) {
      String columnHead = row.getCell(i).getStringCellValue();
      if (columnHead.equals("Title"))
        titleColumn = i;
      else if (columnHead.equals("Authors"))
        authorsColumn = i;
      else
        isbnColumn = i;
    }

    this.titleColumn = titleColumn;
    this.authorsColumn = authorsColumn;
    this.isbnColumn = isbnColumn;

    int maxRowCount = sheet.getPhysicalNumberOfRows();
    int rowsCount = 0;
    for (int i = 0; i < maxRowCount; ++i) {
      String contents = sheet.getRow(i).getCell(titleColumn).getStringCellValue();
      if (contents != null && contents.length() > 0)
        ++rowsCount;
      else
        break;
    }

    this.rowsCount = rowsCount;
  }

  public static ExcelDataSource createExcelDataSource(InputStream inputStream) {
    try {
      return new ExcelDataSource(inputStream);
    } catch (IOException e) {
      logger.error("IOError: {}", e.getMessage());
      return null;
    }
  }

  public List<Book> getBooks() {
    List<Book> books = new ArrayList<>(rowsCount - 1);
    Sheet sheet = workbook.getSheetAt(0);
    for (int i = 1; i < rowsCount; ++i) {
      Book book = new Book();
      Row row = sheet.getRow(i);
      book.setTitle(row.getCell(titleColumn).getStringCellValue());
      book.setIsbn(new BigDecimal(
          row.getCell(isbnColumn).getStringCellValue().substring(8, 21)
      ));
      books.add(book);
    }
    return books;
  }

  public List<String> getAuthorNames() {
    Set<String> authorNames = new HashSet<>();

    Sheet sheet = workbook.getSheetAt(0);
    for (int i = 1; i < rowsCount; ++i) {
      String authors = sheet.getRow(i).getCell(authorsColumn).getStringCellValue();
      for (String name : authors.split(",\\s")) {
        name = name.replace('\u00A0', ' ');
        authorNames.add(name.trim());
      }
    }
    return new LinkedList<>(authorNames);
  }

  public List<BookWithAuthors> getBooksWithAuthors() {
    List<BookWithAuthors> booksWithAuthors = new ArrayList<>(rowsCount - 1);

    Sheet sheet = workbook.getSheetAt(0);
    for (int i = 1; i < rowsCount; ++i) {
      Row row = sheet.getRow(i);
      BigDecimal isbn = new BigDecimal(
          row.getCell(isbnColumn).getStringCellValue().substring(8, 21)
      );
      List<String> authorNames = new LinkedList<>();
      String authors = row.getCell(authorsColumn).getStringCellValue();
      for (String name : authors.split(",\\s")) {
        name = name.replace('\u00A0', ' ');
        authorNames.add(name.trim());
      }
      BookWithAuthors bookWithAuthors = new BookWithAuthors();
      bookWithAuthors.setBookIsbn(isbn);
      bookWithAuthors.setAuthorNames(authorNames);
      booksWithAuthors.add(bookWithAuthors);
    }
    return booksWithAuthors;
  }

  public class BookWithAuthors {
    private BigDecimal bookIsbn;
    private List<String> authorNames;

    public BigDecimal getBookIsbn() {
      return bookIsbn;
    }

    public void setBookIsbn(BigDecimal bookIsbn) {
      this.bookIsbn = bookIsbn;
    }

    public List<String> getAuthorNames() {
      return authorNames;
    }

    public void setAuthorNames(List<String> authorNames) {
      this.authorNames = authorNames;
    }
  }
}
