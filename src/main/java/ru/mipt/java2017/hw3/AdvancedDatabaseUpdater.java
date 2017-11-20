package ru.mipt.java2017.hw3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mipt.java2017.hw3.models.Book;

public class AdvancedDatabaseUpdater {

  private static final Logger logger = LoggerFactory.getLogger("AdvDBUpdater");

  private final DatabaseUpdater databaseUpdater;
  private final YandexDiskFileRetriever retriever;
  private final EntityManager entityManager;
  private final CriteriaBuilder criteriaBuilder;
  private final GoogleImageSearcher imageSearcher;

  AdvancedDatabaseUpdater(DatabaseAccess databaseAccess, String propertiesPath) {
    this.databaseUpdater = new DatabaseUpdater(databaseAccess);
    this.entityManager = databaseAccess.entityManager;
    this.criteriaBuilder = entityManager.getCriteriaBuilder();

    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(propertiesPath));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    String login = properties.getProperty("yandexUser");
    String password = properties.getProperty("yandexPassword");
    this.retriever = new YandexDiskFileRetriever(login, password);

    String apiKey = properties.getProperty("googleApiKey");
    String searchContext = properties.getProperty("googleSearchContext");
    this.imageSearcher = new GoogleImageSearcher(apiKey, searchContext);
  }

  private void updateDatabase(String yandexDiskPath) {
    InputStream inputStream;
    ExcelDataSource excelDataSource = null;
    try {
      inputStream = retriever.getFile(yandexDiskPath);
      excelDataSource = ExcelDataSource.createExcelDataSource(inputStream);
    } catch (IOException e) {
      logger.error("Cannot read file from Yandex Disk: {}", e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
    databaseUpdater.updateDatabase(excelDataSource);
  }

  private void addCoverLinks() {
    logger.info("Adding cover links ...");
    CriteriaQuery<Book> query = criteriaBuilder.createQuery(Book.class);
    Root<Book> root = query.from(Book.class);
    query.select(root);
    List<Book> books = entityManager.createQuery(query).getResultList();
    entityManager.getTransaction().begin();
    books.forEach(book -> {
      try {
        String coverLink = imageSearcher.searchImage(book.getTitle() + " cover");
        book.setCoverLink(coverLink);
        entityManager.merge(book);
      } catch (IOException e) {
        logger.warn("Cannot get cover: {}", e.getMessage());
      }
    });
    entityManager.getTransaction().commit();
    logger.info("Done!");
  }

  public static void main(String[] args) {
    DatabaseAccess databaseAccess = new DatabaseAccess(args[0]);
    AdvancedDatabaseUpdater updater = new AdvancedDatabaseUpdater(databaseAccess, args[3]);

    updater.updateDatabase(args[1]);
    updater.addCoverLinks();

    DatabasePrinter databasePrinter = new DatabasePrinter(databaseAccess, args[2]);
    try {
      databasePrinter.printDatabaseContents();
    } catch (IOException e) {
      logger.error("Cannot print DB to excel file: {}", e.getMessage());
      e.printStackTrace();
    } finally {
      databaseAccess.close();
    }
  }


}
