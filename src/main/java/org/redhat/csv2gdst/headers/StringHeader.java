package org.redhat.csv2gdst.headers;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Map;
import java.util.Optional;

public class StringHeader extends DataColumnHeader {

  public StringHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    super(nodeName, varName, columnNumber, dataColumnType);
  }

  @Override
  public void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord) {
    String recordValue = retrieveRecordValue(csvRecord);
    Element valueNode = row.addElement("value");
    valueNode.addElement("valueString").setText(recordValue);
    valueNode.addElement("dataType").setText("STRING");
    valueNode.addElement("isOtherwise").setText("false");
  }

  @Override
  public String readRecordValue(Node valueNode) {
    return Optional.of(valueNode)
      .map(n -> n.selectSingleNode("valueString"))
      .map(Node::getText)
      .orElse("");
  }

  @Override
  public String toString() {
    return "StringHeader{} " + super.toString();
  }
}
