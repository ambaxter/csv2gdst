package org.redhat.csv2gdst;

import org.apache.commons.csv.CSVParser;
import org.dom4j.*;
import org.redhat.csv2gdst.headers.DataColumnHeader;
import org.redhat.csv2gdst.headers.DescriptionHeader;
import org.redhat.csv2gdst.headers.RowNumHeader;
import org.redhat.csv2gdst.headers.StringHeader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MoveCommand {

  public enum MoveDataType {
    replace, extend
  }

  Document document;
  Element documentRoot;
  List<DataColumnHeader> dataHeaders;
  RowNumHeader rowNumHeader = null;

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
        rowNumHeader.setCurrentRowNum(maxRow);
        break;
    }
    extendData(dataHeaders, csvParser);
  }


  DataColumnHeader handleColumnNode(Node node, int columnNumber) {
    String fieldType = node.selectSingleNode("fieldType").getStringValue();
    String varName = node.selectSingleNode("varName").getStringValue();

    switch (fieldType.toLowerCase()) {
      case "string":
        return new StringHeader(node.getName(), columnNumber, varName);
      default:
        throw new RuntimeException("Unexpected type value: " + fieldType);
    }

  }

  List<DataColumnHeader> readDataHeaders() {
    AtomicInteger columnCounter = new AtomicInteger(0);
    String columnNodesXpathString = "rowNumberCol" +
      "|descriptionCol" +
      "|metadataCols/*" +
      "|attributeCols/*" +
      "|conditionPatterns/org.drools.workbench.models.guided.dtable.shared.model.BRLConditionColumn/childColumns/org.drools.workbench.models.guided.dtable.shared.model.BRLConditionVariableColumn" +
      "|actionCols/org.drools.workbench.models.guided.dtable.shared.model.BRLActionColumn/childColumns/org.drools.workbench.models.guided.dtable.shared.model.BRLActionVariableColumn";
    XPath columnNodesSelector = DocumentHelper.createXPath(columnNodesXpathString);
    List<Node> columnNodes = columnNodesSelector.selectNodes(documentRoot);
    List<DataColumnHeader> dataHeaders = columnNodes.stream()
      .map(node -> {
        int columnNumber = columnCounter.incrementAndGet();
        switch (node.getName()) {
          case "rowNumberCol":
            return new RowNumHeader(node.getName(), columnNumber);
          case "descriptionCol":
            return new DescriptionHeader(node.getName(), columnNumber);
          case "org.drools.workbench.models.guided.dtable.shared.model.BRLConditionVariableColumn":
          case "org.drools.workbench.models.guided.dtable.shared.model.BRLActionVariableColumn":
            return handleColumnNode(node, columnNumber);
          default:
            throw new RuntimeException("Encountered unsupported column type: " + node.getName());
        }
      })
      .collect(Collectors.toList());

    rowNumHeader = dataHeaders.stream()
      .filter(RowNumHeader.class::isInstance)
      .map(RowNumHeader.class::cast)
      .findAny()
      .orElseThrow(() -> new RuntimeException("Unable to find RowNum column"));

    return dataHeaders;
  }

  int findMaxRows() {
    int maxRowNum = 0;
    XPath rowNumSelector = DocumentHelper.createXPath("data/list/value[" + rowNumHeader.getColumnNumber() + "]");
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

  void extendData(List<DataColumnHeader> dataHeaders, CSVParser csvParser) {
    XPath dataSelector = DocumentHelper.createXPath("data");
    Element data = (Element) dataSelector.selectSingleNode(documentRoot);
    csvParser.forEach(csvRecord -> {
      Element list = data.addElement("list");
      dataHeaders.forEach(header -> header.extendRowWithRecord(list, csvRecord));
    });
  }

}
