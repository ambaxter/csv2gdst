package org.redhat.csv2gdst;

import org.dom4j.DocumentException;
import picocli.CommandLine;

import java.io.*;

@CommandLine.Command(name = "csv2gdst.jar", subcommands = {ImportCommand.class, ConvertCommand.class, CommandLine.HelpCommand.class},
  synopsisSubcommandLabel = "COMMAND",
  mixinStandardHelpOptions = true, version = "0.9")
public class Csv2Gdst {

  public static void main(String... args) throws DocumentException, IOException {
    CommandLine commandLine = new CommandLine(new Csv2Gdst());
    commandLine.setExecutionStrategy(new CommandLine.RunLast());
    int exitCode = commandLine.execute(args);
    System.exit(exitCode);
  }

}
