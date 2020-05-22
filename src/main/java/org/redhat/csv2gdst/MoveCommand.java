package org.redhat.csv2gdst;

import org.apache.commons.csv.CSVParser;
import org.dom4j.*;
import org.redhat.csv2gdst.headers.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MoveCommand {

  public enum MoveDataType {
    replace, extend
  }

  Document document;
  Element documentRoot;
  List<ExtendRowWithRecord> dataHeaders;
  AutoIncrementIntHeader autoIncrementIntHeader = null;

  public MoveCommand(Document gdstFile) {
    document = gdstFile;
    documentRoot = document.getRootElement();
    dataHeaders = readDataHeaders();
  }

  public void moveDataWith(MoveDataType moveDataType, CSVParser csvParser) {
    switch (moveDataType) {
      case replace:
        clearDataRows();
        break;
      case extend:
        int maxRow = findMaxRows();
        autoIncrementIntHeader.setCurrentRowNum(maxRow);
        break;
    }
    extendData(dataHeaders, csvParser);
  }

  StringHeader handleMetaDataColumn(Node node, int columnNumber) {
    String varName = node.selectSingleNode("metadata").getStringValue();
    return new StringHeader(node.getName(), varName, columnNumber, DataColumnType.Metadata);
  }

  DataColumnHeader handleTypedDefaultColumn(Node node, int columnNumber, String varNameField, DataColumnType dataColumnType) {
    String fieldType = node.selectSingleNode("typedDefaultValue/dataType").getStringValue();
    String varName = node.selectSingleNode(varNameField).getStringValue();
    return createDataColumnHeader(node.getName(), columnNumber, dataColumnType, fieldType, varName);
  }

  DataColumnHeader handleVariableColumn(Node node, int columnNumber, DataColumnType dataColumnType) {
    String fieldType = node.selectSingleNode("fieldType").getStringValue();
    String varName = node.selectSingleNode("varName").getStringValue();
    return createDataColumnHeader(node.getName(), columnNumber, dataColumnType, fieldType, varName);
  }

  private DataColumnHeader createDataColumnHeader(String nodeName, int columnNumber, DataColumnType dataColumnType, String fieldType, String varName) {
    switch (fieldType.toLowerCase()) {
      case "boolean":
        return new BooleanHeader(nodeName, varName, columnNumber, dataColumnType);
      case "integer":
      case "numeric_integer":
        return new NumericHeader(nodeName, varName, columnNumber, dataColumnType, NumericType.Integer);
      case "bigdecimal":
        return new NumericHeader(nodeName, varName, columnNumber, dataColumnType, NumericType.BigDecimal);
      case "long":
      case "numeric_long":
        return new NumericHeader(nodeName, varName, columnNumber, dataColumnType, NumericType.Long);
      case "date":
        return new DateHeader(nodeName, varName, columnNumber, dataColumnType);
      case "string":
      case "comparable":
        return new StringHeader(nodeName, varName, columnNumber, dataColumnType);
      default:
        throw new RuntimeException("Unexpected type value: " + fieldType);
    }
  }

  List<ExtendRowWithRecord> readDataHeaders() {
    AtomicInteger columnCounter = new AtomicInteger(0);
    String columnNodesXpathString = "rowNumberCol" +
      "|descriptionCol" +
      "|metadataCols/*" +
      "|attributeCols/*" +
      "|conditionPatterns/org.drools.workbench.models.guided.dtable.shared.model.BRLConditionColumn/childColumns/org.drools.workbench.models.guided.dtable.shared.model.BRLConditionVariableColumn" +
      "|actionCols/org.drools.workbench.models.guided.dtable.shared.model.BRLActionColumn/childColumns/org.drools.workbench.models.guided.dtable.shared.model.BRLActionVariableColumn" +
      "|actionCols/set-field-col52";
    XPath columnNodesSelector = DocumentHelper.createXPath(columnNodesXpathString);
    List<Node> columnNodes = columnNodesSelector.selectNodes(documentRoot);
    List<ExtendRowWithRecord> dataHeaders = columnNodes.stream()
      .map(node -> {
        int columnNumber = columnCounter.incrementAndGet();
        switch (node.getName()) {
          case "rowNumberCol":
            return new AutoIncrementIntHeader(node.getName(), node.getName(), columnNumber, DataColumnType.RowNum);
          case "descriptionCol":
            return new UniqueHeader(new StringHeader(node.getName(), node.getName(), columnNumber, DataColumnType.Description));
          case "metadata-column52":
            return handleMetaDataColumn(node, columnNumber);
          case "attribute-column52":
            return handleTypedDefaultColumn(node, columnNumber, "attribute", DataColumnType.Attribute);
          case "org.drools.workbench.models.guided.dtable.shared.model.BRLConditionVariableColumn":
            return handleVariableColumn(node, columnNumber, DataColumnType.Condition);
          case "org.drools.workbench.models.guided.dtable.shared.model.BRLActionVariableColumn":
            return handleVariableColumn(node, columnNumber, DataColumnType.Action);
          case "set-field-col52":
            return handleTypedDefaultColumn(node, columnNumber, "factField", DataColumnType.Action);
          default:
            throw new RuntimeException("Encountered unsupported column type: " + node.getName());
        }
      })
      .collect(Collectors.toList());

    autoIncrementIntHeader = dataHeaders.stream()
      .filter(h -> DataColumnType.RowNum.equals(h.getDataColumnType()) && h instanceof AutoIncrementIntHeader)
      .map(AutoIncrementIntHeader.class::cast)
      .findAny()
      .orElseThrow(() -> new RuntimeException("Unable to find RowNum column"));

    return dataHeaders;
  }

  int findMaxRows() {
    int maxRowNum = 0;
    XPath rowNumSelector = DocumentHelper.createXPath("data/list/value[" + autoIncrementIntHeader.getColumnNumber() + "]");
    List<Node> rowNums = rowNumSelector.selectNodes(documentRoot);
    for (Node rowNumNode : rowNums) {
      String dataType = rowNumNode.selectSingleNode("dataType").getText();
      if (!"numeric_integer".equalsIgnoreCase(dataType)) {
        throw new RuntimeException("I am not on the rowNum column. Expected 'numeric_integer' datatype. Found: " + dataType);
      }
      maxRowNum = Math.max(Integer.parseInt(rowNumNode.selectSingleNode("valueNumeric").getText()), maxRowNum);
    }

    return maxRowNum;
  }

  void clearDataRows() {
    XPath rowNumSelector = DocumentHelper.createXPath("data/*");
    List<Node> rowNums = rowNumSelector.selectNodes(documentRoot);
    rowNums.forEach(Node::detach);
  }

  void extendData(List<ExtendRowWithRecord> dataHeaders, CSVParser csvParser) {
    XPath dataSelector = DocumentHelper.createXPath("data");
    Element data = (Element) dataSelector.selectSingleNode(documentRoot);
    csvParser.forEach(csvRecord -> {
      Element list = data.addElement("list");
      Map<String, String> csvMap = csvRecord.toMap();
      dataHeaders.forEach(header -> header.extendRowWithRecord(list, csvRecord.getRecordNumber(), csvMap));
    });
  }

}
