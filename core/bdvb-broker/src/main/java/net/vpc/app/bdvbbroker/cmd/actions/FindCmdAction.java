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
package net.vpc.app.bdvbbroker.cmd.actions;

import net.vpc.app.bdvbbroker.BdvbBroker;
import net.vpc.app.bdvbbroker.BdvbPacket;
import net.vpc.app.bdvbbroker.cmd.Args;
import net.vpc.app.bdvbbroker.cmd.CmdAction;
import net.vpc.app.bdvbbroker.util.Utils;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by vpc on 12/1/16.
 */
public class FindCmdAction implements CmdAction {
    @Override
    public void invoke(Args args, BdvbBroker bdvbBroker, Map<String, Object> context, PrintStream out) {
        if (args.length() > 0) {
            String what = args.getNonEmpty(0);
            if ("packets".equals(what)) {
                if("count".equals(args.getNonEmpty(1))){
                    long count=bdvbBroker.getRepository().findPacketsCount(
                            args.getOptionValueNonEmpty("--owner"),
                            args.getOptionValueNonEmpty("--device"),
                            Utils.parseDateOrNull(args.getOptionValueNonEmpty("--after")),
                            Utils.parseDateOrNull(args.getOptionValueNonEmpty("--before"))
                    );
                    out.println(count + " packets.");
                }else {
                    List<BdvbPacket> list = bdvbBroker.getRepository().findPackets(
                            args.getOptionValueNonEmpty("--owner"),
                            args.getOptionValueNonEmpty("--device"),
                            Utils.parseDateOrNull(args.getOptionValueNonEmpty("--after")),
                            Utils.parseDateOrNull(args.getOptionValueNonEmpty("--before")),
                            Utils.parseInt(args.getOptionValueNonEmpty("--count"),0),
                            null
                    );
                    for (BdvbPacket p : list) {
                        out.println(Utils.GSON.toJson(p));
                    }
                    out.println(list.size() + " packets.");
                }
            } else if ("devices".equals(what)) {
                Set<String> list = bdvbBroker.getRepository().findDevices(
                        args.getOptionValueNonEmpty("--owner"),
                        Utils.parseDateOrNull(args.getOptionValueNonEmpty("--after")),
                        Utils.parseDateOrNull(args.getOptionValueNonEmpty("--before")),
                        Utils.parseInt(args.getOptionValueNonEmpty("--count"),0)
                );
                for (String p : list) {
                    out.println(p);
                }
                out.println(list.size() + " devices.");
            } else {
                throw new RuntimeException("syntax incorrect : " +
                        "\n\tfind packets [--owner <value>] [--device <value>] [--after <value>] [--before <value>] [--count <value>]" +
                        "\n\tfind packets count [--owner <value>] [--device <value>] [--after <value>] [--before <value>]" +
                        "\n\tfind devices [--owner <value>] [--after <value>] [--before <value>] [--count <value>]");
            }
        } else {
            throw new RuntimeException("syntax incorrect : " +
                    "\n\tfind packets [--owner <value>] [--device <value>] [--after <value>] [--before <value>] [--count <value>]" +
                    "\n\tfind packets count [--owner <value>] [--device <value>] [--after <value>] [--before <value>]" +
                    "\n\tfind devices [--owner <value>] [--after <value>] [--before <value>] [--count <value>]");
        }
    }
}
