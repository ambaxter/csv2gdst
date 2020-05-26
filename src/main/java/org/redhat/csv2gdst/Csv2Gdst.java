package org.redhat.csv2gdst;

import org.dom4j.DocumentException;
import picocli.CommandLine;

import java.io.*;

public class Csv2Gdst {

  public static void main(String... args) throws DocumentException, IOException {
    CommandLine commandLine = new CommandLine(new MoveCommand())
      .addSubcommand("move", new MoveCommand());

    int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }

}
