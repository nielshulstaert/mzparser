/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.mztabparser.model;

/**
 *
 * @author niels
 */
public class MoffResultLine {

    private static final char DELIMITER = '\t';
    private static final int CHARGE = 0;
    private static final int SPECTRUM_INDEX = 1;
    private static final int RT = 2;
    private static final int MZ = 3;
    private static final int INTENSITY = 4;
    private static final int RT_PEAK = 5;
    private static final int LWHM = 6;
    private static final int RWHM = 7;
    private static final int FIVE_P_NOISE = 8;
    private static final int TEN_P_NOISE = 9;
    private static final int SNR = 10;
    private static final int LOG_L_R = 11;
    private static final int LOG_INT = 12;

    private final Long spectrumIndex;
    private final double intensity;
    private final double rtPeak;
    private final double lwhm;
    private final double rwhm;
    private final double snr;
    private final double logLR;
    private final double logInt;

    /**
     * This constructor takes a line from the moff result file.
     *
     * @param moffResultString the moff result file line
     */
    public MoffResultLine(String moffResultString) {
        String[] fields = moffResultString.split(String.valueOf(DELIMITER), -1);

        //populate the fields
        this.spectrumIndex = Long.valueOf(fields[SPECTRUM_INDEX]);
        this.intensity = Double.valueOf(fields[INTENSITY]);
        this.rtPeak = Double.valueOf(fields[RT_PEAK]);
        this.lwhm = Double.valueOf(fields[LWHM]);
        this.rwhm = Double.valueOf(fields[RWHM]);
        this.snr = Double.valueOf(fields[SNR]);
        this.logLR = Double.valueOf(fields[LOG_L_R]);
        this.logInt = Double.valueOf(fields[LOG_INT]);
    }

    public Long getSpectrumIndex() {
        return spectrumIndex;
    }

    public double getIntensity() {
        return intensity;
    }

    public double getRtPeak() {
        return rtPeak;
    }

    public double getLwhm() {
        return lwhm;
    }

    public double getRwhm() {
        return rwhm;
    }

    public double getSnr() {
        return snr;
    }

    public double getLogLR() {
        return logLR;
    }

    public double getLogInt() {
        return logInt;
    }

}
