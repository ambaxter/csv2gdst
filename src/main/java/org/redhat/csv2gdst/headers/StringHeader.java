package org.redhat.csv2gdst.headers;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.*;

public class StringHeader extends ComparableHeader {

  public StringHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    super(nodeName, varName, columnNumber, dataColumnType);
  }

  @Override
  String retrieveRecordValue(Map<String, String> csvRecord) {
    String recordValue = super.retrieveRecordValue(csvRecord);
    if(!isBlank(recordValue) && !DataColumnType.Description.equals(dataColumnType)) {
      recordValue = wrapIfMissing(recordValue, "\"");
    }
    return recordValue;
  }

  @Override
  public String toString() {
    return "StringHeader{} " + super.toString();
  }
}
