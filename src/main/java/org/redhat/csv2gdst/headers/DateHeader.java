package org.redhat.csv2gdst.headers;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class DateHeader extends DataColumnHeader {

  public DateHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    super(nodeName, varName, columnNumber, dataColumnType);
  }

  @Override
  public void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord) {
    String recordValue = retrieveRecordValue(csvRecord);
    Element valueNode = row.addElement("value");
    if (!isBlank(recordValue)) {
      valueNode.addElement("valueDate").setText(recordValue);
    }
    valueNode.addElement("valueString").setText("");
    valueNode.addElement("dataType").setText("DATE");
    valueNode.addElement("isOtherwise").setText("false");
  }

  @Override
  public String readRecordValue(Node valueNode) {
    return Optional.of(valueNode)
      .map(n -> n.selectSingleNode("valueDate"))
      .map(Node::getText)
      .orElse("");
  }

  @Override
  public String toString() {
    return "DateHeader{} " + super.toString();
  }
}
