package ru.mipt.java2017.hw3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mipt.java2017.hw3.ExcelDataSource.BookWithAuthors;
import ru.mipt.java2017.hw3.models.Book;

public class AdvancedDatabaseUpdater extends DatabaseUpdater {

  private static final Logger logger = LoggerFactory.getLogger("AdvDBUpdater");

  private final GoogleImageSearcher imageSearcher;

  AdvancedDatabaseUpdater(DatabaseAccess databaseAccess, Properties properties) {
    super(databaseAccess);
    String apiKey = properties.getProperty("googleApiKey");
    String searchContext = properties.getProperty("googleSearchContext");
    this.imageSearcher = new GoogleImageSearcher(apiKey, searchContext);
  }

  @Override
  protected void updateDatabase(List<BookWithAuthors> booksWithAuthors) {
    Set<String> authorNames = new HashSet<>();
    List<Book> books = new ArrayList<>(booksWithAuthors.size());

    booksWithAuthors.forEach(bookWithAuthors -> {
      authorNames.addAll(bookWithAuthors.getAuthorNames());
      Book book = new Book();
      book.setIsbn(bookWithAuthors.getBookIsbn());
      book.setTitle(bookWithAuthors.getBookTitle());
      try {
        List<String> authors = bookWithAuthors.getAuthorNames();
        String coverLink = imageSearcher.searchImage(
            book.getTitle() + " by " + authors.get(0) + " cover"
        );
        book.setCoverLink(coverLink);
      } catch (IOException e) {
        logger.warn("Cannot get cover: {}", e.getMessage());
      }
      books.add(book);
    });

    Map<String, Long> authorIdByName = addAuthorsAndGetIds(new ArrayList<>(authorNames));
    Map<BigDecimal, Long> bookIdByIsbn = addBooksAndGetIds(books);
    addBookAuthorRelations(authorIdByName, bookIdByIsbn, booksWithAuthors);
  }

  public static void main(String[] args) {
    DatabaseAccess databaseAccess = new DatabaseAccess(args[0]);

    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(args[3]));
    } catch (FileNotFoundException e) {
      logger.error("Properties file {} not found", args[3]);
      System.err.println("Please create properties file!");
    } catch (IOException e) {
      e.printStackTrace();
    }

    AdvancedDatabaseUpdater updater = new AdvancedDatabaseUpdater(databaseAccess, properties);
    ExcelDataSource excelDataSource = null;
    String login = properties.getProperty("yandexUser");
    String password = properties.getProperty("yandexPassword");
    YandexDiskFileRetriever retriever = new YandexDiskFileRetriever(login, password);
    try {
      InputStream inputStream = retriever.getFile(args[1]);
      excelDataSource = ExcelDataSource.createExcelDataSource(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    updater.updateDatabase(excelDataSource.getEntries());
    updater.printDatabase(args[2]);
    databaseAccess.close();
  }


}
