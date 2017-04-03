/**
 * ====================================================================
 *            Big Data Vista Baby
 *
 * is a new Open Source IoT Broker and HAL Server for Iot Devices.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.bdvbbroker;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by vpc on 10/30/16.
 */
public class RichInputStream extends DataInputStream {
    public RichInputStream(InputStream in) {
        super(in);
    }

    public long readUnsignedInt() throws IOException {

        long val = (long) readInt();
        if (val < 0) {
            val += 0X100000000L;
        }
        return val;
    }

    public double readUnsignedDouble() throws IOException {
        throw new RuntimeException("Unsupported yet");
//        long val = (long) readInt();
//        if (val != 0) {
//
//        }
//        if (val < 0) {
//            val += 0X100000000L;
//        }
//
//
////        double odometerKM = (((val >> 0) & 0xFFFFFFFF) * 0.005) + 0.0;
////        //return odometerKM;
//        return val;
    }

    public String readLengthAndASCII() throws IOException {
        // read bytes length
        int length = readInt();

        // read bytes
        byte[] bytes = new byte[length];
        int left = length;

        while (left > 0)
            left -= read(bytes, length - left, left);
        // convert bytes using given encoding
        return new String(bytes, 0, length);
    }

    public String readASCII(int len) throws IOException {
        byte[] t = new byte[len];
        readFully(t, 0, len);
        return new String(t, 0, len);
    }


}
