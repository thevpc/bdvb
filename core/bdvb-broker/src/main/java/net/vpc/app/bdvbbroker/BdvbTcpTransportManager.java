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
import net.vpc.app.bdvbbroker.util.InputStreamPipe;
import net.vpc.app.bdvbbroker.util.Utils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 10/30/16.
 */
public class BdvbTcpTransportManager implements BdvbTransportManager {
    private static final Logger log = Logger.getLogger(BdvbTcpTransportManager.class.getName());
    private Map<InetAddressAndPort, TCPChannel> channels = new HashMap<InetAddressAndPort, TCPChannel>();
    private boolean started = false;
    private ExecutorService executorService;
    private BdvbBroker broker;
    private boolean logEvents;
    private int defautlBacklog;
    private int burstBufferSize;
    private String logEventsPath;

    public void start(JsonObject bootConfig, BdvbBroker broker) {
        if (!started) {
            started = true;
            log.info("Starting Tcp Transport Manager");
        }
        logEvents = (Utils.getJsonValueBoolean(bootConfig, true, "transport","tcp", "log-events"));
        burstBufferSize = (Utils.getJsonValueInt(bootConfig, 1024, "transport","tcp", "burst-buffer-size"));
        defautlBacklog = (Utils.getJsonValueInt(bootConfig, 10, "transport","tcp", "default-backlog"));
        logEventsPath = (Utils.getJsonValueString(bootConfig, "transport","tcp", "log-events-path"));
        if (Utils.trimToNull(logEventsPath) == null) {
            logEventsPath = System.getProperty("user.home") + "/workspace/bdvb/events";
        }
        if (logEvents) {
            new File(logEventsPath).mkdirs();
        }
        String executorString = Utils.trimToNull(Utils.getJsonValueString(bootConfig, "transport","tcp", "executor"));
        if (executorString == null) {
            executorString = "cached";
        }
        if ("cached".equals(executorString)) {
            executorService = Executors.newCachedThreadPool();
        } else if ("workStealing".equals(executorString)) {
            Integer nbr = (Utils.getJsonValueInt(bootConfig, 0, "transport","tcp", "executor", "count"));
            if (nbr == null) {
                nbr = 0;
            }
            if (nbr < 0) {
                nbr = 0;
            }
            if (nbr <= 0) {
                executorService = Executors.newWorkStealingPool();
            } else {
                executorService = Executors.newWorkStealingPool(nbr);
            }
        } else if ("fixed".equals(executorString)) {
            Integer nbr = (Utils.getJsonValueInt(bootConfig, 0, "transport","tcp", "executor", "count"));
            if (nbr == null) {
                nbr = 0;
            }
            if (nbr < 0) {
                nbr = 10;
            }
            executorService = Executors.newFixedThreadPool(nbr);
        } else {
            throw new IllegalArgumentException("Invalid executor type");
        }
        this.broker = broker;
        for (TCPChannel tcpChannel : channels.values()) {
            ensureStarted(tcpChannel);
        }
    }

    public void startChannelByOwner(String channelOwner) {
        boolean found = false;
        for (TCPChannel tcpChannel : channels.values()) {
            if (Objects.equals(tcpChannel.owner, channelOwner)) {
                ensureStarted(tcpChannel);
                found = true;
            }
        }
    }

    private void ensureStarted(TCPChannel tcpChannel) {
        if (!tcpChannel.isStarted()) {
            if (!tcpChannel.isStarting()) {
                tcpChannel.start();
            }
        }
    }

