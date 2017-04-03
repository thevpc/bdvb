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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.devices.teltonika.fm3200.TeltonikaFM3200TcpDecoder;
import net.vpc.app.bdvbbroker.util.Utils;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by vpc on 10/30/16.
 */
public abstract class SimpleBdvbDeviceDriver extends AbstractBdvbDeviceDriver {
    private JsonObject defaultConfig;
    private BdvbTcpDecoder tcpdecoder;
    private String serviceId;

    public SimpleBdvbDeviceDriver(BdvbTcpDecoder tcpdecoder,String defaultConfig) {
        this(tcpdecoder,defaultConfig==null?null:Utils.parseJsonObject(defaultConfig));
    }

    public SimpleBdvbDeviceDriver(BdvbTcpDecoder tcpdecoder,JsonObject defaultConfig) {
        this.tcpdecoder = tcpdecoder;
        this.defaultConfig = defaultConfig;
        this.serviceId = getClass().getName();
    }

    protected Object startChannel(String id, JsonObject config) throws IOException {
        //config=new DelegateBdvbConfig(config,defaultConfig);
        JsonObject c1 = (JsonObject) Utils.getJsonValue(config, "services", serviceId);
        JsonObject c0 = (JsonObject) Utils.getJsonValue(defaultConfig, "services", serviceId);

        JsonElement[] conf = {c1, c0};
        String protocol = Utils.getJsonValueString(conf,"protocol");
        if(tcpdecoder!=null && "tcp".equalsIgnoreCase(protocol)) {
            final String server = Utils.getJsonValueString(conf,"address");
            final int port = Utils.getOrDefault(Utils.getJsonValueInt(conf,"port"),7777);
            final int listen = Utils.getOrDefault(Utils.getJsonValueInt(conf,"backlog"),10);
            getBroker().getTcpManager().addListener(
                    new BdvbTcpPacketDispatcher(
                            new BdvbTcpInfo(serviceId,
                                    port, listen, server==null?null:InetAddress.getByName(server)
                            ),tcpdecoder,getBroker(),id
                    ));
        }else{
            throw new IllegalArgumentException(getClass().getSimpleName()+" : Unsupported protocol "+protocol);
        }
        return null;
    }
}
