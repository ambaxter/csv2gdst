package org.redhat.csv2gdst.headers;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class UniqueHeader implements HeaderDefinition {

  final protected DataColumnHeader delegate;
  final protected HashSet<String> uniqueValues = new HashSet<>();

  public UniqueHeader(DataColumnHeader delegate) {
    this.delegate = delegate;
  }

  @Override
  public void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord) {
    String recordValue = delegate.retrieveRecordValue(csvRecord);
    if (uniqueValues.contains(recordValue)) {
      System.err.println("Non-unique record found at row:column " +
        recordNum + ":" + delegate.getColumnNumber() +
        " - " + recordValue + ". Business Central may report errors");
    } else {
      uniqueValues.add(recordValue);
    }
    delegate.extendRowWithRecord(row, recordNum, csvRecord);
  }

  @Override
  public String readRecordValue(Node valueNode) {
    return delegate.readRecordValue(valueNode);
  }

  public DataColumnHeader getDelegate() {
    return delegate;
  }

  public void addExistingValue(String value) {
    uniqueValues.add(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UniqueHeader that = (UniqueHeader) o;
    return Objects.equals(delegate, that.delegate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delegate);
  }

  @Override
  public String toString() {
    return "UniqueHeader{" +
      "delegate=" + delegate +
      '}';
  }

  @Override
  public String getNodeName() {
    return delegate.getNodeName();
  }

  @Override
  public String getVarName() {
    return delegate.getVarName();
  }

  @Override
  public int getColumnNumber() {
    return delegate.getColumnNumber();
  }

  @Override
  public DataColumnType getDataColumnType() {
    return delegate.getDataColumnType();
  }
}
