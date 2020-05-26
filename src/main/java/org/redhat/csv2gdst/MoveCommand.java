package org.redhat.csv2gdst;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.input.BOMInputStream;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.redhat.csv2gdst.headers.*;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;

public class MoveCommand implements Callable<Integer> {

  @CommandLine.Option(names = "-m", defaultValue = "extend",
    description = "Handling mode for existing gdst data: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})")
  MoveDataType handlingMode;

  @CommandLine.Option(names = "-i", required = true, paramLabel = "INPUT",
    description = "input gdst file")
  File gdstFile;

  @CommandLine.Option(names = "-o", required = true, paramLabel = "OUTPUT",
    description = "output gdst file")
  File outGdstFile;

  @CommandLine.Parameters(arity = "1..*", paramLabel = "CSVs",
    description = "one ore more CSV files to import")
  List<File> csvFiles;

  public enum MoveDataType {
    replace, extend
  }

  void moveDataWith(GdstDocument gdstDocument, MoveDataType moveDataType, CSVParser csvParser) {
    switch (moveDataType) {
      case replace:
        gdstDocument.clearDataRows();
        break;
      case extend:
        gdstDocument.updateHeadersWithExistingData();
        break;
    }
    gdstDocument.extendData(csvParser);
  }

    @Override
    public Integer call() throws Exception {
      SAXReader saxReader = new SAXReader();
      Document document = saxReader.read(gdstFile);
      GdstDocument gdstDocument = new GdstDocument(document);

      for (File csvFile : csvFiles) {
        System.out.println("Parsing " + csvFile.getAbsolutePath());
        InputStream csvInputStream = new FileInputStream(csvFile);
        BOMInputStream bomInputStream = new BOMInputStream(csvInputStream);
        CSVParser csvParser = CSVParser.parse(bomInputStream, StandardCharsets.UTF_8,
          CSVFormat.DEFAULT.withHeader().withNullString("").withTrim(true));
        moveDataWith(gdstDocument, handlingMode, csvParser);
        handlingMode = MoveCommand.MoveDataType.extend;
        bomInputStream.close();
      }

      OutputFormat outputFormat = new OutputFormat("  ", true, "UTF-8");
      outputFormat.setOmitEncoding(true);
      outputFormat.setSuppressDeclaration(true);
      outputFormat.setTrimText(true);
      XMLWriter xmlWriter = new XMLWriter(outputFormat);
      FileOutputStream outputStream = new FileOutputStream(outGdstFile);
      xmlWriter.setOutputStream(outputStream);
      xmlWriter.write(document);
      xmlWriter.close();
      return 0;
    }

}
