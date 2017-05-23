package com.compomics.mztabparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;

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
