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
package net.vpc.app.bdvbbroker.mock;

import net.vpc.app.bdvbbroker.RichOutputStream;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by vpc on 10/30/16.
 */
public class URLMock extends AbstractTcpMock{
    public static class FileConfig extends Config{
        private URL[] url = new URL[0];


        public URL[] getUrls() {
            return url;
        }

        public FileConfig setUrls(URL[] files) {
            this.url = files;
            return this;
        }
    }

    public URLMock() {
        super(new FileConfig());
    }

    public FileConfig getConfig() {
        return (FileConfig) super.getConfig();
    }


    protected void send(RichOutputStream os) throws IOException {
        URL[] urls = getConfig().getUrls();
        URL file = urls[(int) (Math.random() * urls.length)];
        try {
            os.write(Files.readAllBytes(Paths.get(file.toURI())));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}
