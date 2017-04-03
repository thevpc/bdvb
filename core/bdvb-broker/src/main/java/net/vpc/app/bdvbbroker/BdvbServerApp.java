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
import net.vpc.app.bdvbbroker.cmd.Args;
import net.vpc.app.bdvbbroker.util.LogUtils;
import net.vpc.app.bdvbbroker.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by vpc on 11/30/16.
 */
public class BdvbServerApp {
    private static final Logger log = Logger.getLogger(DefaultBdvbBroker.class.getName());

    public static void main(String[] args) {
        log.info("Bootsrapping //Big Data Vista Baby// Broker...");
        Args a = new Args(args);
        if(a.containOption("--help") || a.containOption("-h")  || a.containOption("-?")){
            System.out.println("--file <JSON_FILE_PATH>");
            System.out.println("-f     <JSON_FILE_PATH>");
            System.out.println("            loads config json file <JSON_FILE_PATH>");
            System.out.println("--log-file <PATH>");
            System.out.println("            path of log file");
        }
        String file = a.getOptionValueNonEmpty("--file", "-f");
        if (file == null) {
            file = "bdvb.config";
        }
        JsonObject config = null;
        try {
            if (new File(file).exists()) {
                log.info("Loading config file : "+file);
                config = (JsonObject) Utils.parseJsonElement(new File(file));
            }
            //file pattern
            String logfile=a.getOptionValueNonEmpty("--log-file");
            if(logfile==null && config!=null){
                logfile= Utils.getJsonValueString(config,"log-file");
            }
            //max file size
            String logsize=a.getOptionValueNonEmpty("--log-size");
            if(logsize==null && config!=null){
                logsize= Utils.getJsonValueString(config,"log-size");
            }
            //count of rotated fileds
            String logcount=a.getOptionValueNonEmpty("--log-count");
            if(logcount==null && config!=null){
                logcount= Utils.getJsonValueString(config,"log-count");
            }

            LogUtils.prepare(logfile,
                    Utils.parseInt(logsize, 0),
                    Utils.parseInt(logcount, 0)
            );
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        log.info("Starting //Big Data Vista Baby// Broker ...");
        BdvbBroker broker = BdvbFactory.createBroker(config);
        log.info("Server version : "+broker.getVersion().getVersion());
        broker.registerDefaultDrivers();
        broker.addService("net.vpc.app.bdvbbroker.devices.ulbotech.tracking.UlbotechTracking");
        broker.addPacketListener(new BdvbPacketPrintStreamLogger());
        broker.start();

        final Object o = new Object();
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
