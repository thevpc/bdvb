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

import net.vpc.app.bdvbbroker.BdvbFactory;
import net.vpc.app.bdvbbroker.BdvbBroker;
import net.vpc.app.bdvbbroker.BdvbPacketPrintStreamLogger;
import net.vpc.app.bdvbbroker.mock.FileMock;
//import org.junit.Test;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by vpc on 10/30/16.
 */
public class BdvbBrokerServerAndClientTest {
    //@Test
    public void startServer() {
        BdvbBroker broker = BdvbFactory.createBroker();
        broker.registerDefaultDrivers();
        broker.addService("net.vpc.app.bdvbbroker.devices.ulbotech.tracking.UlbotechTracking");
        broker.addPacketListener(new BdvbPacketPrintStreamLogger());
        broker.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            t361.getConfig()
                    .setFiles(new File[]{
                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/VIN data text format.txt"),
                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/Text data.txt"),
                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/J1708 data text format.txt"),
                            new File("/data/vpc/Data/work/entities/faurecia/bigdata/resources/T361/SourceExamples/J1939 data text format.txt")
                    })
                    .setPort(38096)
                    .setServerAddress(InetAddress.getLocalHost())
                    .setRepeatInterval(1)
                    .setRepeatCount(4)
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

        broker.stop();
        System.out.println("END!!!");
    }
}
