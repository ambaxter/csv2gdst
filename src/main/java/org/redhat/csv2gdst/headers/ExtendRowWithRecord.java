package org.redhat.csv2gdst.headers;

import org.apache.commons.csv.CSVRecord;
import org.dom4j.Element;

public interface ExtendRowWithRecord {
  void extendRowWithRecord(Element row, CSVRecord csvRecord);
}
