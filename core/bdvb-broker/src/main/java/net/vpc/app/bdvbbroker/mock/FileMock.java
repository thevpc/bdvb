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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by vpc on 10/30/16.
 */
public class FileMock extends AbstractTcpMock{
    public static class FileConfig extends Config{
        private File[] files = new File[0];


        public File[] getFiles() {
            return files;
        }

        public FileConfig setFiles(File[] files) {
            this.files = files;
            return this;
        }
    }

    public FileMock() {
        super(new FileConfig());
    }

    public FileConfig getConfig() {
        return (FileConfig) super.getConfig();
    }


    protected void send(RichOutputStream os) throws IOException {
        File[] files = getConfig().getFiles();
        File file = files[(int) (Math.random() * files.length)];
        os.write(Files.readAllBytes(file.toPath()));
    }

}
