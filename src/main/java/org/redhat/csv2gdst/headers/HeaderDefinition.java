package org.redhat.csv2gdst.headers;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Map;

public interface HeaderDefinition {

  void extendRowWithRecord(Element row, long recordNum, Map<String, String> csvRecord);

  String readRecordValue(Node valueNode);

  String getNodeName();

  String getVarName();

  int getColumnNumber();

  DataColumnType getDataColumnType();

}
