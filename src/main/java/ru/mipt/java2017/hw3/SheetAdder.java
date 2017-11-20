package ru.mipt.java2017.hw3;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

public class SheetAdder<T> {

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


  Row addHeaderRow(Sheet sheet) {
    Row headerRow = sheet.createRow(0);

    CellStyle cellStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    cellStyle.setFont(font);
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    headerRow.setRowStyle(cellStyle);

    return headerRow;
  }

  public void add() {
    Sheet sheet = workbook.createSheet(tableClass.getAnnotation(Table.class).name());

    CriteriaQuery<T> criteriaQuery = builder.createQuery(tableClass);
    Root<T> root = criteriaQuery.from(tableClass);
    criteriaQuery.select(root);
    List<T> entries = entityManager.createQuery(criteriaQuery).getResultList();

    Row headerRow = addHeaderRow(sheet);

    try {
      Map<Field, Method> getters = new HashMap<>();
      PropertyDescriptor[] propertyDescriptors =
          Introspector.getBeanInfo(tableClass, Object.class).getPropertyDescriptors();
      for (PropertyDescriptor pd : propertyDescriptors) {
        Field field = tableClass.getDeclaredField(pd.getName());
        Method getter = pd.getReadMethod();
        getters.put(field, getter);
      }

      Field[] fields = tableClass.getDeclaredFields();
      for (int i = 0; i < fields.length; ++i) {
        headerRow.createCell(i).setCellValue(fields[i].getAnnotation(Column.class).name());
      }
      int lastRow = 0;
      for (T entry : entries) {
        Row row = sheet.createRow(++lastRow);
        for (int i = 0; i < fields.length; ++i) {
          Cell cell = row.createCell(i);
          Method getter = getters.get(fields[i]);
          Object o = getter.invoke(entry);
          if (o != null) {
            try {
              cell.setCellValue(Long.class.cast(o));
            } catch (ClassCastException e) {
              cell.setCellValue(o.toString());
            }
          } else {
            cell.setCellValue("");
          }
//          row.createCell(i).setCellValue(getters.get(fields[i].getName()).invoke(entry).toString());
        }
      }
    } catch (IntrospectionException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
