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
package net.vpc.app.bdvbbroker.cmd;

import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.*;
import net.vpc.app.bdvbbroker.cmd.actions.FindCmdAction;
import net.vpc.app.bdvbbroker.cmd.actions.RebuildCmdAction;
import net.vpc.app.bdvbbroker.cmd.actions.ResetCmdAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by vpc on 11/30/16.
 */
public class DefaultBdvbBrokerCmdServer implements BdvbBrokerCmdServer {
    private BdvbBroker bdvbBroker;
    private Map<String, CmdAction> actions = new HashMap<>();

    @Override
    public void start(JsonObject config, BdvbBroker bdvbBroker) {
        this.bdvbBroker = bdvbBroker;
        bdvbBroker.getTcpManager().addListener(new BdvbTcpConnectionListener() {
            @Override
            public String getDriverId() {
                return "CmdServer";
            }

            @Override
            public BdvbTcpInfo getInfo() {
                try {
                    return new BdvbTcpInfo(
                            getDriverId(),
                            8866,
                            10,
                            null// InetAddress.getLocalHost()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onOpenConnection(BdvbTcpConnection connection) {
                try {
                    Map<String, Object> context = new HashMap<>();
                    try {
                        while (true) {
                            BufferedReader r = new BufferedReader(new InputStreamReader(connection.in));
                            PrintStream o = new PrintStream(connection.out);
                            o.print(">");
                            String line = null;
                            line = r.readLine();
                            if (line == null) {
                                break;
                            }
                            line = line.trim();
                            if (line.length() > 0) {
                                try {
                                    String[] args = translateCommandline(line);
                                    if ("quit".equals(args[0])) {
                                        return;
                                    }
                                    if ("help".equals(args[0])) {
                                        o.println("Available commands are :");
                                        o.println("quit");
                                        o.println("help");
                                        for (String s : actions.keySet()) {
                                            o.println(s);
                                        }
                                    }else {
                                        CmdAction a = actions.get(args[0]);
                                        if (a == null) {
                                            o.println("Command Not Found");
                                        } else {
                                            String[] args2 = new String[args.length-1];
                                            System.arraycopy(args, 1, args2, 0, args2.length);
                                            a.invoke(new Args(args2), bdvbBroker, context, o);
                                        }
                                    }

                                } catch (Exception e) {
                                    o.println(e.toString());
                                }
                            }
                        }
                    }finally{
                        connection.socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        actions.put("reset", new ResetCmdAction());
        actions.put("find", new FindCmdAction());
        actions.put("rebuild", new RebuildCmdAction());
        bdvbBroker.getTcpManager().startChannelByOwner("CmdServer");
    }

    /**
     * code from org.apache.tools.ant.types.Commandline
     * copyrights goes to Apache Ant Authors (Licensed to the Apache Software Foundation (ASF))
     * Crack a command line.
     *
     * @param toProcess the command line to process.
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.length() == 0) {
            //no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<String>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote:
                    if ("\'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    if ("\'".equals(nextTok)) {
                        state = inQuote;
                    } else if ("\"".equals(nextTok)) {
                        state = inDoubleQuote;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() != 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() != 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new RuntimeException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public void stop() {
        //
    }

}
