package org.redhat.csv2gdst.headers;

import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class StringHeader extends ComparableHeader {

  public StringHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    super(nodeName, varName, columnNumber, dataColumnType);
  }

  @Override
  public String readRecordValue(Node valueNode) {
    String recordValue =  Optional.of(valueNode)
      .map(n -> n.selectSingleNode("valueString"))
      .map(Node::getText)
      .map(StringEscapeUtils::unescapeXml)
      .orElse("");
    if(!isBlank(recordValue) && !recordValue.startsWith("\"&quot;\"")) {
      recordValue = "&quot;" + recordValue + "&quot;";
    }
    return recordValue;
  }

  @Override
  public String toString() {
    return "StringHeader{} " + super.toString();
  }
}
