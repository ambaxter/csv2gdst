package org.redhat.csv2gdst.headers;

public enum NumericType {
  Integer("NUMERIC_INTEGER", "int"),
  Long("NUMERIC_LONG", "long"),
  BigDecimal("NUMERIC_BIGDECIMAL", null);

  NumericType(String dataType, String dataClass) {
    this.dataType = dataType;
    this.dataClass = dataClass;
  }

  private final String dataType;
  private final String dataClass;

  public String getDataType() {
    return dataType;
  }

  public String getDataClass() {
    return dataClass;
  }
}
