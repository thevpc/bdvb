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
package net.vpc.app.bdvbbroker.repository;

import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.BdvbPacket;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 11/17/16.
 */
public class BdvbRepositoryNone implements BdvbRepository {
    public void start(JsonObject config) {
    }

    public void store(BdvbPacket packet) {
    }

    public void stop() {
    }

    @Override
    public void resetPackets(String ownerUUID, String deviceUUID) {

    }

    @Override
    public Set<String> findDevices(String ownerUUID, Date from, Date to, int maxCount) {
        return Collections.emptySet();
    }

    @Override
    public List<BdvbPacket> findPackets(String ownerUUID, String deviceUUID, Date from, Date to, int maxCount, BdvbPacketTransform transform) {
        return Collections.emptyList();
    }

    @Override
    public long findAndUpdate(String ownerUUID, String deviceUUID, Date from, Date to, BdvbPacketTransform transform) {
        return 0;
    }

    @Override
    public long findPacketsCount(String ownerUUID, String deviceUUID, Date from, Date to) {
        return 0;
    }

    @Override
    public long findAndDelete(String ownerUUID, String deviceUUID, Date from, Date to) {
        return 0;
    }
}
