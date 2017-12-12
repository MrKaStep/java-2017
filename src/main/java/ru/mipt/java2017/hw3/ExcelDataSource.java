package ru.mipt.java2017.hw3;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelDataSource {

  private static final Logger logger = LoggerFactory.getLogger("excel");
  private final Workbook workbook;

  private int titleColumn;
  private int authorsColumn;
  private int isbnColumn;
  private int rowsCount;

  private ExcelDataSource(InputStream inputStream) throws IOException {
    workbook = new XSSFWorkbook(inputStream);
    Sheet sheet = workbook.getSheetAt(0);
    Row row = sheet.getRow(0);
    for (int i = 0; i < 3; ++i) {
      String columnHead = row.getCell(i).getStringCellValue();
      if (columnHead.equals("Title")) {
        titleColumn = i;
      } else if (columnHead.equals("Authors")) {
        authorsColumn = i;
      } else {
        isbnColumn = i;
      }
    }

    int maxRowCount = sheet.getPhysicalNumberOfRows();
    rowsCount = 0;
    for (int i = 0; i < maxRowCount; ++i) {
      String contents = sheet.getRow(i).getCell(titleColumn).getStringCellValue();
      if (contents != null && contents.length() > 0) {
        ++rowsCount;
      } else {
        break;
      }
    }
  }

  public static ExcelDataSource createExcelDataSource(InputStream inputStream) throws IOException {
    return new ExcelDataSource(inputStream);
  }

  public List<BookWithAuthors> getEntries() {
    Sheet sheet = workbook.getSheetAt(0);

    List<BookWithAuthors> entries = new ArrayList<>(rowsCount - 1);
    for (int i = 1; i < rowsCount; ++i) {
      Row row = sheet.getRow(i);
      BookWithAuthors book = new BookWithAuthors();
      book.setBookIsbn(new BigDecimal(
          row.getCell(isbnColumn).getStringCellValue().substring(8, 21)
      ));
      book.setBookTitle(
          row.getCell(titleColumn).getStringCellValue().replace('\u00A0', ' ').trim()
      );
      String[] authors = row.getCell(authorsColumn).getStringCellValue()
          .replace('\u00A0', ' ').split(",\\s*");
      List<String> authorNames = new ArrayList<>(authors.length);
      for (String authorName : authors) {
        authorNames.add(authorName.trim());
      }
      book.setAuthorNames(authorNames);
      entries.add(book);
    }
    return entries;
  }

  public class BookWithAuthors {

    private BigDecimal bookIsbn;
    private String bookTitle;
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

    public String getBookTitle() {
      return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
      this.bookTitle = bookTitle;
    }
  }
}
