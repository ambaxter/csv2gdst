package org.redhat.csv2gdst.headers;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public abstract class DataColumnHeader implements HeaderDefinition {
  final protected String nodeName;
  final protected String varName;
  final protected int columnNumber;
  final protected DataColumnType dataColumnType;

  public DataColumnHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    this.nodeName = requireNonNull(nodeName);
    this.varName = requireNonNull(varName);
    this.columnNumber = columnNumber;
    this.dataColumnType = requireNonNull(dataColumnType);
  }

  String retrieveRecordValue(Map<String, String> csvRecord) {
    return defaultIfNull(csvRecord.get(varName), "");
  }

  @Override
  public String getNodeName() {
    return nodeName;
  }

  @Override
  public String getVarName() {
    return varName;
  }

  @Override
  public int getColumnNumber() {
    return columnNumber;
  }

  @Override
  public DataColumnType getDataColumnType() {
    return dataColumnType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataColumnHeader that = (DataColumnHeader) o;
    return columnNumber == that.columnNumber &&
      Objects.equals(nodeName, that.nodeName) &&
      Objects.equals(varName, that.varName) &&
      dataColumnType == that.dataColumnType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeName, varName, columnNumber, dataColumnType);
  }

  @Override
  public String toString() {
    return "DataColumnHeader{" +
      "nodeName='" + nodeName + '\'' +
      ", varName='" + varName + '\'' +
      ", columnNumber=" + columnNumber +
      ", dataColumnType=" + dataColumnType +
      '}';
  }
}
