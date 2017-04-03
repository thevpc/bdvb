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

/**
 * Created by vpc on 12/7/16.
 */
public class Args {
    private String[] args;

    public Args(String[] args) {
        this.args = args;
    }

    public String getOptionValueNonEmpty(String ...option){
        for (int i = 0; i < option.length; i++) {
            String s=getOptionValueNonEmpty(option[i]);
            if(s!=null){
                return s;
            }
        }
        return null;
    }

    public String getOptionValueNonEmpty(String option){
        int index = findOption(option);
        if(index>=0){
            return getNonEmpty(index+1);
        }
        return null;
    }

    public String getOptionValue(String option){
        int index = findOption(option);
        if(index>=0){
            return get(index+1);
        }
        return null;
    }

    public String getNonEmpty(int i){
        String s = get(i);
        if(s!=null){
            if(s.length()==0){
                return null;
            }
        }
        return s;
    }

    public String get(int i){
        if(i>=0 && i<args.length){
            return args[i];
        }
        return null;
    }

    public boolean containOption(String name){
        return findOption(name)>=0;
    }

    public int findOption(String name){
        if(name.startsWith("-") || name.startsWith("--")){
            for (int i = 0; i < args.length; i++) {
                if(args[i].equals(name)){
                    return i;
                }
            }
        }else{
            throw new IllegalArgumentException("Not an option "+name);
        }
        return -1;
    }

    public int length(){
        return args.length;
    }

    public boolean empty(){
        return args.length==0;
    }

}
