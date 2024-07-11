package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import one.oth3r.sit.file.Data;
import one.oth3r.sit.packet.SitPayloads;
import one.oth3r.sit.utl.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;

public class Sit implements ModInitializer {
	public static final String MOD_ID = "oth3r-sit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static String CONFIG_DIR = FabricLoader.getInstance().getConfigDir().toFile()+"/sit!/";

	public static HashMap<ServerPlayerEntity, HashMap<String,String>> playerSettings = new HashMap<>();
	public static final String ENTITY_NAME = "-sit!-entity-";
	public static MinecraftServer server;
	public static CommandManager commandManager;

	public static boolean client = false;
	public static boolean singleplayer = false;

	@Override
	public void onInitialize() {
		Data.loadFiles(true);
		Events.registerCommon();

		//PACKETS
		PayloadTypeRegistry.playC2S().register(SitPayloads.SettingsPayload.ID, SitPayloads.SettingsPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SitPayloads.SettingsPayload.ID,((payload, context) -> server.execute(() -> {
            Type hashMapToken = new TypeToken<HashMap<String, Object>>() {}.getType();
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            playerSettings.put(context.player(),gson.fromJson(payload.value(),hashMapToken));
        })));
	}
}