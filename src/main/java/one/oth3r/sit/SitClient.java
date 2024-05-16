package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import one.oth3r.sit.packet.CustomPayloads;

public class SitClient implements ClientModInitializer {
    public static boolean inGame = false;
    @Override
    public void onInitializeClient() {
        Sit.isClient = true;
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            inGame = true;
            // send a data packet whenever joining a server
            sendSettingsPackets();
        });
        // reset inGame
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inGame = false);
    }

    /**
     * sends the settings packets to the server is the client is in game
     */
    public static void sendSettingsPackets() {
        if (inGame) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            ClientPlayNetworking.send(new CustomPayloads.SettingsPayload(gson.toJson(Utl.HandSettings.getHandSettings())));
        }
    }
}
