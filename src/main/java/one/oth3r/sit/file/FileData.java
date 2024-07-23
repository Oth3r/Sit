package one.oth3r.sit.file;

import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

public class FileData {
    /**
     * Sit! config file
     */
    private static ServerConfig serverConfig = new ServerConfig();

    public static ServerConfig getServerConfig() {
        return new ServerConfig(serverConfig);
    }

    public static void setServerConfig(ServerConfig newServerConfig) {
        serverConfig = new ServerConfig(newServerConfig);
    }

    /**
     * The default sitting config for all new players
     */
    private static SittingConfig sittingConfig = new SittingConfig();

    public static SittingConfig getSittingConfig() {
        return new SittingConfig(sittingConfig);
    }

    public static void setSittingConfig(SittingConfig newSittingConfig) {
        sittingConfig = new SittingConfig(newSittingConfig);
    }

    /**
     * the sitting config stored per player on the server
     */
    private static final HashMap<ServerPlayerEntity, SittingConfig> playerSettings = new HashMap<>();

    public static void clearPlayerSettings() {
        playerSettings.clear();
    }

    public static void setPlayerSetting(ServerPlayerEntity player, SittingConfig config) {
        playerSettings.put(player, config);
    }

    public static void removePlayerSetting(ServerPlayerEntity player) {
        playerSettings.remove(player);
    }

    public static SittingConfig getPlayerSetting(ServerPlayerEntity player) {
        return playerSettings.getOrDefault(player, sittingConfig);
    }

    /**
     * a list of every Sit! entity in the server, bound to the player
     */
    private static final HashMap<ServerPlayerEntity, DisplayEntity.TextDisplayEntity> sitEntities = new HashMap<>();

    public static void addSitEntity(ServerPlayerEntity player, DisplayEntity.TextDisplayEntity entity) {
        sitEntities.put(player, entity);
    }

    public static void removeSitEntity(DisplayEntity.TextDisplayEntity entity) {
        sitEntities.values().remove(entity);
    }

    public static DisplayEntity.TextDisplayEntity getSitEntity(ServerPlayerEntity player) {
        return sitEntities.get(player);
    }

    public static HashMap<ServerPlayerEntity, DisplayEntity.TextDisplayEntity> getSitEntities() {
        return new HashMap<>(sitEntities);
    }

    /**
     * a list of players who just joined, to check if they are mounted to a Sit! entity
     */
    private static final HashMap<ServerPlayerEntity, Integer> checkPlayers = new HashMap<>();

    public static void setCheckPlayer(ServerPlayerEntity player, Integer time) {
        checkPlayers.put(player, time);
    }

    public static void removeCheckPlayer(ServerPlayerEntity player) {
        checkPlayers.remove(player);
    }

    public static HashMap<ServerPlayerEntity, Integer> getCheckPlayers() {
        return new HashMap<>(checkPlayers);
    }

    /**
     * loads all config files to memory
     * @param tryLegacy try to load the legacy file, usually only used on server startup
     */
    public static void loadFiles(boolean tryLegacy) {
        ServerConfig.load(tryLegacy);
        SittingConfig.load();
    }
}