    private void ensureStopped(TCPChannel tcpChannel) {
        if (tcpChannel.isStarted()) {
            tcpChannel.stop();
        } else {
            while (tcpChannel.isStarting()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (tcpChannel.isStarted()) {
                tcpChannel.stop();
            }
        }
    }


    public void addListener(BdvbTcpConnectionListener listener) {
        BdvbTcpInfo info = listener.getInfo();
        InetAddressAndPort p = new InetAddressAndPort(info.getInetAddress(), info.getPort());
        TCPChannel tcpChannel = channels.get(p);
        if (tcpChannel == null) {
            int backlog = info.getBacklog();
            if(backlog<=0){
                backlog=defautlBacklog;
            }
            if(backlog<=0){
                backlog=10;
            }
            tcpChannel = new TCPChannel(info.getOwner(), info.getPort(), backlog, info.getInetAddress());
            channels.put(p, tcpChannel);
        }
        tcpChannel.add(listener);
        if (started) {
            ensureStarted(tcpChannel);
        }
    }

    public void stop() {
        if (started) {
            started = false;
            log.info("Stopping Tcp Transport Manager");
        }
        for (TCPChannel tcpChannel : channels.values()) {
            ensureStopped(tcpChannel);
        }
    }

    public static class InetAddressAndPort {
        private InetAddress address;
        private int port;

        public InetAddressAndPort(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InetAddressAndPort that = (InetAddressAndPort) o;

            if (port != that.port) return false;
            return address != null ? address.equals(that.address) : that.address == null;

        }

        @Override
        public int hashCode() {
            int result = address != null ? address.hashCode() : 0;
            result = 31 * result + port;
            return result;
        }
    }

    public class TCPChannel {
        private InetAddress address;
        private int port;
        private int backlog;
        private boolean starting;
        private String owner;
        private ServerSocket serverSocket;
        private List<BdvbTcpConnectionBurstListener> birstListeners = new ArrayList<BdvbTcpConnectionBurstListener>();
        private BdvbTcpConnectionListener nonBirstListener;

        public TCPChannel(String owner, int port, int backlog, InetAddress address) {
            this.owner = owner;
            this.address = address;
            this.port = port;
            this.backlog = backlog;
        }

        public void add(BdvbTcpConnectionListener listener) {
            if (listener instanceof BdvbTcpConnectionBurstListener) {
                if (nonBirstListener != null) {
                    throw new IllegalArgumentException("Already bound");
                }
                birstListeners.add((BdvbTcpConnectionBurstListener) listener);
            } else {
                if (birstListeners.size() > 0) {
                    throw new IllegalArgumentException("Must be a burst Listener");
                }
                if (nonBirstListener != null) {
                    throw new IllegalArgumentException("Already bound");
                }
                nonBirstListener = listener;
            }
        }

        public boolean isStarted() {
            return serverSocket != null;
        }

        public boolean isStarting() {
            return starting;
        }

        public void stop() {
            if (serverSocket != null) {
                log.info("Stopping TCP Channel @ " + this.address + ":" + this.port + " (backlog=" + this.backlog + ")");
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverSocket = null;
            }
        }

        public void start() {
            if (isStarted()) {
                throw new IllegalArgumentException("Already Started");
            }
            while (starting) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new IllegalArgumentException("Unable to wait for starting");
                }
            }
            if (!isStarted()) {
                try {
                    log.info("Starting TCP Channel @ " + (this.address == null ? "<ANY_ADDR>" : address.toString()) + ":" + this.port + " (backlog=" + this.backlog + ") by " + owner);

                    starting = true;
                    try {
                        serverSocket = new ServerSocket(port, backlog, address);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Unable to start", e);
                    }
                    executorService.execute(new Runnable() {
                        public void run() {
                            acceptLoop();
                        }
                    });
                } finally {
                    starting = false;
                }
            }
        }

        private void acceptLoop() {
            while (true) {
                Socket socket = null;
                try {
                    if (serverSocket != null) {
                        socket = serverSocket.accept();
                    }
                } catch (IOException e) {
                    //serverSocket closed
                }
                if (socket != null) {
                    final Socket socket0 = socket;
                    executorService.execute(new Runnable() {
                        public void run() {
                            processSocket(socket0);
                        }
                    });
                } else {
                    return;
                }
            }
        }

        private void processSocket(Socket socket) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                if (birstListeners.size() > 0) {
                    InputStream pb = new BufferedInputStream(in, burstBufferSize);
                    pb.mark(burstBufferSize);
                    for (BdvbTcpConnectionBurstListener birstListener : birstListeners) {
                        boolean accept = birstListener.accept(pb);
                        pb.reset();
                        if (accept) {
                            pb = warpStream(socket, pb);
                            birstListener.onOpenConnection(new BdvbTcpConnection(
                                    birstListener.getDriverId(),
                                    socket, new RichInputStream(pb), new RichOutputStream(out), broker));
                            return;
                        }
                    }
                    onOpenConnectionFallback(new BdvbTcpConnection(null, socket, new RichInputStream(pb), new RichOutputStream(out), broker));
                    return;
                }
                if (nonBirstListener != null) {
                    in = warpStream(socket, in);
                    BdvbTcpConnection connection = new BdvbTcpConnection(nonBirstListener.getDriverId(), socket, new RichInputStream(in), new RichOutputStream(out), broker);
                    nonBirstListener.onOpenConnection(connection);
                } else {
                    in = warpStream(socket, in);
                    BdvbTcpConnection connection = new BdvbTcpConnection(null, socket, new RichInputStream(in), new RichOutputStream(out), broker);
                    onOpenConnectionFallback(connection);
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error",e);
            }
        }

        private InputStream warpStream(Socket socket, InputStream in) throws IOException {
            if(logEvents) {
                String path = logEventsPath;
                File folder = new File(path);
                folder.mkdirs();
                File out = new File(folder, socket.getInetAddress().toString());
                InputStreamPipe pipe = new InputStreamPipe(
                        in, out
                );
                in=pipe;
            }
            return in;
        }

        private void onOpenConnectionFallback(BdvbTcpConnection cnx) {

        }
    }
}
