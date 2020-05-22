package org.redhat.csv2gdst;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.input.BOMInputStream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;

public class Csv2Gdst implements Callable<Integer> {

  @Option(names = "-m", defaultValue = "extend",
    description = "Handling mode for existing gdst data: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
  MoveCommand.MoveDataType handlingMode;

  @Option(names = "-i", required = true, paramLabel = "INPUT",
    description = "input gdst file")
  File gdstFile;

  @Option(names = "-o", required = true, paramLabel = "OUTPUT",
    description = "output gdst file")
  File outGdstFile;

  @Parameters(arity = "1..*", paramLabel = "CSVs",
    description = "one ore more CSV files to import")
  List<File> csvFiles;

  public static void main(String... args) throws DocumentException, IOException {
    Csv2Gdst csv2Gdst = new Csv2Gdst();
    CommandLine commandLine = new CommandLine(csv2Gdst);
    commandLine.execute(args);
  }

  @Override
  public Integer call() throws Exception {
    SAXReader saxReader = new SAXReader();
    Document document = saxReader.read(gdstFile);
    MoveCommand moveCommand = new MoveCommand(document);
    for (File csvFile : csvFiles) {
      System.out.println("Parsing " + csvFile.getAbsolutePath());
      InputStream csvInputStream = new FileInputStream(csvFile);
      BOMInputStream bomInputStream = new BOMInputStream(csvInputStream);
      CSVParser csvParser = CSVParser.parse(bomInputStream, StandardCharsets.UTF_8,
        CSVFormat.DEFAULT.withHeader().withNullString("").withTrim(true));
      moveCommand.moveDataWith(handlingMode, csvParser);
      handlingMode = MoveCommand.MoveDataType.extend;
      bomInputStream.close();
    }

    OutputFormat outputFormat = new OutputFormat("  ", true, "UTF-8");
    outputFormat.setOmitEncoding(false);
    outputFormat.setTrimText(true);
    XMLWriter xmlWriter = new XMLWriter(outputFormat);
    FileOutputStream outputStream = new FileOutputStream(outGdstFile);
    xmlWriter.setOutputStream(outputStream);
    xmlWriter.write(document);
    xmlWriter.close();
    return 0;
  }
}
