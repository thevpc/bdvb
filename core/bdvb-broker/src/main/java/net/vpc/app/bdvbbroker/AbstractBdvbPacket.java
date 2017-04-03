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

import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.util.Utils;

import java.util.Date;

/**
 * Created by vpc on 11/17/16.
 */
public class AbstractBdvbPacket implements BdvbPacket {
    public JsonObject raw;
    public JsonObject value;
    private String deviceDriver;
    private String deviceType;
    private String deviceUUID;
    private String deviceAddress;
    private String deviceFullAddress;
    private Date serverTime;
    private Date deviceTime;

    public String getDeviceDriver() {
        return deviceDriver;
    }

    public void setDeviceDriver(String deviceDriver) {
        this.deviceDriver = deviceDriver;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public Date getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(Date deviceTime) {
        this.deviceTime = deviceTime;
    }

    public JsonObject getRaw() {
        return raw;
    }

    public void setRaw(JsonObject raw) {
        this.raw = raw;
    }

    public JsonObject getValue() {
        return value;
    }

    public void setValue(JsonObject value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(raw);
    }

    @Override
    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public String getDeviceFullAddress() {
        return deviceFullAddress;
    }

    public void setDeviceFullAddress(String deviceFullAddress) {
        this.deviceFullAddress = deviceFullAddress;
    }

    @Override
    public BdvbPacket simplify() {
        DefaultBdvbPacket d=new DefaultBdvbPacket();
        d.setDeviceDriver(getDeviceDriver());
        d.setDeviceType(getDeviceType());
        d.setDeviceUUID(getDeviceUUID());
        d.setDeviceTime(getDeviceTime());
        d.setServerTime(getServerTime());
        d.setDeviceAddress(getDeviceAddress());
        d.setDeviceFullAddress(getDeviceFullAddress());
        JsonObject value = getRaw();
        d.setRaw(value==null?null: (JsonObject) Utils.simplify(value));
        value = getValue();
        d.setValue(value==null?null: (JsonObject) Utils.simplify(value));
        return d;
    }

}
