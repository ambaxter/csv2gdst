package org.redhat.csv2gdst.headers;

import org.apache.commons.csv.CSVRecord;
import org.dom4j.Element;

import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class StringHeader extends DataColumnHeader {
  private String varName;

  public StringHeader(String nodeName, int columnNumber, String varName) {
    super(nodeName, columnNumber);
    this.varName = varName;
  }

  @Override
  public void extendRowWithRecord(Element row, CSVRecord csvRecord) {
    String value = csvRecord.get(varName);
    Element valueNode = row.addElement("value");
    valueNode.addElement("valueString").setText(defaultIfNull(value, ""));
    valueNode.addElement("dataType").setText("STRING");
    valueNode.addElement("isOtherwise").setText("false");
  }

  public String getVarName() {
    return varName;
  }

  public void setVarName(String varName) {
    this.varName = varName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    StringHeader that = (StringHeader) o;
    return Objects.equals(varName, that.varName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), varName);
  }

  @Override
  public String toString() {
    return "StringHeader{" +
      "varName='" + varName + '\'' +
      '}';
  }
}
