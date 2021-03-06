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
import net.vpc.app.bdvbbroker.cmd.Args;
import net.vpc.app.bdvbbroker.cmd.CmdAction;
import net.vpc.app.bdvbbroker.util.Utils;

import java.io.PrintStream;
import java.util.Date;
import java.util.Map;

/**
 * Created by vpc on 12/1/16.
 */
public class RebuildCmdAction implements CmdAction {
    @Override
    public void invoke(Args args, BdvbBroker bdvbBroker, Map<String, Object> context, PrintStream out) {
        long updates = bdvbBroker.updateUniformValue(
                args.getOptionValueNonEmpty("--owner"),
                args.getOptionValueNonEmpty("--device"),
                Utils.parseDateOrNull(args.getOptionValueNonEmpty("--after")),
                Utils.parseDateOrNull(args.getOptionValueNonEmpty("--before"))
        );
        out.println(updates + " elements.");
    }
}
