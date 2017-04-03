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
package net.vpc.app.bdvbbroker.test;

import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.devices.ulbotech.tracking.UlbotechTrackingTcpDecoder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by vpc on 11/20/16.
 */
public class TestT361 {
    public static void main(String[] args) {
        File file = new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/VIN data text format.txt");
//        File file=new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/Text data.txt");
//        File file=new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/J1708 data text format.txt");
//        File file=new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/J1939 data text format.txt");
        Path path = file.toPath();
        try {
            byte[] data = Files.readAllBytes(path);
            UlbotechTrackingTcpDecoder d = new UlbotechTrackingTcpDecoder();
            for (JsonObject frame : d.DataDecode(data)) {
                System.out.println(frame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
