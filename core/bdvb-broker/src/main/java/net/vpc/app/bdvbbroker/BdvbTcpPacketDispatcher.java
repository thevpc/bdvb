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

import java.io.IOException;

/**
 * Created by vpc on 10/30/16.
 */
public class BdvbTcpPacketDispatcher implements BdvbTcpConnectionListener {
    private BdvbTcpInfo info;
    private BdvbBroker broker;
    private BdvbTcpDecoder decoder;
    private String driverId;

    public BdvbTcpPacketDispatcher(BdvbTcpInfo info, BdvbTcpDecoder decoder, BdvbBroker broker, String driverId) {
        this.info = info;
        this.decoder = decoder;
        this.broker = broker;
        this.driverId = driverId;
    }

    public String getDriverId() {
        return driverId;
    }

    public BdvbTcpInfo getInfo() {
        return info;
    }

    public void onOpenConnection(BdvbTcpConnection connection) {
        while (true) {
            try {
                decoder.decode(connection);
            } catch (java.io.EOFException e) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
