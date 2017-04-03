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

import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by vpc on 11/20/16.
 */
public class Utils {
    public static final String UNIVERSAL_DATETIME_FORMAT_STRING=("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String UNIVERSAL_DATETIME_SIMPLE_FORMAT_STRING=("yyyy-MM-dd HH:mm:ss");
    public static final String UNIVERSAL_DATE_FORMAT_STRING=("yyyy-MM-dd");
    public static final SimpleDateFormat UNIVERSAL_DATETIME_FORMAT=new SimpleDateFormat(UNIVERSAL_DATETIME_FORMAT_STRING);
    public static final SimpleDateFormat UNIVERSAL_DATE_FORMAT=new SimpleDateFormat(UNIVERSAL_DATE_FORMAT_STRING);
    public static final SimpleDateFormat UNIVERSAL_DATETIME_SIMPLE_FORMAT=new SimpleDateFormat(UNIVERSAL_DATETIME_SIMPLE_FORMAT_STRING);
    public static final Gson GSON=new GsonBuilder().setDateFormat(UNIVERSAL_DATETIME_FORMAT_STRING).create();

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

    public static JsonElement parseJsonElement(File file) throws IOException {
        return parseJsonElement(loadFileAsString(file));
    }

    public static String loadFileAsString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static JsonElement parseJsonElement(String json){
        return
                GSON.fromJson(json,JsonElement.class);
    }

    public static JsonObject parseJsonObject(String json){
        return
                GSON.fromJson(json,JsonObject.class);
    }

    public static <T> T getOrDefault(T o,T defaultValue){
        if(o==null){
            return defaultValue;
        }
        return o;
    }

    public static Integer getJsonValueInt(JsonElement[] e,String ... expr){
        String s = getJsonValueString(e, expr);
        if(s!=null && s.trim().length()>0){
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e1) {
                //ignore
            }
        }
        return null;
    }

    public static Integer getJsonValueInt(JsonElement e,Integer defautlValue,String ... expr){
        String s = getJsonValueString(e, expr);
        if(s!=null && s.trim().length()>0){
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e1) {
                //ignore
            }
        }
        return defautlValue;
    }

    public static Boolean getJsonValueBoolean(JsonElement e,Boolean defautlValue,String ... expr){
        String s = getJsonValueString(e, expr);
        if(s!=null && s.trim().length()>0){
            try {
                return Boolean.parseBoolean(s);
            } catch (NumberFormatException e1) {
                //ignore
            }
        }
        return defautlValue;
    }

    public static String getJsonValueString(JsonElement[] e,String ... expr){
        Object v = getJsonValue(e, expr);
        if(v instanceof JsonPrimitive){
            return ((JsonPrimitive)v).getAsString();
        }
        if(v!=null){
            return String.valueOf(v);
        }
        return null;
    }
    public static String getJsonValueString(JsonElement e,String ... expr){
        Object v = getJsonValue(e, expr);
        if(v instanceof JsonPrimitive){
            return ((JsonPrimitive)v).getAsString();
        }
        if(v!=null){
            return String.valueOf(v);
        }
        return null;
    }

    public static Object getJsonValue(JsonElement[] e,String ... expr){
        for (int i = 0; i < e.length; i++) {
            Object r=getJsonValue(e[i],expr);
            if(r!=null){
                return r;
            }
        }
        return null;
    }

    public static JsonElement getJsonValue(JsonElement e,String ... expr){
        JsonElement e0=e;
        for (int i = 0; i < expr.length; i++) {
            if(e0==null){
                e0=null;
                break;
            }else  if(e0 instanceof JsonNull){
                e0=null;
                break;
            }else  if(e0 instanceof JsonPrimitive){
                e0=null;
                break;
            }else  if(e0 instanceof JsonObject){
                e0=((JsonObject)e0).get(expr[i]);
            }else if(e0 instanceof JsonArray){
                int i1 = 0;
                try {
                    i1 = Integer.parseInt(expr[i]);
                } catch (NumberFormatException e1) {
                    return null;
                }
                e0=((JsonArray)e0).get(i1);
            }else{
                e0=null;
                break;
            }
        }
        return e0;
    }

    public static Date datePlusMillis(Date d,int amount){
        return datePlus(d,Calendar.MILLISECOND,amount);
    }

    public static Date datePlusSeconds(Date d,int amount){
        return datePlus(d,Calendar.SECOND,amount);
    }

    public static Date datePlus(Date d,int field,int amount){
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(field, amount);
        return c.getTime();
    }

    public static String formatHex(int value, int len){
        if(value<0){
            throw new RuntimeException("Unsupported");
        }
        String s1 = Integer.toHexString(value);
        if(s1.length()<len){
            char[] c=new char[len];
            int extra = len - s1.length();
            for (int i = 0; i < extra; i++) {
                c[i]='0';
            }
            System.arraycopy(s1.toCharArray(),0,c,extra,s1.length());
            return new String(c);
        }
        return s1;
    }

    public static String formatDecimal(int value, int len){
        if(value<0){
            throw new RuntimeException("Unsupported");
        }
        String s1 = Integer.toString(value);
        if(s1.length()<len){
            char[] c=new char[len];
            int extra = len - s1.length();
            for (int i = 0; i < extra; i++) {
                c[i]='0';
            }
            System.arraycopy(s1.toCharArray(),0,c,extra,s1.length());
            return new String(c);
        }
        return s1;
    }

    public static JsonElement simplify(JsonElement e){
        return e;
    }

    public static String trimToNull(String s){
        if(s!=null){
            s=s.trim();
            if(s.length()==0){
                s=null;
            }
        }
        return s;
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

}
