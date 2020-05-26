package org.redhat.csv2gdst.headers;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class BooleanHeader extends DataColumnHeader {

  public BooleanHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    super(nodeName, varName, columnNumber, dataColumnType);
  }

  @Override
  public void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord) {
    String recordValue = retrieveRecordValue(csvRecord);
    Element valueNode = row.addElement("value");
    if (!isBlank(recordValue)) {
      if (!("true".equals(recordValue) || "false".equals(recordValue))) {
        System.err.println("Unexpected boolean value on record:column " +
          recordNum + ":" + columnNumber +
          " - " + recordValue);
      }
      valueNode.addElement("valueBoolean").setText(recordValue);
    }
    valueNode.addElement("valueString").setText("");
    valueNode.addElement("dataType").setText("BOOLEAN");
    valueNode.addElement("isOtherwise").setText("false");
  }

  @Override
  public String readRecordValue(Node valueNode) {
    return Optional.of(valueNode)
      .map(n -> n.selectSingleNode("valueBoolean"))
      .map(Node::getText)
      .orElse("");
  }

  @Override
  public String toString() {
    return "BooleanHeader{} " + super.toString();
  }
}
