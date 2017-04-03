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
package net.vpc.app.bdvbbroker.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.*;

/**
 * @author Taha BEN SALAH <taha.bensalah@gmail.com>
 * @creationdate 9/16/12 10:00 PM
 */
public class LogUtils {


    public static void prepare(String pattern,int maxSize,int count) throws IOException {
        Logger rootLogger = Logger.getLogger("");
        int MEGA = 1024 * 1024;
        if(Utils.trimToNull(pattern)==null){
            pattern="log/bdvb-%g.log";
        }
        if(maxSize<0){
            maxSize=20;
        }
        if(count<0){
            count=3;
        }
        if(pattern.contains("/")){
            new File(pattern.substring(0,pattern.lastIndexOf('/'))).mkdirs();
        }
        rootLogger.addHandler(new FileHandler(pattern, 5 * MEGA,3,true));
        rootLogger.setLevel(Level.FINE);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.FINE);
            handler.setFormatter(new LogFormatter());
//            handler.setFilter(new Filter() {
//                public boolean isLoggable(LogRecord record) {
//                    return record.getLoggerName().startsWith("net.vpc.");
//                }
//            });
        }
    }

    private static final class LogFormatter extends Formatter {

        private static final String LINE_SEPARATOR = System.getProperty("line.separator");

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            sb.append(new Date(record.getMillis()))
                    .append(" ")
                    .append(record.getLevel().getLocalizedName())
                    .append(" ")
                    .append(record.getSourceClassName())
                    .append(": ")
                    .append(formatMessage(record))
                    .append(LINE_SEPARATOR);

            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    // ignore
                }
            }

            return sb.toString();
        }
    }
}
