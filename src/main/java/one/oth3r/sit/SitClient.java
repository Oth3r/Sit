package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class SitClient implements ClientModInitializer {
    public static boolean inGame = false;
    @Override
    public void onInitializeClient() {
        Sit.isClient = true;
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            inGame = true;
            // send a data packet whenever joining a server
            client.execute(SitClient::sendPackets);
        });
        // reset inGame
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inGame = false);
    }
    public static void sendPackets() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        new PacketBuilder(gson.toJson(Utl.PlayerSettings.getHandSettings())).send();
    }
}
