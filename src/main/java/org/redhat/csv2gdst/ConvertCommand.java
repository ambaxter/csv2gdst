package org.redhat.csv2gdst;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.redhat.csv2gdst.headers.DataColumnType;
import org.redhat.csv2gdst.headers.HeaderDefinition;
import picocli.CommandLine;

import java.io.File;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "convert", description = "Convert a gdst file to a csv file", mixinStandardHelpOptions = true)
public class ConvertCommand implements Callable<Integer> {

  @CommandLine.Option(names = "-i", required = true, paramLabel = "INPUT",
    description = "input gdst file")
  File gdstFile;

  @CommandLine.Option(names = "-o", required = true, paramLabel = "OUTPUT",
    description = "input gdst file")
  File csvFile;

  @CommandLine.Option(names = "-t", description = "Output header only")
  boolean headerOnly;

  @Override
  public Integer call() throws Exception {
    SAXReader saxReader = new SAXReader();
    Document document = saxReader.read(gdstFile);
    GdstDocument gdstDocument = new GdstDocument(document);
    List<HeaderDefinition> headerDefinitions = gdstDocument.getDataHeaders().stream()
      .filter(h -> !DataColumnType.RowNum.equals(h.getDataColumnType()))
      .collect(Collectors.toList());
    String[] headers = headerDefinitions.stream()
      .map(HeaderDefinition::getVarName)
      .toArray(String[]::new);

    List<Pair<HeaderDefinition, XPath>> headerPaths = headerDefinitions.stream()
      .map(h -> ImmutablePair.of(h, DocumentHelper.createXPath("value[" + h.getColumnNumber() + "]")))
      .collect(Collectors.toList());

    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(headers).withNullString("").withTrim(true);

    List<Node> dataRows = gdstDocument.retrieveDataRows();


    System.out.println("Printing " + csvFile.getAbsolutePath());
    CSVPrinter csvPrinter = csvFormat.print(csvFile, StandardCharsets.UTF_8);
    if(!headerOnly) {
      for (Node list : dataRows) {
        Stream<String> iter = headerPaths.stream()
          .map(p -> p.getLeft().readRecordValue(p.getRight().selectSingleNode(list)));
        Iterable<String> iterable = iter::iterator;
        csvPrinter.printRecord(iterable);
      }
    }
    csvPrinter.close();
    return 0;
  }

}
