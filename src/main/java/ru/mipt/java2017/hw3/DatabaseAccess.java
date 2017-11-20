package ru.mipt.java2017.hw3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mipt.java2017.hw3.models.Author;
import ru.mipt.java2017.hw3.models.Book;
import ru.mipt.java2017.hw3.models.BookAuthorRelation;

public class DatabaseAccess {

  private static final Logger logger = LoggerFactory.getLogger("DBAccess");

  private final SessionFactory sessionFactory;
  final Session session;

  private final EntityManagerFactory entityManagerFactory;
  final EntityManager entityManager;

  void close() {
    sessionFactory.close();
    entityManagerFactory.close();
  }

  public DatabaseAccess(String url) {
    String driverClassName = null;
    try {
      driverClassName = DriverManager.getDriver(url).getClass().getCanonicalName();
    } catch (SQLException e) {
      logger.error("Cannot get driver for specified URL");
      e.printStackTrace();
      System.exit(1);
    }

    String dialectName = null;
    try {
      Connection connection = DriverManager.getConnection(url);
      dialectName = new StandardDialectResolver().resolveDialect(
          new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData())
      ).toString();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    Properties properties = new Properties();

    properties.setProperty("hibernate.connection.driver_class", driverClassName);
    properties.setProperty("hibernate.connection.dialect", dialectName);
    properties.setProperty("hibernate.connection.url", url);

    Configuration configuration = new Configuration();

    configuration.addProperties(properties);
    configuration.addAnnotatedClass(Book.class);
    configuration.addAnnotatedClass(Author.class);
    configuration.addAnnotatedClass(BookAuthorRelation.class);
    configuration.configure();

    sessionFactory = configuration.buildSessionFactory();
    entityManagerFactory =
        Persistence.createEntityManagerFactory("mydb", properties);
    session = sessionFactory.openSession();
    entityManager = entityManagerFactory.createEntityManager();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("*** shutting down DatabaseAccess since JVM is shutting down");
      this.close();
      System.err.println("*** DatabaseAccess shut down");
    }));
  }
}
