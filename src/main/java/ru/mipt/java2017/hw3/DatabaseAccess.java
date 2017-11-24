package ru.mipt.java2017.hw3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseAccess {

  private static final Logger logger = LoggerFactory.getLogger("DBAccess");

  private final EntityManagerFactory entityManagerFactory;
  final EntityManager entityManager;

  void close() {
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

    Properties properties = new Properties();

    properties.setProperty("hibernate.connection.driver_class", driverClassName);
    properties.setProperty("hibernate.connection.url", url);

    entityManagerFactory =
        Persistence.createEntityManagerFactory("mydb", properties);
    entityManager = entityManagerFactory.createEntityManager();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("*** shutting down DatabaseAccess since JVM is shutting down");
      this.close();
      System.err.println("*** DatabaseAccess shut down");
    }));
  }
}
