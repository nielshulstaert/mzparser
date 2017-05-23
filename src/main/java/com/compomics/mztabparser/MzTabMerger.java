package com.compomics.mztabparser;

import com.compomics.mztabparser.model.MoffResultLine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;

/**
 * Created by niels on 3/27/17.
 */
public class MzTabMerger {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(MzTabMerger.class);

    /**
     * The child directory of the input directory that contains the MGF files.
     */
    private static final String CHILD_DIRECTORY = "submitted";
    private static final String SEPARATOR = "\t";
    private static final String SPECTRUM_REFERENCE_PREFIX = "index=";
    private static final String HEADER = "prot" + SEPARATOR + "expMZ" + SEPARATOR + "calcMZ" + SEPARATOR + "modification" + SEPARATOR + "peptide" + SEPARATOR + "charge" + SEPARATOR + "spectraRef";
    private static final String PRIDE_MZTAB_EXTENSION = ".pride.mztab";
    private static final String MZTAB_EXTENSION = ".mztab";
    private static final String MOFF_RESULT_EXTENSION = "_moff_result.txt";
    private static final String INTENSITY = "intensity";
    private static final String RT_PEAK = "rt_peak";
    private static final String LWHM = "lwhm";
    private static final String RWHM = "rwhm";
    private static final String SNR = "SNR";
    private static final String LOG_L_R = "log_L_R";
    private static final String LOG_INT = "log_int";

    /**
     * No-arg constructor
     */
    public MzTabMerger() {

    }

    /**
     * Parse the mzTab files in the input directory, merge them with the moff
     * result files in the output directory and write the resulting mzTab file
     * to the output directory.
     *
     * @param inputDirectory the input directory
     * @param outputDirectory the output directory
     * @throws IOException in case of directory read problem
     * @throws IllegalArgumentException in case of invalid input or output
     * locations
     */
    public void parseAndMerge(Path inputDirectory, Path outputDirectory) throws IOException {
        LOGGER.info("started parsing mzTab files in " + inputDirectory + " and merging the moff result files in " + outputDirectory);

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

        //look for mzTab files
        PathMatcher mzTabFileMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{mztab,mzTab,MZTAB}");
        List<Path> mzTabFiles = Files.list(childDirectory).filter(file -> mzTabFileMatcher.matches(file.getFileName())).collect(Collectors.toList());

        //look for moff result files        
        PathMatcher moffResultFileMatcher = FileSystems.getDefault().getPathMatcher("glob:*{_moff_result.txt}");
        List<Path> moffResultFiles = Files.list(outputDirectory).filter(file -> moffResultFileMatcher.matches(file.getFileName())).collect(Collectors.toList());

        //for each mzTab file, find the matching moff result file in the output directory
        for (Path mzTabFile : mzTabFiles) {
            Optional<Path> foundMoffResult = moffResultFiles.stream()
                    .filter((moffResultFile) -> {
                        return isMatchingMoffResultFile(mzTabFile, moffResultFile);
                    }).findAny();
            if (foundMoffResult.isPresent()) {
                //parse both files and merge into the output file
                LOGGER.info("started parsing mzTab file " + mzTabFile);

                Path moffResultFile = foundMoffResult.get();
                Path outputMzTabFile = outputDirectory.resolve(FilenameUtils.removeExtension(moffResultFile.getFileName().toString()) + MZTAB_EXTENSION);

                merge(mzTabFile, foundMoffResult.get(), outputMzTabFile);
            } else {
                LOGGER.warn("No matching moff result file found for " + mzTabFile.toString());
            }
        }

        LOGGER.info("finished merging, output written to " + outputDirectory);
    }

    /**
     * Merge the mzTab file with the moff result file and write it to a new
     * mzTab file.
     *
     * @param mzTabFile the mzTab file
     * @param moffResultFile the moff result file
     * @param outputMzTabFile the output mzTab file
     */
    private void merge(Path mzTabFile, Path moffResultFile, Path outputMzTabFile) {
        //read the moff result file and populate the map
        Map<Long, MoffResultLine> moffResultLines = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(moffResultFile)) {
            //skip the header
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                MoffResultLine moffResultLine = new MoffResultLine(line);
                moffResultLines.put(moffResultLine.getSpectrumIndex(), moffResultLine);
            }

