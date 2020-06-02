package org.redhat.csv2gdst;

import org.apache.commons.csv.CSVParser;
import org.dom4j.*;
import org.redhat.csv2gdst.headers.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class GdstDocument {
  Document document;
  Element documentRoot;
  List<HeaderDefinition> dataHeaders;
  AutoIncrementIntHeader rowNumHeader = null;

  public GdstDocument(Document gdstFile) {
    document = gdstFile;
    documentRoot = document.getRootElement();
    dataHeaders = readDataHeaders();

  }

  public List<HeaderDefinition> getDataHeaders() {
    return dataHeaders;
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
    // handle cases where an object matched, but no variables are used
    if(isBlank(varName)) {
      varName = node.selectSingleNode("header").getStringValue();

    }
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

  List<HeaderDefinition> readDataHeaders() {
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
    List<HeaderDefinition> dataHeaders = columnNodes.stream()
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

    rowNumHeader = dataHeaders.stream()
      .filter(h -> DataColumnType.RowNum.equals(h.getDataColumnType()) && h instanceof AutoIncrementIntHeader)
      .map(AutoIncrementIntHeader.class::cast)
      .findAny()
      .orElseThrow(() -> new RuntimeException("Unable to find RowNum column"));

    return dataHeaders;
  }

  void updateHeadersWithExistingData() {
    XPath existingDataListsSelector = DocumentHelper.createXPath("data/list");
    List<Node> existingDataLists = existingDataListsSelector.selectNodes(documentRoot);

    XPath rowNumSelector = DocumentHelper.createXPath("value["+rowNumHeader.getColumnNumber() + "]" );

    Map<UniqueHeader, XPath> uniqueHeaders = dataHeaders.stream()
      .filter(UniqueHeader.class::isInstance)
      .map(UniqueHeader.class::cast)
      .collect(
        Collectors.toMap(
          Function.identity(),
          h -> DocumentHelper.createXPath("value[" + h.getColumnNumber() + "]")
        )
      );

    for(Node list : existingDataLists) {

      // Handle maxRow search
      Node rowNumNode = rowNumSelector.selectSingleNode(list);
      String dataType = rowNumNode.selectSingleNode("dataType").getText();
      if (!"numeric_integer".equalsIgnoreCase(dataType)) {
        throw new RuntimeException("I am not on the rowNum column. Expected 'numeric_integer' datatype. Found: " + dataType);
      }
      int maxRowNum = Math.max(rowNumHeader.getCurrentRowNum(), Integer.parseInt(rowNumNode.selectSingleNode("valueNumeric").getText()));
      rowNumHeader.setCurrentRowNum(maxRowNum);

      uniqueHeaders.forEach( (h, path) -> {
        Node uniqueNode = path.selectSingleNode(list);
        String valueString = uniqueNode.selectSingleNode("valueString").getText();
        h.addExistingValue(valueString);
      });

    }
  }

  List<Node> retrieveDataRows() {
    XPath rowSelector = DocumentHelper.createXPath("data/*");
    List<Node> rows = defaultIfNull(rowSelector.selectNodes(documentRoot), Collections.emptyList());
    return rows;
  }


  void clearDataRows() {
    retrieveDataRows()
      .forEach(Node::detach);
  }

  void extendData(CSVParser csvParser) {
    extendData(dataHeaders, csvParser);
  }

  void extendData(List<HeaderDefinition> dataHeaders, CSVParser csvParser) {
    XPath dataSelector = DocumentHelper.createXPath("data");
    Element data = (Element) dataSelector.selectSingleNode(documentRoot);
    csvParser.forEach(csvRecord -> {
      Element list = data.addElement("list");
      Map<String, String> csvMap = csvRecord.toMap();
      dataHeaders.forEach(header -> header.extendRowWithRecord(list, csvRecord.getRecordNumber(), csvMap));
    });
  }
}
