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
//import org.junit.Test;


/**
 * Created by vpc on 10/30/16.
 */
public class BdvbBrokerServerTest {
    public static void main(String[] args) {
        BdvbBroker broker = BdvbFactory.createBroker();
        broker.registerDefaultDrivers();
        broker.addService("net.vpc.app.bdvbbroker.devices.ulbotech.tracking.UlbotechTracking");
        broker.addPacketListener(new BdvbPacketPrintStreamLogger());
        broker.start();

       final Object o=new Object();
        synchronized (o){
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
