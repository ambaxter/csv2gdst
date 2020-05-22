package org.redhat.csv2gdst.headers;

import java.util.Map;
import java.util.Objects;

public class AutoIncrementIntHeader extends NumericHeader {
  private int currentRowNum = 0;

  public AutoIncrementIntHeader(String nodeName, String varName, int columnNumber, DataColumnType dataColumnType) {
    super(nodeName, varName, columnNumber, dataColumnType, NumericType.Integer);
  }

  @Override
  protected String retrieveRecordValue(Map<String, String> csvRecord) {
    currentRowNum += 1;
    return Integer.toString(currentRowNum);
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
    AutoIncrementIntHeader that = (AutoIncrementIntHeader) o;
    return currentRowNum == that.currentRowNum;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), currentRowNum);
  }

  @Override
  public String toString() {
    return "AutoIncrementHeader{" +
      "currentRowNum=" + currentRowNum +
      "} " + super.toString();
  }
}
