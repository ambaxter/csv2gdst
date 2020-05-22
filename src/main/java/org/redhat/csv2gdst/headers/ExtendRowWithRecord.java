package org.redhat.csv2gdst.headers;

import org.dom4j.Element;

import java.util.Map;

public interface ExtendRowWithRecord {
  void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord);

  String getNodeName();

  String getVarName();

  int getColumnNumber();

  DataColumnType getDataColumnType();

}
