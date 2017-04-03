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
package net.vpc.app.bdvbbroker.client;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 11/30/16.
 */
public class DefaultBdvbClient implements BdvbClient{
    private JsonObject bootConfig;
    private BdvbRepositoryClient repositoryClient;
    public DefaultBdvbClient(JsonObject bootConfig) {
        this.bootConfig=bootConfig;
    }

    private synchronized BdvbRepositoryClient getRepo(){
        if(repositoryClient==null){
            repositoryClient=new BdvbRepositoryClientMongodb();
            repositoryClient.start(bootConfig);
        }
        return repositoryClient;
    }

    @Override
    public void stop() {
        if(repositoryClient!=null){
            repositoryClient.stop();
        }
    }

    @Override
    public void resetPackets(String ownerUUID,String deviceUUID) {
        getRepo().resetPackets(ownerUUID,deviceUUID);
    }

    @Override
    public List<BdvbPacket> findByDeviceUUID(String ownerUUID,String deviceUUID, Date from, Date to, int maxCount,BdvbPacketTransform transform) {
        return getRepo().findByDeviceUUID(ownerUUID,deviceUUID, from, to, maxCount,transform);
    }

    @Override
    public Set<String> findDistinctDeviceUUID(String ownerUUID, Date from, Date to, int maxCount) {
        return getRepo().findDistinctDeviceUUID(ownerUUID, from, to, maxCount);
    }
}
