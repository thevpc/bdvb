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

import net.vpc.app.bdvbbroker.mock.FileMock;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 10/30/16.
 */
public class BdvbBrokerClientTest {
    public static void main(String[] args) {

//        TeltonikaFM3200Mock fm = new TeltonikaFM3200Mock();
//        try {
//            fm.getConfig()
//                    .setPort(1234)
//                    .setServerAddress(InetAddress.getLocalHost())
//                    .setRepeatInterval(1)
//                    .setRepeatCount(3)
//            ;
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
        //fm.startAndWait();

        FileMock t361 = new FileMock();
        try {
            String PREFIX = "/home/vpc/workspace/faurecia";
            List<File> files=new ArrayList<>();
            for (String file : new String[]{
//                    "77.154.225.95",
//                    "92.90.20.65",
//                    "77.154.202.194",
//                    "77.154.204.48",
//                    "92.90.17.127",
//                    "77.154.202.167",
//                    "92.90.20.78",
                    "197.28.169.139"

            }) {
                files.add(new File(PREFIX +"/"+file));
            }
            t361.getConfig()
                    .setFiles(
                            files.toArray(new File[files.size()])
//                            new File("//home/vpc/workspace/faurecia/197.9.42.148"),
//                            new File("//home/vpc/workspace/faurecia/exemple"),
//                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/VIN data text format.txt"),
//                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/Text data.txt"),
//                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/J1708 data text format.txt"),
//                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/J1939 data text format.txt")
                    )
                    .setPort(38096)
                    .setServerAddress(InetAddress.getLocalHost())
//                    .setServerAddress(InetAddress.getByName("eniso.info"))
                    .setRepeatInterval(1)
                    .setRepeatCount(50)
            ;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        t361.startAndWait();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("END!!!");
    }
}
