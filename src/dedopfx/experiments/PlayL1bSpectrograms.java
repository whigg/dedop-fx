/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 by Norman Fomferra (https://github.com/forman) and contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dedopfx.experiments;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * https://sentinel.esa.int/web/sentinel/user-guides/sentinel-3-altimetry/test-data-set
 */
public class PlayL1bSpectrograms {

    public static void main(String[] args) throws IOException, LineUnavailableException {
        new PlayL1bSpectrograms().run();
    }

    private void run() throws LineUnavailableException, IOException {
        NetcdfFile ncfile = NetcdfFile.open("hydro.nc");
        //NetcdfFile ncfile = NetcdfFile.open("icesheet.nc");
        List<Variable> variables = ncfile.getVariables();
        for (Variable variable : variables) {
            System.out.println("variable = " + variable.getNameAndDimensions());
        }
        Variable measVar = ncfile.findVariable("i2q2_meas_ku_l1b_echo_sar_ku");
        Array data = measVar.read();
        int[][] intData = (int[][]) data.copyToNDJavaArray();

        int sampleRate = 44100;
        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, true);
        InputStream inputStream = new PitchModulatorInputStream(audioFormat, intData);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        SourceDataLine lineIn = (SourceDataLine) AudioSystem.getLine(info);
        lineIn.open(audioFormat);

        lineIn.start();
        int nBytesRead = 0;
        byte[] abData = new byte[1024];

        try {
            while (nBytesRead != -1) {
                nBytesRead = inputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) {
                    //System.out.println("abData = " + Arrays.toString(abData));
                    lineIn.write(abData, 0, nBytesRead);
                }
            }
        } finally {
            lineIn.drain();
            lineIn.close();
        }
    }
}
