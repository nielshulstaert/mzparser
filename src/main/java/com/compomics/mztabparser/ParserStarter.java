/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.mztabparser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author niels
 */
public class ParserStarter {

    private static final Logger LOGGER = LogManager.getLogger(ParserStarter.class);

    private static final String HEADER = "[MZ Parser]\n";
    private static final String USAGE = "java -jar <jar file name>";
    private static Options options;

    /**
     * Main executable.
     *
     * @param commandLineArguments the command-line arguments.
     */
    public static void main(String[] commandLineArguments) {
        constructOptions();

        //displayBlankLines(1, System.out);
        //displayHeader(System.out);
        //displayBlankLines(2, System.out);
        parse(commandLineArguments);
    }

    /**
     * Parse the MGF files in the given input directory and write the output
     * file to the output directory.
     *
     * @param inputDirectory the input directory
     * @param outputDirectory the output directory
     */
    public static void parseMgfFiles(Path inputDirectory, Path outputDirectory) {
        MgfParser mgfParser = new MgfParser();
        try {
            mgfParser.parse(inputDirectory, outputDirectory);
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.error(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Parse the mzTab files in the input directory, merge them with the moff
     * result files in the output directory and write the resulting mzTab file
     * to the output directory.
     *
     * @param inputDirectory the input directory
     * @param outputDirectory the output directory
     */
    public static void parseAndMergeMzTabFiles(Path inputDirectory, Path outputDirectory) {
        MzTabMerger mzTabMerger = new MzTabMerger();
        try {
            mzTabMerger.parseAndMerge(inputDirectory, outputDirectory);
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.error(ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Apply Apache Commons CLI parser to command-line arguments.
     *
     * @param commandLineArguments the command-line arguments to be processed.
     */
    private static void parse(String[] commandLineArguments) {
        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = cmdLineParser.parse(options, commandLineArguments);
            if (commandLine.hasOption('h')) {
                printHelp(
                        options, 80, "Help", "End of Help",
                        5, 3, true, System.out);
            }
            if (commandLine.hasOption('u')) {
                printUsage(USAGE, options, System.out);
            }
            Path inputDirectory = null;
            Path outputDirectory = null;
            if (commandLine.hasOption('i')) {
                String inputDirectoryString = commandLine.getOptionValue('i');
                inputDirectory = Paths.get(inputDirectoryString);
                if (Files.exists(inputDirectory) && Files.isDirectory(inputDirectory)) {
                    //do nothing
                } else {
                    System.out.println("No input directory with path \"" + inputDirectoryString + "\" could be found.");
                    printHelp(
                            options, 80, "Help", "End of Help",
                            5, 3, true, System.out);
                }
            }
            if (commandLine.hasOption('o')) {
                String outputDirectoryString = commandLine.getOptionValue('o');
                outputDirectory = Paths.get(outputDirectoryString);
                if (Files.exists(outputDirectory) && Files.isDirectory(outputDirectory)) {
                    //do nothing
                } else {
                    System.out.println("No output directory with path \"" + outputDirectoryString + "\" could be found.");
                    printHelp(
                            options, 80, "Help", "End of Help",
                            5, 3, true, System.out);
                }
            }
            if (commandLine.hasOption('m')) {
                parseMgfFiles(inputDirectory, outputDirectory);
            } else if (commandLine.hasOption('z')) {
                parseAndMergeMzTabFiles(inputDirectory, outputDirectory);
            } else {
                System.out.println("Please provide either the MGF of mzTab parse option.");
                printHelp(
                        options, 80, "Help", "End of Help",
                        5, 3, true, System.out);
            }
        } catch (ParseException parseException) {
            System.out.println("Encountered exception while parsing :\n"
                    + parseException.getMessage());
            printHelp(
                    options, 80, "Help", "End of Help",
                    5, 3, true, System.out);
        }
    }

    /**
     * Construct Options.
     *
     */
    private static void constructOptions() {
        options = new Options();

        options.addOption("h", "help", false, "Help");
        options.addOption("u", "usage", false, "Usage");

        Option inputOption = new Option("i", "input", true, "Input directory");
        inputOption.setArgName("input");
        options.addOption(inputOption);

        Option outputOption = new Option("o", "output", true, "Output directory");
        outputOption.setArgName("output");
        options.addOption(outputOption);

        Option mgfOption = new Option("m", "mgf", false, "MGF parsing");
        outputOption.setArgName("mgf");
        Option mzTabOption = new Option("z", "mztab", false, "mzTab parsing and merging");
        outputOption.setArgName("mztab");
        OptionGroup parsingOptionGroup = new OptionGroup();
        parsingOptionGroup.setRequired(true);
        parsingOptionGroup.addOption(mgfOption);
        parsingOptionGroup.addOption(mzTabOption);
        options.addOptionGroup(parsingOptionGroup);
    }

    /**
     * Display example application header.
     *
     * @out OutputStream to which header should be written.
     */
    private static void displayHeader(OutputStream out) {
        try {
            out.write(HEADER.getBytes());
        } catch (IOException ioEx) {
            System.out.println(HEADER);
        }
    }

    /**
     * Write the provided number of blank lines to the provided OutputStream.
     *
     * @param numberBlankLines Number of blank lines to write.
     * @param out OutputStream to write the blank lines to.
     */
    private static void displayBlankLines(
            int numberBlankLines,
            OutputStream out) {
        try {
            for (int i = 0; i < numberBlankLines; ++i) {
                out.write("\n".getBytes());
            }
        } catch (IOException ioEx) {
            for (int i = 0; i < numberBlankLines; ++i) {
                System.out.println();
            }
        }
    }

    /**
     * Print usage information to provided OutputStream.
     *
     * @param applicationName Name of application to list in usage.
     * @param options Command-line options to be part of usage.
     * @param out OutputStream to write the usage information to.
     */
    private static void printUsage(
            String applicationName,
            Options options,
            OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        HelpFormatter usageFormatter = new HelpFormatter();
        usageFormatter.printUsage(writer, 80, applicationName, options);
        writer.flush();
    }

    /**
     * Write "help" to the provided OutputStream.
     */
    private static void printHelp(
            Options options,
            int printedRowWidth,
            String header,
            String footer,
            int spacesBeforeOption,
            int spacesBeforeOptionDescription,
            boolean displayUsage,
            final OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                USAGE,
                header,
                options,
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);
        writer.flush();
    }

}
