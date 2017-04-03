package net.vpc.app.bdvbbroker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.cmd.BdvbBrokerCmdServer;
import net.vpc.app.bdvbbroker.cmd.DefaultBdvbBrokerCmdServer;
import net.vpc.app.bdvbbroker.repository.*;
import net.vpc.app.bdvbbroker.util.Utils;
import net.vpc.app.bdvbbroker.util.Version;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 11/23/16.
 */
public class DefaultBdvbBroker implements BdvbBroker {
    private static final Logger log=Logger.getLogger(DefaultBdvbBroker.class.getName());
    private BdvbTcpTransportManager tcpManager = new BdvbTcpTransportManager();
    private Map<String, BdvbDeviceDriver> deviceBrokersByDriverType = new HashMap<String, BdvbDeviceDriver>();
    private Map<String, BdvbDeviceDriver> deviceBrokersByDriverId = new HashMap<String, BdvbDeviceDriver>();
    private List<BdvbPacketListener> packetListeners = new ArrayList<BdvbPacketListener>();
    private JsonObject bootConfig;
    private BdvbRepository repository;
    private BdvbBrokerCmdServer cmdServer;

    public DefaultBdvbBroker(JsonObject bootConfig) {
        this.bootConfig = bootConfig == null ? new JsonObject() : bootConfig;
    }

    @Override
    public Version getVersion() {
        return Version.INSTANCE;
    }

    @Override
    public void registerDefaultDrivers() {
        log.info("Registering Default Drivers...");
        ServiceLoader<BdvbDeviceDriver> drivers
                = ServiceLoader.load(BdvbDeviceDriver.class);
        for (BdvbDeviceDriver driver : drivers) {
            registerDriver(driver);
        }
    }

    public void registerDriver(BdvbDeviceDriver driver) {
        if (!deviceBrokersByDriverType.containsKey(driver.getClass().getName())) {
            log.info("Installing Driver "+driver.getClass().getName());
            deviceBrokersByDriverType.put(driver.getClass().getName(), driver);
            driver.install(this);
        }
    }

    public void register(Class<? extends BdvbDeviceDriver> driverClass) {
        try {
            if (!deviceBrokersByDriverType.containsKey(driverClass.getName())) {
                registerDriver(driverClass.newInstance());
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to instantiate " + driverClass, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to instantiate " + driverClass, e);
        }
    }

    @Override
    public void addService(Class<? extends BdvbDeviceDriver> cls) {
        addService(null, cls, null);
    }

    @Override
    public void addService(Class<? extends BdvbDeviceDriver> cls, JsonObject config) {
        addService(null, cls, config);
    }

    public void addService(String id, Class<? extends BdvbDeviceDriver> cls, JsonObject config) {
        if (id != null && deviceBrokersByDriverId.containsKey(id)) {
            throw new IllegalArgumentException("Driver Id Already Registered");
        }
        register(cls);
        BdvbDeviceDriver driver = deviceBrokersByDriverType.get(cls.getName());
        if (id == null) {
            id = cls.getName();
            if (deviceBrokersByDriverId.containsKey(id)) {
                throw new IllegalArgumentException("Driver Id Already Registered");
            }
        }
        log.info("Starting Driver "+id);
        driver.start(id, config);
        deviceBrokersByDriverId.put(id, driver);
    }

    @Override
    public void addDefaultServices() {
        log.info("Registering Default Services...");
        for (String s : deviceBrokersByDriverType.keySet()) {
            addService(s);
        }
    }

    public void addService(String id) {
        addService(id, null);
    }

    public void addService(String id, JsonObject config) {
        if (id == null) {
            throw new IllegalArgumentException("Missing Driver Id");
        }
        BdvbDeviceDriver driver = deviceBrokersByDriverType.get(id);
        if (driver == null) {
            throw new IllegalArgumentException("No Such Driver " + id);
        }
        if (!deviceBrokersByDriverId.containsKey(id)) {
            log.info("Starting Driver "+id);
            driver.start(id, config);
            deviceBrokersByDriverId.put(id, driver);
        }
    }

    public JsonObject getBootConfig() {
        return bootConfig;
    }

    public void start() {
        String repositoryId = Utils.getOrDefault(Utils.getJsonValueString(new JsonElement[]{getBootConfig()},"boot","repository"),"default");
        if ("mongodb".equals(repositoryId)) {
            this.repository = new BdvbRepositoryMongodb();
        } else if ("none".equals(repositoryId)) {
            this.repository = new BdvbRepositoryNone();
        } else if ("default".equals(repositoryId)) {
            this.repository = new BdvbRepositoryMongodb();
        } else {
            try {
                this.repository = (BdvbRepository) Class.forName(repositoryId.trim()).newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        log.info("Starting Repository "+repositoryId+" as "+repository.getClass().getName());
        this.repository.start(bootConfig);
        tcpManager.start(bootConfig, this);
        cmdServer=new DefaultBdvbBrokerCmdServer();
        cmdServer.start(bootConfig,this);
    }

    public void stop() {
        log.info("Stopping Broker");
        tcpManager.stop();
        if (repository != null) {
            repository.stop();
        }
    }

    public BdvbTcpTransportManager getTcpManager() {
        return tcpManager;
    }

    public void publish(BdvbPacket packet) {
        preparePacket(packet);
        repository.store(packet);
        for (BdvbPacketListener packetListener : packetListeners) {
            packetListener.onReceivePacket(packet);
        }
    }

    @Override
    public BdvbRepository getRepository() {
        return repository;
    }

    public void addPacketListener(BdvbPacketListener listener) {
        packetListeners.add(listener);
    }

    public void removePacketListener(BdvbPacketListener listener) {
        packetListeners.add(listener);
    }

    @Override
    public long updateUniformValue(String ownerUUID, String deviceUUID, Date from, Date to) {
        return getRepository().findAndUpdate(ownerUUID, deviceUUID, from, to, new BdvbPacketTransform() {
            @Override
            public BdvbPacket transform(BdvbPacket packet) {
                if(preparePacket(packet)){
                    return packet;
                }
                return null;
            }
        });
    }

    private boolean preparePacket(BdvbPacket packet){
        BdvbDeviceDriver bdvbDeviceDriver = deviceBrokersByDriverId.get(packet.getDeviceDriver());
        if(bdvbDeviceDriver!=null){
            JsonObject uniform = bdvbDeviceDriver.uniformPacket(packet.getRaw());
            packet.setValue(uniform);

            //this is a workaround
            Date timestamp = Utils.parseDateOrNull(uniform.get("timestamp").getAsString());
            if(timestamp!=null) {
                packet.setDeviceTime(timestamp);
            }

            updateAlarms(packet);
            return true;
        }
        return false;
    }

    private void updateAlarms(BdvbPacket packet){
        JsonObject value = packet.getValue();
        int speed = value.getAsJsonObject("gps").get("speed").getAsInt();
        if(speed>120){
            JsonObject alarms = value.get("alarms").getAsJsonObject();
            JsonElement jsonElement = alarms.get("Over Speed");
            if(jsonElement==null || jsonElement.getAsInt()==0){
                alarms.addProperty("Over Speed",1);
            }
        }
    }

}
