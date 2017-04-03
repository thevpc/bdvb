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
package net.vpc.app.bdvbbroker.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vpc on 11/29/16.
 */
public class ClientUtils {
    public static final String UNIVERSAL_DATETIME_FORMAT_STRING=("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String UNIVERSAL_DATETIME_SIMPLE_FORMAT_STRING=("yyyy-MM-dd HH:mm:ss");
    public static final String UNIVERSAL_DATE_FORMAT_STRING=("yyyy-MM-dd");
    public static final SimpleDateFormat UNIVERSAL_DATETIME_FORMAT=new SimpleDateFormat(UNIVERSAL_DATETIME_FORMAT_STRING);
    public static final SimpleDateFormat UNIVERSAL_DATE_FORMAT=new SimpleDateFormat(UNIVERSAL_DATE_FORMAT_STRING);
    public static final SimpleDateFormat UNIVERSAL_DATETIME_SIMPLE_FORMAT=new SimpleDateFormat(UNIVERSAL_DATETIME_SIMPLE_FORMAT_STRING);
    public static final Gson GSON=new GsonBuilder().setDateFormat(ClientUtils.UNIVERSAL_DATETIME_FORMAT_STRING).create();
    public static final Date MIN_DATE=safeParseDateTime("1970-01-01 00:00:00.000");
    public static final Date MAX_DATE=safeParseDateTime("3000-01-01 00:00:00.000");
    public static final Date safeParseDateTime(String s){
        try {

            return UNIVERSAL_DATETIME_FORMAT.parse(s);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement simplify(JsonElement e){
        return e;
    }

    public static int parseInt(String val,int defaultValue){
        if(val!=null) {
            try {
                return Integer.parseInt(val);
            } catch (Exception e) {

            }
        }
        return defaultValue;
    }

    public static Date parseDateOrNull(String dte){
        if(dte!=null){
            try {
                return UNIVERSAL_DATETIME_FORMAT.parse(dte);
            } catch (ParseException e) {
                try {
                    return UNIVERSAL_DATETIME_SIMPLE_FORMAT.parse(dte);
                } catch (ParseException e1) {
                    try {
                        return UNIVERSAL_DATE_FORMAT.parse(dte);
                    } catch (ParseException e2) {
                        //
                    }
                }
            }
        }
        return null;
    }

}
