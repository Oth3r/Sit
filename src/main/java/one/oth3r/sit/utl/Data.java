package one.oth3r.sit.utl;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Data {
    public static final String MOD_ID = "sit-oth3r";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String CONFIG_DIR = FabricLoader.getInstance().getConfigDir().toFile()+"/sit!/";

    public static final String ENTITY_NAME = "-sit!-entity-";

    // init on server load
    private static MinecraftServer server;

    public static MinecraftServer getServer() {
        return server;
    }

    public static void setServer(MinecraftServer server) {
        Data.server = server;
    }

    // client booleans
    private static boolean client = false;
    private static boolean inGame = false;
    private static boolean singleplayer = false;
    private static boolean supportedServer = false;

    public static boolean isClient() {
        return client;
    }

    public static void setClient(boolean client) {
        Data.client = client;
    }

    public static boolean isInGame() {
        return inGame;
    }

    public static void setInGame(boolean inGame) {
        Data.inGame = inGame;
    }

    public static boolean isSingleplayer() {
        return singleplayer;
    }

    public static void setSingleplayer(boolean singleplayer) {
        Data.singleplayer = singleplayer;
    }

    public static boolean isSupportedServer() {
        return supportedServer;
    }

    public static void setSupportedServer(boolean supportedServer) {
        Data.supportedServer = supportedServer;
    }
}
