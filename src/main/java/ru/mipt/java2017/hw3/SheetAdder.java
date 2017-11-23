package ru.mipt.java2017.hw3;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SheetAdder<T> {

  private static final Logger logger = LoggerFactory.getLogger("SheetAdder");

  private final Workbook workbook;
  private final EntityManager entityManager;
  private final CriteriaBuilder builder;

  private final Class<T> tableClass;

  public SheetAdder(Workbook workbook, EntityManager entityManager,
      CriteriaBuilder builder, Class<T> tableClass) {
    this.workbook = workbook;
    this.entityManager = entityManager;
    this.builder = builder;
    this.tableClass = tableClass;
  }

  private Row addHeaderRow(Sheet sheet) {
    Row headerRow = sheet.createRow(0);

    CellStyle cellStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    cellStyle.setFont(font);
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    headerRow.setRowStyle(cellStyle);

    return headerRow;
  }

  private Map<String, Method> getColumnNamesWithGetters() throws IntrospectionException {
    Map<String, Method> getters = new HashMap<>();
    PropertyDescriptor[] propertyDescriptors =
        Introspector.getBeanInfo(tableClass, Object.class).getPropertyDescriptors();
    for (PropertyDescriptor pd : propertyDescriptors) {
      try {
        Field field = tableClass.getDeclaredField(pd.getName());
        Method getter = pd.getReadMethod();
        Column column = field.getAnnotation(Column.class);
        if (column != null)
          getters.put(column.name(), getter);

      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      }
    }
    return getters;
  }

  public void add() {
    Map<String, Method> getters;
    try {
      getters = getColumnNamesWithGetters();
    } catch (IntrospectionException e) {
      logger.error("Unable to get fields of {}: {}", tableClass.toString(), e.getMessage());
      return;
    }

    Table table = tableClass.getAnnotation(Table.class);
    if (table == null) {
      logger.error("Provided class is not a JPA table");
      return;
    }

    logger.info("Adding {} sheet...", table.name());

    Sheet sheet = workbook.createSheet(table.name());

    CriteriaQuery<T> criteriaQuery = builder.createQuery(tableClass);
    Root<T> root = criteriaQuery.from(tableClass);
    criteriaQuery.select(root);
    List<T> entries = entityManager.createQuery(criteriaQuery).getResultList();

    Row headerRow = addHeaderRow(sheet);

    List<Map.Entry<String, Method>> getterList = new ArrayList<>(getters.entrySet());

    for (int i = 0; i < getterList.size(); ++i) {
      headerRow.createCell(i).setCellValue(getterList.get(i).getKey());
    }

    int lastRow = 0;
    for (T entry : entries) {
      Row row = sheet.createRow(++lastRow);
      for (int i = 0; i < getterList.size(); ++i) {
        Cell cell = row.createCell(i);
        Method getter = getterList.get(i).getValue();
        Object o = null;
        try {
          o = getter.invoke(entry);
        } catch (IllegalAccessException e) {
          logger.warn("Can't access getter of field {}, skipping", getterList.get(i).getKey());
        } catch (InvocationTargetException e) {
          logger.warn("Getter invocation caused an exception: {}", e.getMessage());
        }
        if (o != null) {
          try {
            cell.setCellValue(Long.class.cast(o));
          } catch (ClassCastException e) {
            cell.setCellValue(o.toString());
          }
        } else {
          cell.setCellValue("");
        }
      }
    }

    logger.info("Sheet {} successfully added!", table.name());
  }
}
