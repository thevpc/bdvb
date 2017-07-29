package net.vpc.app.bdvbbroker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.vpc.app.bdvbbroker.cmd.BdvbBrokerCmdServer;
import net.vpc.app.bdvbbroker.cmd.DefaultBdvbBrokerCmdServer;
import net.vpc.app.bdvbbroker.repository.BdvbPacketTransform;
import net.vpc.app.bdvbbroker.repository.BdvbRepository;
import net.vpc.app.bdvbbroker.repository.BdvbRepositoryMongodb;
import net.vpc.app.bdvbbroker.repository.BdvbRepositoryNone;
import net.vpc.app.bdvbbroker.util.Utils;
import net.vpc.app.bdvbbroker.util.Version;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 11/23/16.
 */
public class DefaultBdvbBroker implements BdvbBroker {
    private static final Logger log = Logger.getLogger(DefaultBdvbBroker.class.getName());
    private BdvbTcpTransportManager tcpManager = new BdvbTcpTransportManager();
    private Map<String, BdvbDeviceDriver> deviceDriverByDriverType = new HashMap<String, BdvbDeviceDriver>();
    private Map<String, String> idToType = new HashMap<String, String>();
    private Map<String, String> aliasToType = new HashMap<String, String>();
    private List<BdvbPacketListener> packetListeners = new ArrayList<BdvbPacketListener>();
    private List<BdvbPacketHandler> packetHandlers = new ArrayList<BdvbPacketHandler>();
    private JsonObject bootConfig;
    private BdvbRepository repository;
    private BdvbBrokerCmdServer cmdServer;

    public DefaultBdvbBroker(JsonObject bootConfig) {
        this.bootConfig = bootConfig == null ? new JsonObject() : bootConfig;
        addPacketHandler(new BdvbPacketHandler() {
            @Override
            public void onHandlePacket(BdvbPacket packet) {
                JsonObject value = packet.getValue();
                int speed = value.getAsJsonObject("gps").get("speed").getAsInt();
                if (speed > 120) {
                    JsonObject alarms = value.get("alarms").getAsJsonObject();
                    JsonElement jsonElement = alarms.get("Over Speed");
                    if (jsonElement == null || jsonElement.getAsInt() == 0) {
                        alarms.addProperty("Over Speed", 1);
                    }
                }
            }
        });
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
        if (!deviceDriverByDriverType.containsKey(driver.getDriverId())) {
            log.info("Installing Driver " + driver.getClass().getName());
            deviceDriverByDriverType.put(driver.getClass().getName(), driver);
            driver.install(this);
        }
    }

    public BdvbDeviceDriver register(Class<? extends BdvbDeviceDriver> driverClass) {
        try {
            BdvbDeviceDriver driver = driverClass.newInstance();
            registerDriver(driver);
            return driver;
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
        if (id != null && idToType.containsKey(id)) {
            throw new IllegalArgumentException("Driver Id Already Registered");
        }
        BdvbDeviceDriver driver=register(cls);
        if (id == null) {
            id = cls.getName();
            if (idToType.containsKey(id)) {
                throw new IllegalArgumentException("Driver Id Already Registered");
            }
        }
        log.info("Starting Driver " + id);
        driver.start(id, config);
        idToType.put(id, cls.getName());
    }

    @Override
    public void addDefaultServices() {
        log.info("Registering Default Services...");
        for (String s : deviceDriverByDriverType.keySet()) {
            addService(s);
        }
    }

    public void addService(String driverType) {
        addService(null, driverType, null);
    }

    public void addService(String id, String driverType, JsonObject config) {
        BdvbDeviceDriver driver = resolveDriverOrError(driverType);
        if (id == null || id.trim().length() == 0) {
            id = driver.getDefaultServiceId();
        }
        if (!idToType.containsKey(id)) {
            log.info("Starting Driver " + driver);
            driver.start(id, config);
        }
    }

    public JsonObject getBootConfig() {
        return bootConfig;
    }

    public void start() {
        String repositoryId = Utils.getOrDefault(Utils.getJsonValueString(new JsonElement[]{getBootConfig()}, "boot", "repository"), "default");
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
        log.info("Starting Repository " + repositoryId + " as " + repository.getClass().getName());
        this.repository.start(bootConfig);
        tcpManager.start(bootConfig, this);
        cmdServer = new DefaultBdvbBrokerCmdServer();
        cmdServer.start(bootConfig, this);
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
        handlePacket(packet);
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


    public void addPacketHandler(BdvbPacketHandler handler) {
        packetHandlers.add(handler);
    }

    public void removePacketHandler(BdvbPacketHandler handler) {
        packetHandlers.add(handler);
    }


    public BdvbDeviceDriver resolveDriverOrError(String driverId) {
        BdvbDeviceDriver d = resolveDriver(driverId);
        if (d != null) {
            return d;
        }
        throw new IllegalArgumentException("No Such Driver " + driverId);
    }

    public BdvbDeviceDriver resolveDriver(String driverId) {
        BdvbDeviceDriver driver = deviceDriverByDriverType.get(driverId);
        if (driver == null) {
            String newId = aliasToType.get(driverId);
            if (newId != null) {
                driver = deviceDriverByDriverType.get(newId);
            }
        }
        return driver;
    }

    public void addDriverAlias(String alias, String driverType) {
        if (Utils.isEmpty(alias)) {
            throw new IllegalArgumentException("Invalid Empty Alias");
        }
        if (Utils.isEmpty(driverType)) {
            throw new IllegalArgumentException("Invalid Empty Driver Id");
        }
        if (deviceDriverByDriverType.containsKey(alias)) {
            throw new IllegalArgumentException("Alias already registered " + alias);
        }
        if (!deviceDriverByDriverType.containsKey(driverType)) {
            throw new IllegalArgumentException("Invalid Type " + driverType);
        }
        aliasToType.put(alias, driverType);
    }

    public boolean removeDriverAlias(String alias, String driverId) {
        return aliasToType.remove(alias) != null;
    }

    @Override
    public long updateUniformValue(String ownerUUID, String deviceUUID, Date from, Date to) {
        return getRepository().findAndUpdate(ownerUUID, deviceUUID, from, to, new BdvbPacketTransform() {
            @Override
            public BdvbPacket transform(BdvbPacket packet) {
                if (handlePacket(packet)) {
                    return packet;
                }
                return null;
            }
        });
    }

    private boolean handlePacket(BdvbPacket packet) {
        BdvbDeviceDriver bdvbDeviceDriver = deviceDriverByDriverType.get(packet.getDeviceDriver());
        if (bdvbDeviceDriver != null) {
            JsonObject uniform = bdvbDeviceDriver.uniformPacket(packet.getRaw());
            packet.setValue(uniform);

            //this is a workaround
            Date timestamp = Utils.parseDateOrNull(uniform.get("timestamp").getAsString());
            if (timestamp != null) {
                packet.setDeviceTime(timestamp);
            }
            for (BdvbPacketHandler packetHandler : packetHandlers) {
                packetHandler.onHandlePacket(packet);
            }
            return true;
        }
        return false;
    }

}
