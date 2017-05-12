package com.compomics.mztabparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.IndexElement;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.mgf_parser.model.Ms2Query;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

/**
 * Created by niels on 3/27/17.
 */
public class Playground2 {

    public static final String SEPARATOR = "\t";

    
    
    public static void mainzz(String[] args) {
        File exampleMzTabFile = new File("/home/niels/Desktop/SILAC_CQI.mzTab");
        try {
            MZTabFileParser mzTabFileParser = new MZTabFileParser(exampleMzTabFile, System.out);
            Collection<PSM> psms = mzTabFileParser.getMZTabFile().getPSMs();

            //write output
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get("/home/niels/Desktop/testMzTabOutput.txt"))) {
                for (PSM psm : psms) {
                    bufferedWriter.write(psm.getAccession() + "\t" + psm.getCharge());
                    bufferedWriter.newLine();
                }
            }

            System.out.println("test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
