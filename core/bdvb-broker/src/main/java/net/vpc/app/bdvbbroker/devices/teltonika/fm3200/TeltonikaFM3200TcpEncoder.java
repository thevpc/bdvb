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

import net.vpc.app.bdvbbroker.RichOutputStream;
import net.vpc.app.bdvbbroker.devices.teltonika.fm3200.model.*;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by vpc on 10/30/16.
 */
public class TeltonikaFM3200TcpEncoder {
    public void encode(FMPacket packet, RichOutputStream output) throws IOException {
        output.writeInt(packet.packetLength);
        output.writeUnsignedShort(packet.packetID);
        encode(packet.payload,output);

    }

    private void encode(FMPayload payload, RichOutputStream output) throws IOException {
        output.writeByte(payload.avlPacketID);
        output.writeShort(payload.mobileIMEI.length()); //000F
        output.writeASCII(payload.mobileIMEI);
        encode(payload.dataArray,output);
    }

    private void encode(FMDataArr arr, RichOutputStream output) throws IOException {
        output.writeByte(arr.codecID);
        output.writeByte(arr.data.size());
        for (int i = 0; i < arr.data.size(); i++)
        {
            encode(arr.data.get(i),output);
        }
        output.writeByte(arr.nbrRecordsBis);
    }

    private void encode(FMData data, RichOutputStream output) throws IOException {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, 1970);
        instance.set(Calendar.MONTH, Calendar.JANUARY);
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        output.writeLong(data.timestamp.getTime()-instance.getTime().getTime());
        output.writeByte(data.priority);
        encode(data.gps,output);
        encode(data.io,output);
    }

    private void encode(FMGPS data, RichOutputStream output) throws IOException {
        output.writeInt(data.longitude);
        output.writeInt(data.latitude);
        output.writeShort(data.altitude);
        output.writeShort(data.angle);
        output.writeByte(data.satellite);
        output.writeShort(data.speed);
    }

    private void encode(FMIO data, RichOutputStream output) throws IOException {
        output.writeByte(data.eventIOID);

        output.writeByte(data.allCount);
        output.writeByte(data.oneByteValues.size());
        for (int i = 0; i < data.oneByteValues.size(); i++)
        {
            FMIOValue val = data.oneByteValues.get(i);
            output.writeByte(val.id);
            output.writeByte((int) val.value);
        }

        output.writeByte(data.twoByteValues.size());
        for (int i = 0; i < data.twoByteValues.size(); i++)
        {
            FMIOValue val = data.twoByteValues.get(i);
            output.writeByte(val.id);
            output.writeShort((int) val.value);
        }

        output.writeByte(data.fourByteValues.size());
        for (int i = 0; i < data.fourByteValues.size(); i++)
        {
            FMIOValue val = data.fourByteValues.get(i);

            output.writeByte(val.id);
            output.writeUnsignedInt((long) val.value);
        }

        output.writeByte(data.eightByteValues.size());
        for (int i = 0; i < data.eightByteValues.size(); i++)
        {
            FMIOValue val = data.eightByteValues.get(i);
            output.writeByte(val.id);
            output.writeLong(Double.doubleToLongBits(val.value));
        }
    }

}
