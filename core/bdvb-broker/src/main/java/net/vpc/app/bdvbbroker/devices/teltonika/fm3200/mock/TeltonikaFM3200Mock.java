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
package net.vpc.app.bdvbbroker.devices.teltonika.fm3200.mock;

import net.vpc.app.bdvbbroker.RichOutputStream;
import net.vpc.app.bdvbbroker.devices.teltonika.fm3200.TeltonikaFM3200TcpEncoder;
import net.vpc.app.bdvbbroker.devices.teltonika.fm3200.model.FMPacket;
import net.vpc.app.bdvbbroker.mock.AbstractTcpMock;

import java.io.IOException;

/**
 * Created by vpc on 10/30/16.
 */
public class TeltonikaFM3200Mock extends AbstractTcpMock {
    public static class FMConfig extends Config{
        private String imei="356938035643809";


        public String getImei() {
            return imei;
        }

        public FMConfig setImei(String imei) {
            this.imei = imei;
            return this;
        }

    }

    public TeltonikaFM3200Mock() {
        super(new FMConfig());
    }

    public FMConfig getConfig() {
        return (FMConfig) super.getConfig();
    }

    protected void send(RichOutputStream os) throws IOException {
        TeltonikaFM3200TcpEncoder d = new TeltonikaFM3200TcpEncoder();
        FMPacket packet = nextPacket();
        System.out.println(">>["+getConfig().getImei()+"] "+packet);
        d.encode(packet, os);
    }

    private short lastPacketID=0;
    private FMPacket nextPacket() {
        lastPacketID++;
        FMPacket fmPacket = new FMPacket();
        fmPacket.packetID=lastPacketID;
        fmPacket.payload.mobileIMEI=getConfig().getImei();
        fmPacket.payload.avlPacketID=(byte)(fmPacket.packetID%255);
        fmPacket.payload.imeiheader=15;
        return fmPacket;
    }
}
