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

/**
 * Created by vpc on 11/20/16.
 */
public class ByteArrayList {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    byte[] arr;
    int size;

    public ByteArrayList() {
        this(10);
    }

    public ByteArrayList(int initialSize) {
        this.arr = new byte[initialSize];
    }

    public byte get(int pos) {
        if(pos>=size){
            throw new ArrayIndexOutOfBoundsException(pos);
        }
        return arr[pos];
    }

    public void add(byte val) {
        int pos=size;
        size++;
        ensureSize(size);
        arr[pos] = val;
    }

    private void ensureSize(int size) {
        if (size < arr.length) {
            return;
        }
        int newSize = size + size >> 1;
        if (newSize - size < 0)
            newSize = size;
        if (newSize - MAX_ARRAY_SIZE > 0)
            newSize = MAX_ARRAY_SIZE;

        byte[] arr2 = new byte[newSize];
        System.arraycopy(arr, 0, arr2, 0, arr.length);
        arr = arr2;
    }

    public byte[] toArray() {
        byte[] arr2 = new byte[size];
        System.arraycopy(arr, 0, arr2, 0, arr.length);
        return arr2;
    }
}
