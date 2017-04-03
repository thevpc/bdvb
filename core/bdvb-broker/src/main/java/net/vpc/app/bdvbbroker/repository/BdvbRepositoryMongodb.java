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
package net.vpc.app.bdvbbroker.repository;

import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.vpc.app.bdvbbroker.BdvbPacket;
import net.vpc.app.bdvbbroker.DefaultBdvbPacket;
import net.vpc.app.bdvbbroker.util.Utils;
import org.bson.Document;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 11/17/16.
 */
public class BdvbRepositoryMongodb implements BdvbRepository {
    private static final Logger log = Logger.getLogger(BdvbRepositoryMongodb.class.getName());
    private MongoClient mongo;
    private MongoDatabase db;
    private MongoCollection<Document> packets;

    public void start(JsonObject config) {
        /**** Connect to MongoDB ****/
        // Since 2.10.0, uses MongoClient
        String serverAddress = "localhost";
        int serverPort = 27017;
        String databaseName = "bdvb";
        log.info("Connecting to mongodb://" + serverAddress + ":" + serverPort + "/" + databaseName + "");
        mongo = new MongoClient(serverAddress, serverPort);
        db = mongo.getDatabase(databaseName);
        packets = db.getCollection("packets");
    }

    public void resetPackets(String ownerUUID, String deviceUUID) {
        Document allFilter = new Document();
        if (Utils.trimToNull(deviceUUID) != null) {
            allFilter.put("deviceUUID", deviceUUID);
        }
        packets.deleteMany(allFilter);
    }

    public List<BdvbPacket> findByDeviceUUID(String deviceUUID) {
        Document allFilter = new Document();
        if (Utils.trimToNull(deviceUUID) != null) {
            allFilter.put("deviceUUID", deviceUUID);
        }
        List<BdvbPacket> list = new ArrayList<>();
        for (Document document : packets.find(allFilter)) {
            DefaultBdvbPacket p = new DefaultBdvbPacket();

        }
        return list;
    }

    public Set<String> findDevices(String ownerUUID, Date from, Date to, int maxCount) {
        Document allFilter = new Document();

        createBsonServerTimeFilter(from, to, allFilter);

        DistinctIterable<String> deviceUUIDs = packets.distinct("deviceUUID", allFilter, String.class);
        HashSet<String> set = new HashSet<>();
        if (maxCount > 0) {
            int size = 0;
            for (String deviceUUID : deviceUUIDs) {
                set.add(deviceUUID);
                size++;
                if (size >= maxCount) {
                    break;
                }
            }
        } else {
            for (String deviceUUID : deviceUUIDs) {
                set.add(deviceUUID);
            }
        }
        return set;
    }

    public long findPacketsCount(String ownerUUID, String deviceUUID, Date from, Date to) {
        Document allFilter = new Document();
        if (Utils.trimToNull(deviceUUID) != null) {
            allFilter.put("deviceUUID", deviceUUID);
        }
        createBsonServerTimeFilter(from, to, allFilter);
        List<BdvbPacket> list = new ArrayList<>();
        return packets.count(allFilter);
    }

