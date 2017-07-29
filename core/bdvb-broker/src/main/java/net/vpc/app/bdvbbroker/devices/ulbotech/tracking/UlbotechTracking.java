package net.vpc.app.bdvbbroker.devices.ulbotech.tracking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.SimpleBdvbDeviceDriver;
import net.vpc.app.bdvbbroker.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 10/30/16.
 */
public class UlbotechTracking extends SimpleBdvbDeviceDriver {
    public UlbotechTracking() {
        super(new UlbotechTrackingTcpDecoder(),
                "{\"services\":{\"net.vpc.app.bdvbbroker.devices.ulbotech.tracking.UlbotechTracking\":{\"protocol\":\"tcp\",\"port\":38096}}}"
        );
    }

    private static Map<String, String> propMapper = new HashMap<>();

    static {
        propMapper.put("Distance traveled since codes cleared", "distance-traveled");
        propMapper.put("Engine Coolant Temperature", "engine-coolant-temperature");
        propMapper.put("Engine RPM", "rpm");
        propMapper.put("Fuel", "fuel");
        propMapper.put("Fuel level", "fuel-level");
        propMapper.put("Vehicle Speed", "speed");
    }

    public JsonObject uniformPacket(JsonObject raw) {
        JsonObject u = new JsonObject();
        u.addProperty("timestamp", raw.get("timestamp").getAsString());
//        raw = raw.getAsJsonObject("raw");
        JsonObject gps = new JsonObject();
        if(raw.getAsJsonObject("GPS")!=null) {
            gps.addProperty("latitude", raw.getAsJsonObject("GPS").get("latitude").getAsDouble());
            gps.addProperty("longitude", raw.getAsJsonObject("GPS").get("longitude").getAsDouble());
            gps.addProperty("hdop", raw.getAsJsonObject("GPS").get("hdop").getAsDouble());
            gps.addProperty("dimension", raw.getAsJsonObject("GPS").get("dimension").getAsInt());
            gps.addProperty("course", raw.getAsJsonObject("GPS").get("course").getAsString());
            Integer speed = Utils.getJsonValueInt(raw, 0, "GPS", "speed");
            gps.addProperty("speed", speed == null ? 0 : speed);
        }else{
            gps.addProperty("latitude", 0);
            gps.addProperty("longitude", 0);
            gps.addProperty("hdop", 0);
            gps.addProperty("dimension", 0);
            gps.addProperty("course", 0);
            gps.addProperty("speed", 0 );
        }
        u.add("gps", gps);

        JsonObject device = new JsonObject();
        u.add("device", device);
        device.addProperty("device-temperature", Utils.getJsonValueString(raw, "ADC", "Device Temperature"));
        device.addProperty("inner-battery", Utils.getJsonValueString(raw, "ADC", "Inner Battery"));

        JsonObject vehicle = new JsonObject();
        u.add("vehicle", vehicle);
        vehicle.addProperty("engines-seconds", Utils.getJsonValueString(raw, "EGT"));
        vehicle.addProperty("battery", Utils.getJsonValueString(raw, "ADC", "Car Battery"));
        vehicle.addProperty("fuel", Utils.getJsonValueString(raw, "FUL", "value"));
        vehicle.addProperty("mileage", Utils.getJsonValueString(raw, "MGR"));
        JsonElement obd = Utils.getJsonValue(raw, "OBD");
        JsonArray obdArr = obd == null ? new JsonArray() : obd.getAsJsonArray();
        for (JsonElement element : obdArr) {
            JsonObject o = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> elementEntry : o.entrySet()) {
                String key = elementEntry.getKey();
                if (!(key.equals("TYP") || key.equals("Service") || key.equals("PID"))) {
                    String key2 = propMapper.get(key);
                    if(key2!=null){
                        key=key2;
                    }
                    vehicle.add(key, elementEntry.getValue());
                }
            }
        }

        JsonObject alarms = new JsonObject();
        u.add("alarms", alarms);
        JsonElement stt = Utils.getJsonValue(raw, "STT");
        JsonObject r = stt == null ? new JsonObject() : stt.getAsJsonObject();
        for (Map.Entry<String, JsonElement> e : r.entrySet()) {
            if (e.getKey().startsWith("Alarm ")) {
                alarms.add(e.getKey().substring("Alarm ".length()), e.getValue());
            }
        }
        JsonElement hdb = Utils.getJsonValue(raw, "HDB");
        r = hdb == null ? new JsonObject() : hdb.getAsJsonObject();
        for (Map.Entry<String, JsonElement> e : r.entrySet()) {
            alarms.add(e.getKey(), e.getValue());
        }
        JsonObject settings = new JsonObject();
        u.add("settings", settings);
        JsonObject alarmsettings = new JsonObject();
        settings.add("alarms", alarmsettings);

        r = stt == null ? new JsonObject() : stt.getAsJsonObject();
        for (Map.Entry<String, JsonElement> e : r.entrySet()) {
            if (e.getKey().startsWith("Status ")) {
                alarmsettings.add(e.getKey().substring("Status ".length()), e.getValue());
            }
        }
        return u;
    }
}
