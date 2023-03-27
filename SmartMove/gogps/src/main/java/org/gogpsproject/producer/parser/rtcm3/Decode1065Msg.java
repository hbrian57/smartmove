package org.gogpsproject.producer.parser.rtcm3;


import android.location.GnssStatus;

import org.gogpsproject.Constants;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;
import org.gogpsproject.util.Bits;

import java.util.LinkedHashMap;

public class Decode1065Msg implements Decode {

    public Object decode(boolean[] bits, int referenceTS) {

        // Specific to the constellation
        int system = GnssStatus.CONSTELLATION_GLONASS;
        int indexPrn = 5;
        int offsetPrn = 0;
        int ncode = 4;

        int i = 0;
        // Decode header
        i += 12;

        double tod = (double) Bits.bitsToUInt(Bits.subset(bits, i, 17)); i += 17;
        i += 4; // Update interval
        i += 1; // Multiple Message Indicator
        int iod = (int) Bits.bitsToUInt(Bits.subset(bits, i, 4)); i += 4;
        i += 16; // SSR Provider ID
        i += 4; // SSR Solution ID
        int nsat = (int) Bits.bitsToUInt(Bits.subset(bits, i, 6)); i += 6;



        SatelliteCodeBiases scb = new SatelliteCodeBiases();
        // Decode body
        for(int j = 0; j < nsat && (i + 5 + indexPrn <= bits.length*8); j++)
        {
            int prn = (int) Bits.bitsToUInt(Bits.subset(bits, i, indexPrn)) + offsetPrn; i += indexPrn;
            int nbias = (int) Bits.bitsToUInt(Bits.subset(bits, i, 5)); i += 5;

            LinkedHashMap<Integer, Double> biases = new LinkedHashMap<>();

            for(int k = 0; k < nbias && (i + 19 <= bits.length*8) ; k++)
            {
                int mode = (int) Bits.bitsToUInt(Bits.subset(bits, i, 5)); i += 5;
                Double bias = (double) Bits.bitsTwoComplement(Bits.subset(bits, i, 14)) * 0.01; i += 14;

                if(mode > ncode)
                {
                    //Log.e("RTCM", "Unsupported code for DCB: " + mode);
                    continue;
                }

                int code = Constants.CODES_GLONASS[mode];

                biases.put(code, bias);
            }

            SatelliteCodeBiases.CodeBias cb = new SatelliteCodeBiases.CodeBias(system, prn, 0, iod, biases);
            cb.setTod(tod);
            scb.setCodeBias(cb);
        }

        return scb;


    }
}
