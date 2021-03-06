/**
 * ====================================================================
 * Big Data Vista Baby
 * <p>
 * is a new Open Source IoT Broker and HAL Server for Iot Devices.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.bdvbbroker;

import com.google.gson.JsonObject;

/**
 * Created by vpc on 10/30/16.
 */
public interface BdvbDeviceDriver {
    String getDriverId();

    String getDefaultServiceId();

    void install(BdvbBroker broker);

    void start(String id, JsonObject config);

    JsonObject uniformPacket(JsonObject raw);

    void stop(String id);
}
