package org.redhat.csv2gdst.headers;

import org.apache.commons.csv.CSVRecord;
import org.dom4j.Element;

import java.util.Objects;

public class RowNumHeader extends DataColumnHeader {
  private int currentRowNum = 0;

  public RowNumHeader(String nodeName, int columnNumber) {
    super(nodeName, columnNumber);
  }

  @Override
  public void extendRowWithRecord(Element row, CSVRecord csvRecord) {
    currentRowNum += 1;
    Element valueNode = row.addElement("value");
    valueNode.addElement("valueNumeric").addAttribute("class", "int").setText(Integer.toString(currentRowNum));
    valueNode.addElement("valueString").setText("");
    valueNode.addElement("dataType").setText("NUMERIC_INTEGER");
    valueNode.addElement("isOtherwise").setText("false");
  }

  public int getCurrentRowNum() {
    return currentRowNum;
  }

  public void setCurrentRowNum(int currentRowNum) {
    this.currentRowNum = currentRowNum;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    RowNumHeader that = (RowNumHeader) o;
    return currentRowNum == that.currentRowNum;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), currentRowNum);
  }

  @Override
  public String toString() {
    return "RowNumHeader{" +
      "currentRowNum=" + currentRowNum +
      '}';
  }
}
