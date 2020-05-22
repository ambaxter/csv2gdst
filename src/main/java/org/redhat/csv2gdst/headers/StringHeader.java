package org.redhat.csv2gdst.headers;

import org.dom4j.Element;

import java.util.Map;

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
  public String toString() {
    return "StringHeader{} " + super.toString();
  }
}
