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
package net.vpc.app.bdvbbroker.mock;

import net.vpc.app.bdvbbroker.RichInputStream;
import net.vpc.app.bdvbbroker.RichOutputStream;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by vpc on 10/30/16.
 */
public abstract class AbstractTcpMock {
    public static class Config {
        private InetAddress serverAddress;
        private int port;
        private int repeatInterval = 5;
        private int repeatCount = 20;


        public InetAddress getServerAddress() {
            return serverAddress;
        }

        public Config setServerAddress(InetAddress serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public int getPort() {
            return port;
        }

        public Config setPort(int port) {
            this.port = port;
            return this;
        }

        public int getRepeatInterval() {
            return repeatInterval;
        }

        public Config setRepeatInterval(int repeatInterval) {
            this.repeatInterval = repeatInterval;
            return this;
        }

        public int getRepeatCount() {
            return repeatCount;
        }

        public Config setRepeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
            return this;
        }

    }

    private Config config;


    public AbstractTcpMock(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public void start() {
        new Thread(new Runnable() {
            public void run() {
                startAndWait();
            }
        }).start();
    }

    public void startAndWait() {
        try {
            Socket s = new Socket(getConfig().getServerAddress(), getConfig().getPort());
            RichInputStream is = new RichInputStream(s.getInputStream());
            RichOutputStream os = new RichOutputStream(s.getOutputStream());
            int packetsCount = getConfig().getRepeatCount();
            if (packetsCount > 0) {
                for (int i = 0; i < packetsCount; i++) {
                    Thread.sleep(getConfig().getRepeatInterval() * 1000);
                    send(os);
                }
            } else {
                while (true) {
                    Thread.sleep(getConfig().getRepeatInterval() * 1000);
                    send(os);
                }
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract void send(RichOutputStream os) throws IOException ;

}
