package com.compomics.mztabparser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;

/**
 * Created by niels on 3/27/17.
 */
public class MzTabParser {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(MzTabParser.class);

    /**
     * The child directory of the input directory that contains the MGF files.
     */
    private static final String CHILD_DIRECTORY = "submitted";
    private static final String SEPARATOR = "\t";
    private static final String HEADER = "prot" + SEPARATOR + "expMZ" + SEPARATOR + "calcMZ" + SEPARATOR + "modification" + SEPARATOR + "peptide" + SEPARATOR + "charge" + SEPARATOR + "spectraRef";
    private static final String OUTPUT_EXTENSION = ".txt";

    /**
     * No-arg constructor
     */
    public MzTabParser() {
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
        LOGGER.info("started parsing mzTab files in " + inputDirectory);

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

        //look for mzTab files and parse them
        DirectoryStream<Path> mzTabFiles = Files.newDirectoryStream(childDirectory, "*.{mztab,mzTab,MZTAB}");
        for (Path mzTab : mzTabFiles) {
            parseMzTabFile(mzTab, outputDirectory);
        }

        LOGGER.info("finished parsing mzTab files in " + inputDirectory + ", output written to " + outputDirectory);
    }

    /**
     * Parse the give mzTab file and write the output file to the output
     * directory.
     *
     * @param mzTabFile the mzTab file
     * @param outputDirectory the output directory
     */
    private void parseMzTabFile(Path mzTabFile, Path outputDirectory) {
        LOGGER.info("started parsing mzTab file " + mzTabFile);
        Path outputFile = outputDirectory.resolve(FilenameUtils.removeExtension(mzTabFile.getFileName().toString()) + OUTPUT_EXTENSION);
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            //make a new mzTab file parser
            MZTabFileParser mzTabFileParser = new MZTabFileParser(mzTabFile.toFile(), System.out);
            Collection<PSM> psms = mzTabFileParser.getMZTabFile().getPSMs();
            writer.write(HEADER);
            for (PSM psm : psms) {
                writer.newLine();
                writer.write(psm.getAccession() + SEPARATOR + psm.getExpMassToCharge() + SEPARATOR + psm.getCalcMassToCharge() + SEPARATOR + psm.getModifications() + SEPARATOR + psm.getSequence() + SEPARATOR + psm.getCharge() + SEPARATOR + psm.getSpectraRef());
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

}