    public List<BdvbPacket> findPackets(String ownerUUID, String deviceUUID, Date from, Date to, int maxCount, BdvbPacketTransform transform) {
        Document allFilter = new Document();
        if (Utils.trimToNull(deviceUUID) != null) {
            allFilter.put("deviceUUID", deviceUUID);
        }
        createBsonServerTimeFilter(from, to, allFilter);
        List<BdvbPacket> list = new ArrayList<>();

        Document sortCriteria = new Document();
        sortCriteria.put("deviceTime", -1);
        FindIterable<Document> documents = packets.find(allFilter).sort(sortCriteria);
        if (maxCount != 0) {
            documents = documents.limit(Math.abs(maxCount));
        }
        if (maxCount != 0) {
            int size = 0;
            if (transform != null) {
                int maxCount0 = maxCount < 0 ? -maxCount : maxCount;
                if (maxCount > 0) {
                    for (Document document : documents) {
                        BdvbPacket defaultBdvbPacket = Utils.GSON.fromJson(Utils.GSON.toJson(document), DefaultBdvbPacket.class);
                        defaultBdvbPacket = transform.transform(defaultBdvbPacket);
                        if (defaultBdvbPacket != null) {
                            list.add(defaultBdvbPacket);
                            size++;
                            if (size >= maxCount) {
                                break;
                            }
                        }
                    }
                } else {
                    LinkedList<BdvbPacket> llist = new LinkedList<>();
                    for (Document document : documents) {
                        BdvbPacket defaultBdvbPacket = Utils.GSON.fromJson(Utils.GSON.toJson(document), DefaultBdvbPacket.class);
                        defaultBdvbPacket = transform.transform(defaultBdvbPacket);
                        if (defaultBdvbPacket != null) {
                            llist.add(defaultBdvbPacket);
                            size++;
                            if (size > maxCount0) {
                                llist.removeFirst();
                            }
                        }
                    }
                    list = llist;
                }
            } else {
                for (Document document : documents) {
                    DefaultBdvbPacket defaultBdvbPacket = Utils.GSON.fromJson(Utils.GSON.toJson(document), DefaultBdvbPacket.class);
                    list.add(defaultBdvbPacket);
                    size++;
                    if (size >= maxCount) {
                        break;
                    }
                }
            }
        } else {
            if (transform != null) {
                for (Document document : documents) {
                    BdvbPacket defaultBdvbPacket = Utils.GSON.fromJson(Utils.GSON.toJson(document), DefaultBdvbPacket.class);
                    defaultBdvbPacket = transform.transform(defaultBdvbPacket);
                    if (defaultBdvbPacket != null) {
                        list.add(defaultBdvbPacket);
                    }
                }
            } else {
                for (Document document : documents) {
                    DefaultBdvbPacket defaultBdvbPacket = Utils.GSON.fromJson(Utils.GSON.toJson(document), DefaultBdvbPacket.class);
                    list.add(defaultBdvbPacket);
                }
            }
        }
        Collections.reverse(list);
        return list;
    }

    private void createBsonServerTimeFilter(Date from, Date to, Document allFilter) {
        if (from != null || to != null) {
            Document dt = new Document();
            if (from != null) {
                dt.put("$gte", from);
            }
            if (to != null) {
                dt.put("$lte", to);
            }
            allFilter.put("serverTime", dt);
        }
    }

    public void stop() {
        mongo.close();
    }


    public void store(BdvbPacket packet) {
        Document document = new Document();
        document.put("deviceTime", packet.getDeviceTime());
        document.put("serverTime", packet.getServerTime());
        document.put("deviceDriver", packet.getDeviceDriver());
        document.put("deviceType", packet.getDeviceType());
        document.put("deviceUUID", packet.getDeviceUUID());
        String s = Utils.GSON.toJson(packet.getRaw());
        document.put("raw", Document.parse(s));
        s = Utils.GSON.toJson(packet.getValue());
        document.put("value", Document.parse(s));
        packets.insertOne(document);
    }

    public long findAndUpdate(String ownerUUID, String deviceUUID, Date from, Date to, BdvbPacketTransform transform) {
        Document allFilter = new Document();
        if (deviceUUID != null) {
            allFilter.put("deviceUUID", deviceUUID);
        }
        createBsonServerTimeFilter(from, to, allFilter);
        long size = 0;
        for (Document document : packets.find(allFilter)) {
            BdvbPacket defaultBdvbPacket = Utils.GSON.fromJson(Utils.GSON.toJson(document), DefaultBdvbPacket.class);
            defaultBdvbPacket = transform.transform(defaultBdvbPacket);
            Document filter = new Document();
            filter.put("_id", document.get("_id"));
            if (defaultBdvbPacket != null) {
                size++;
                document.put("deviceTime", defaultBdvbPacket.getDeviceTime());
                document.put("serverTime", defaultBdvbPacket.getServerTime());
                document.put("deviceDriver", defaultBdvbPacket.getDeviceDriver());
                document.put("deviceType", defaultBdvbPacket.getDeviceType());
                document.put("deviceUUID", defaultBdvbPacket.getDeviceUUID());
                String s = Utils.GSON.toJson(defaultBdvbPacket.getRaw());
                document.put("raw", Document.parse(s));
                s = Utils.GSON.toJson(defaultBdvbPacket.getValue());
                document.put("value", Document.parse(s));
                packets.replaceOne(filter, document);
            }
        }
        return size;
    }

    public long findAndDelete(String ownerUUID, String deviceUUID, Date from, Date to) {
        Document allFilter = new Document();
        if (deviceUUID != null) {
            allFilter.put("deviceUUID", deviceUUID);
        }
        createBsonServerTimeFilter(from, to, allFilter);
        long size = 0;
        for (Document document : packets.find(allFilter)) {
            Document filter = new Document();
            filter.put("_id", document.get("_id"));
            packets.findOneAndDelete(filter);
            size++;
        }
        return size;
    }

}
