package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;

public class Sit implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("sit");
	public static final String MOD_ID = "oth3r-sit";
	public static HashMap<ServerPlayerEntity, HashMap<String,String>> playerSettings = new HashMap<>();
	public static final String ENTITY_NAME = "-sit!-entity-";
	public static MinecraftServer server;
	public static CommandManager commandManager;
	public static boolean isClient = false;

	@Override
	public void onInitialize() {
		//todo future:
		// make it so it updates the sitting height and pos based on the block so if it changed while offline it still works (or if stair changes shape)
		// inner stair offset & custom support for that ig
		config.load();
		Events.register();
		//PACKETS
		ServerPlayNetworking.registerGlobalReceiver(PacketBuilder.getIdentifier(),
				(server, player, handler, buf, responseSender) -> {
			// copy to not throw errors
			PacketBuilder packet = new PacketBuilder(buf.copy());
			server.execute(() -> {
						Type hashMapToken = new TypeToken<HashMap<String, Object>>() {}.getType();
						Gson gson = new GsonBuilder().disableHtmlEscaping().create();
						playerSettings.put(player,gson.fromJson(packet.getMessage(),hashMapToken));
					});
				});
	}
}