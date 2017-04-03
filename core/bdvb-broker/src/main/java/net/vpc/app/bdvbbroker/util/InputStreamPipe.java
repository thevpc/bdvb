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
package net.vpc.app.bdvbbroker.util;

import java.io.*;

/**
 * Created by vpc on 11/23/16.
 */
public class InputStreamPipe extends InputStream{
    private OutputStream out;
    private InputStream in;

    public InputStreamPipe(InputStream in,File out) throws IOException {
        this(in,new FileOutputStream(out));
    }

    public InputStreamPipe(InputStream in,OutputStream out) {
        this.out = out;
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        int read = in.read();
        if(read>=0) {
            out.write(read);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = in.read(b, off, len);
        if(count>0) {
            out.write(b, off, count);
        }
        return count;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public void closeAll() throws IOException {
        close();
        closeOut();
    }

    public void closeOut() throws IOException {
        out.close();
    }
}
