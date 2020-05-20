package org.redhat.csv2gdst.headers;

import java.util.Objects;

public abstract class DataColumnHeader implements ExtendRowWithRecord {
  private String nodeName;
  private int columnNumber;

  public DataColumnHeader(String nodeName, int columnNumber) {
    this.nodeName = nodeName;
    this.columnNumber = columnNumber;
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public int getColumnNumber() {
    return columnNumber;
  }

  public void setColumnNumber(int columnNumber) {
    this.columnNumber = columnNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataColumnHeader that = (DataColumnHeader) o;
    return columnNumber == that.columnNumber &&
      Objects.equals(nodeName, that.nodeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeName, columnNumber);
  }

  @Override
  public String toString() {
    return "DataColumnHeader{" +
      "nodeName='" + nodeName + '\'' +
      ", columnNumber=" + columnNumber +
      '}';
  }
}
