package com.compomics.mztabparser;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.mgf_parser.model.Ms2Query;

/**
 * Created by niels on 3/27/17.
 */
public class MgfParser {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(MgfParser.class);

    /**
     * The child directory of the input directory that contains the MGF files.
     */
    private static final String CHILD_DIRECTORY = "submitted";
    private static final String SEPARATOR = "\t";
    private static final String HEADER = "INDEX" + SEPARATOR + "SCANS" + SEPARATOR + "PEPMASS" + SEPARATOR + "RTINSECONDS" + SEPARATOR + "CHARGE";

    private static final String OUTPUT_EXTENSION = ".moff2start";

    /**
     * No-arg constructor
     */
    public MgfParser() {
    }

    /**
     * Parse the MGF files in the input directory and write them to the output
     * directory.
     *
     * @param inputDirectory the input directory
     * @param outputDirectory the output directory
     * @throws IOException in case of directory read problem
     * @throws IllegalArgumentException in case of invalid input or output
     * locations
     */
    public void parse(Path inputDirectory, Path outputDirectory) throws IOException {
        LOGGER.info("started parsing MGF files in " + inputDirectory);

        //check whether the provided directories exist
        if (!Files.exists(inputDirectory) || !Files.isDirectory(inputDirectory)) {
            throw new IllegalArgumentException("The input directory " + inputDirectory + " doesn't exist.");
        }
        if (!Files.exists(inputDirectory) || !Files.isDirectory(inputDirectory)) {
            throw new IllegalArgumentException("The output directory " + outputDirectory + " doesn't exist.");
        }
        //look for the child directory
        Path childDirectory = inputDirectory.resolve(CHILD_DIRECTORY);
        if (!Files.exists(childDirectory) || !Files.isDirectory(childDirectory)) {
            throw new IllegalArgumentException("The child directory " + childDirectory + " doesn't exist.");
        }

        //look for mgf files and parse them
        DirectoryStream<Path> mgfFiles = Files.newDirectoryStream(childDirectory, "*.{MGF,mgf}");
        for (Path mgfFile : mgfFiles) {
            parseMgfFile(mgfFile, outputDirectory);
        }

        LOGGER.info("finished parsing MGF files in " + inputDirectory + ", output written to " + outputDirectory);
    }

    /**
     * Parse the give MGF file and write the output file to the output
     * directory.
     *
     * @param mgfPath the MGF file
     * @param outputDirectory the output directory
     */
    private void parseMgfFile(Path mgfPath, Path outputDirectory) {
        try {
            LOGGER.info("started parsing MGF file " + mgfPath);
            //make a new MgfFile instance
            MgfFile mgfFile = new MgfFile(mgfPath.toFile());
            MgfFile.Ms2QueryIterator ms2QueryIterator = mgfFile.getMs2QueryIterator();
            Path outputFile = outputDirectory.resolve(FilenameUtils.removeExtension(mgfPath.getFileName().toString()) + OUTPUT_EXTENSION);
            try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
                writer.write(HEADER);
                while (ms2QueryIterator.hasNext()) {
                    Ms2Query ms2Query = ms2QueryIterator.next();
                    writer.newLine();
                    writer.write(ms2Query.getId() + SEPARATOR + ms2Query.getScan() + SEPARATOR + ms2Query.getPeptideMass() + SEPARATOR + ms2Query.getRetentionTime() + SEPARATOR + ms2Query.getChargeState());
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        } catch (JMzReaderException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

}
