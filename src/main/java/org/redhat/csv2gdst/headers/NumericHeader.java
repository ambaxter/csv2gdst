package org.redhat.csv2gdst.headers;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NumericHeader extends DataColumnHeader {

  final protected NumericType numericType;

  public NumericHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType, NumericType numericType) {
    super(nodeName, varName, columnNumber, dataColumnType);
    this.numericType = requireNonNull(numericType);
  }

  @Override
  public void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord) {
    String recordValue = retrieveRecordValue(csvRecord);
    Element valueNode = row.addElement("value");
    if (!isBlank(recordValue)) {
      Element valueNumeric = valueNode.addElement("valueNumeric");
      if (!isBlank(numericType.getDataClass())) {
        valueNumeric.addAttribute("class", numericType.getDataClass());
      }
      valueNumeric.setText(recordValue);
    }
    valueNode.addElement("valueString").setText("");
    valueNode.addElement("dataType").setText(numericType.getDataType());
    valueNode.addElement("isOtherwise").setText("false");
  }

  @Override
  public String readRecordValue(Node valueNode) {
    return Optional.of(valueNode)
      .map(n -> n.selectSingleNode("valueNumeric"))
      .map(Node::getText)
      .orElse("");
  }

  public NumericType getNumericType() {
    return numericType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    NumericHeader that = (NumericHeader) o;
    return numericType == that.numericType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), numericType);
  }

  @Override
  public String toString() {
    return "NumericHeader{" +
      "numericType=" + numericType +
      "} " + super.toString();
  }
}
