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
import net.vpc.app.bdvbbroker.repository.BdvbPacketTransform;
import net.vpc.app.bdvbbroker.repository.BdvbRepository;
import net.vpc.app.bdvbbroker.util.Version;

import java.util.Date;

/**
 * Created by vpc on 10/30/16.
 */
public interface BdvbBroker {

    Version getVersion();

    void register(Class<? extends BdvbDeviceDriver> driverClass);

    void registerDefaultDrivers();

    void registerDriver(BdvbDeviceDriver driver);

    void addDefaultServices();

    void addService(String driverDefaultName);

    void addService(String driverDefaultName, JsonObject config);

    void addService(Class<? extends BdvbDeviceDriver> cls);

    void addService(Class<? extends BdvbDeviceDriver> cls, JsonObject config);

    void addService(String id, Class<? extends BdvbDeviceDriver> cls, JsonObject config);

    JsonObject getBootConfig();

    BdvbRepository getRepository();

    void start();

    void stop();

    BdvbTcpTransportManager getTcpManager();

    void publish(BdvbPacket packet);

    void addPacketListener(BdvbPacketListener listener);

    void removePacketListener(BdvbPacketListener listener);
    long updateUniformValue(String ownerUUID, String deviceUUID, Date from, Date to);

}
