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

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by vpc on 10/30/16.
 */
public class RichOutputStream extends DataOutputStream{
    public RichOutputStream(OutputStream out) {
        super(out);
    }

    public void writeLengthAndASCII(String str) throws IOException {
        writeInt(str.length());
        write(str.getBytes());
    }

    public void writeASCII(String str) throws IOException {
        write(str.getBytes());
    }

    public void writeUnsignedDouble(double unsignedDouble) throws IOException {
        throw new RuntimeException("Unsupported yet");
    }
    public void writeUnsignedShort(int unsignedShort) throws IOException {
//        int ch1 = in.read();
//        int ch2 = in.read();
//        if ((ch1 | ch2) < 0)
//            throw new EOFException();
//        return (ch1 << 8) + (ch2 << 0);
//
        write((unsignedShort >>>  8) & 0xFF);
        write((unsignedShort >>>  0) & 0xFF);
    }

    public void writeUnsignedInt(long value) throws IOException {
        writeInt((int)value);
    }

}
