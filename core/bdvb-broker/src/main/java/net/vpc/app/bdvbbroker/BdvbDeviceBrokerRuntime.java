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

/**
 * Created by vpc on 10/30/16.
 */
public class BdvbDeviceBrokerRuntime {
    private String id;
    private JsonObject config;
    private Object runtime;

    public BdvbDeviceBrokerRuntime(String id, JsonObject config, Object runtime) {
        this.id = id;
        this.config = config;
        this.runtime = runtime;
    }

    public String getId() {
        return id;
    }

    public JsonObject getConfig() {
        return config;
    }

    public Object getRuntime() {
        return runtime;
    }
}
