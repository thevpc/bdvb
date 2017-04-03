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
package net.vpc.app.bdvbbroker.devices.teltonika.fm3200;

import net.vpc.app.bdvbbroker.BdvbPacket;
import net.vpc.app.bdvbbroker.BdvbTcpConnection;
import net.vpc.app.bdvbbroker.BdvbTcpDecoder;
import net.vpc.app.bdvbbroker.RichInputStream;
import net.vpc.app.bdvbbroker.devices.teltonika.fm3200.model.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by vpc on 10/30/16.
 */
public class TeltonikaFM3200TcpDecoder implements BdvbTcpDecoder{

    public void decode(BdvbTcpConnection connection) throws IOException {
        FMPacket packet = new FMPacket();
        decode(packet,connection.in);
        packet.setDeviceDriver(connection.driverId);
        packet.setDeviceUUID(packet.getImei());
        packet.setServerTime(new Date());
        packet.setDeviceTime(packet.getServerTime());
        packet.setDeviceAddress(connection.socket.getInetAddress().toString());
        packet.setDeviceFullAddress(connection.socket.getRemoteSocketAddress().toString());
        packet.setDeviceType("TeltonikaFM3200");
        connection.broker.publish(packet);
    }

    private void decode(FMPacket packet, RichInputStream input) throws IOException {
        packet.packetLength = input.readInt();
        packet.packetID = input.readUnsignedShort();

        // byte[] leng = { 0x0003 };
        // byte[] typ = { 0x02 };
        // packet.packetLength = input.ReadUnsignedShort();
        // packet.packetID = input.ReadUnsignedShort();
        // packet.packetType = input.ReadByte();
        // output.Write(new byte[] { 0, 3 });
        //// output.WriteShort((short)packet.packetLength);
        //// output.Write(leng);
        // output.WriteShort((short)packet.packetID);
        // output.Write(new byte[] {2});
        // output.Write(typ);
        //output.Write(packet.packetType);
        decode(packet.payload,input);

    }

    private void decode(FMPayload payload, RichInputStream input) throws IOException {
        payload.avlPacketID = input.readByte();
        payload.imeiheader = input.readShort(); //000F
        payload.mobileIMEI = input.readASCII(payload.imeiheader);
        decode(payload.dataArray,input);
    }

    private void decode(FMDataArr arr, RichInputStream input) throws IOException {
        arr.codecID = input.readByte();
        int nbrDocuments = input.readByte();
        for (int i = 0; i <nbrDocuments; i++)
        {
            //System.out.println("Document " + (i + 1));
            FMData d = new FMData();
            arr.data.add(d);
            decode(d,input);

        }
        arr.nbrRecordsBis = input.readByte();
    }

    private void decode(FMData data, RichInputStream input) throws IOException {
        long timestamp = input.readLong();
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, 1970);
        instance.set(Calendar.MONTH, Calendar.JANUARY);
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        instance.setTimeInMillis(instance.getTime().getTime() + timestamp);
        data.timestamp = instance.getTime();
        data.priority = input.readByte();
        decode(data.gps,input);
        decode(data.io,input);
    }

    private void decode(FMGPS data, RichInputStream input) throws IOException {
        data.longitude = input.readInt();
        data.latitude = input.readInt();
        data.altitude = input.readShort();
        data.angle = input.readShort();
        data.satellite = input.readByte();
        data.speed = input.readShort();
        double velocity = data.speed;
    }

    private void decode(FMIO data, RichInputStream input) throws IOException {
        data.eventIOID = input.readByte();

        data.allCount = input.readByte();
        int oneByteCount = input.readByte();
        for (int i = 0; i < oneByteCount; i++)
        {
            FMIOValue val = new FMIOValue();
            data.oneByteValues.add(val);
            val.id = input.readByte();
            val.value = input.readByte();
        }

        int twoByteCount = input.readByte();
        for (int i = 0; i < twoByteCount; i++)
        {
            FMIOValue val = new FMIOValue();
            data.twoByteValues.add(val);
            val.id = input.readByte();
            val.value = input.readShort();
        }

        int fourByteCount = input.readByte();
        for (int i = 0; i < fourByteCount; i++)
        {
            FMIOValue val = new FMIOValue();
            data.fourByteValues.add(val);
            val.id = input.readByte();
            val.value = input.readUnsignedDouble();
        }

        int eightByteCount = input.readByte();
        for (int i = 0; i < eightByteCount; i++)
        {
            FMIOValue val = new FMIOValue();
            data.eightByteValues.add(val);
            val.id = input.readByte();
            val.value = input.readLong();
        }
    }

}
