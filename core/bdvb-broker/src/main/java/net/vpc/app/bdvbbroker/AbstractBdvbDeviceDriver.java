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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 10/30/16.
 */
public abstract class AbstractBdvbDeviceDriver implements BdvbDeviceDriver {
    private BdvbBroker broker;
    private Map<String,BdvbDeviceBrokerRuntime> configs=new HashMap<String, BdvbDeviceBrokerRuntime>();

    @Override
    public String getDefaultServiceId() {
        return getClass().getName();
    }

    @Override
    public String getDriverId() {
        return getClass().getName();
    }

    public void install(BdvbBroker broker) {
        this.broker = broker;
    }

    public void start(String id, JsonObject config) {
        if(configs.containsKey(id)){
            throw new IllegalArgumentException(id+" already registered");
        }
        if(config==null){
            config=new JsonObject();
        }
        Object rt= null;
        try {
            rt = startChannel(id,config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        configs.put(id,new BdvbDeviceBrokerRuntime(
                id,config,rt
        ));
    }

    protected abstract Object startChannel(String id, JsonObject config) throws Exception;

    public void stop(String id) {

    }

    public BdvbBroker getBroker() {
        return broker;
    }
}