            //make a new mzTab file parser
            MZTabFileParser mzTabFileParser = new MZTabFileParser(mzTabFile.toFile(), System.out);
            Collection<PSM> psms = mzTabFileParser.getMZTabFile().getPSMs();

            //add PSM moff column headers
            mzTabFileParser.getMZTabFile().getPsmColumnFactory().addOptionalColumn(INTENSITY, Double.class);
            mzTabFileParser.getMZTabFile().getPsmColumnFactory().addOptionalColumn(RT_PEAK, Double.class);
            mzTabFileParser.getMZTabFile().getPsmColumnFactory().addOptionalColumn(LWHM, Double.class);
            mzTabFileParser.getMZTabFile().getPsmColumnFactory().addOptionalColumn(RWHM, Double.class);
            mzTabFileParser.getMZTabFile().getPsmColumnFactory().addOptionalColumn(SNR, Double.class);
            mzTabFileParser.getMZTabFile().getPsmColumnFactory().addOptionalColumn(LOG_L_R, Double.class);
            mzTabFileParser.getMZTabFile().getPsmColumnFactory().addOptionalColumn(LOG_INT, Double.class);

            for (PSM psm : psms) {
                String spectraReference = psm.getSpectraRef().get(0).getReference();
                //strip the prefix
                spectraReference = spectraReference.substring(spectraReference.indexOf(SPECTRUM_REFERENCE_PREFIX) + SPECTRUM_REFERENCE_PREFIX.length(), spectraReference.length());
                Long spectrumReference = Long.valueOf(spectraReference);
                //find the mathing moff result line           
                if (moffResultLines.containsKey(spectrumReference)) {
                    MoffResultLine moffResultLine = moffResultLines.get(spectrumReference);

                    psm.setOptionColumnValue(INTENSITY, moffResultLine.getIntensity());
                    psm.setOptionColumnValue(RT_PEAK, moffResultLine.getRtPeak());
                    psm.setOptionColumnValue(LWHM, moffResultLine.getLwhm());
                    psm.setOptionColumnValue(RWHM, moffResultLine.getRwhm());
                    psm.setOptionColumnValue(SNR, moffResultLine.getSnr());
                    psm.setOptionColumnValue(LOG_L_R, moffResultLine.getLogLR());
                    psm.setOptionColumnValue(LOG_INT, moffResultLine.getLogInt());
                } else {
                    LOGGER.warn("No match in moff result file found for PSM with spectra reference " + psm.getSpectraRef().get(0).getReference());
                }
            }

            //write the output mzTab file
            OutputStream mzTabOutputStream = Files.newOutputStream(outputMzTabFile);
            mzTabFileParser.getMZTabFile().printMZTab(mzTabOutputStream);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Check if the given mzTab file matches the moff result file.
     *
     * @param mzTabFile the mzTab file
     * @param moffResultFile the moff result file
     * @return whether the files match or not
     */
    private boolean isMatchingMoffResultFile(Path mzTabFile, Path moffResultFile) {
        //strip the file names from extensions
        String mzTabFileName = mzTabFile.getFileName().toString();
        if (mzTabFileName.endsWith(PRIDE_MZTAB_EXTENSION)) {
            mzTabFileName = mzTabFileName.substring(0, mzTabFileName.indexOf(PRIDE_MZTAB_EXTENSION));
        } else {
            mzTabFileName = mzTabFileName.substring(0, mzTabFileName.indexOf(MZTAB_EXTENSION));
        }
        String moffResultFileName = moffResultFile.getFileName().toString();
        moffResultFileName = moffResultFileName.substring(0, moffResultFileName.indexOf(MOFF_RESULT_EXTENSION));
        if (mzTabFileName.equals(moffResultFileName)) {
            return true;
        } else {
            return false;
        }
    }

}
