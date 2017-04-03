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
package net.vpc.app.bdvbbroker.devices.teltonika.fm3200.model;

/**
 * Created by vpc on 10/30/16.
 */
public class FMGPS {
    //4
    public int longitude ;
    //4
    public int latitude ;
    //4
    public String getLongitudes(){
        int startlon = Integer.parseInt(String.valueOf(longitude).substring(0, 1));
        if (startlon > 1)
            return String.valueOf(((double)longitude)/10.0);
        else
            return String.valueOf(((double)longitude)/100.0);
    }
    //4
    public String getLatitudes() {
        return String.valueOf(((double)latitude)/10.0);
    }
    //2
    public int altitude ;

    //2
    public int angle ;

    public byte satellite ;

    //2
    public int speed ;

    public String toString()
    {
        return "FMGPS{" + "longitude=" + longitude + ", latitude=" + latitude + ", altitude=" + altitude + ", angle=" + angle + ", satellite=" + satellite + ", speed=" + speed + '}';
    }

}
