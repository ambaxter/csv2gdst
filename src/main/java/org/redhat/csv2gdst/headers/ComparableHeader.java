package org.redhat.csv2gdst.headers;

import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Map;
import java.util.Optional;

public class ComparableHeader extends DataColumnHeader {

  public ComparableHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    super(nodeName, varName, columnNumber, dataColumnType);
  }

  @Override
  public void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord) {
    String recordValue = retrieveRecordValue(csvRecord);
    recordValue = StringEscapeUtils.escapeXml11(recordValue);
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
    return "ComparableHeader{} " + super.toString();
  }
}
