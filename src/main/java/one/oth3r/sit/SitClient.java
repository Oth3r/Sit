package one.oth3r.sit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import one.oth3r.sit.file.Data;
import one.oth3r.sit.packet.SitPayloads;
import one.oth3r.sit.utl.Utl;

public class SitClient implements ClientModInitializer {
    private static boolean IN_GAME = false;

    @Override
    public void onInitializeClient() {
        Sit.client = true;

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            IN_GAME = true;
            if (client.isInSingleplayer()) Sit.singleplayer = true;
            // send a data packet whenever joining a server
            sendSettingsPackets();
        });

        // reset cashed things
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            IN_GAME = false;
            if (Sit.singleplayer) {
                // flip the single player switch
                Sit.singleplayer = false;
            }
        });
    }

    /**
     * sends the settings packets to the server
     */
    public static void sendSettingsPackets() {
        if (IN_GAME) ClientPlayNetworking.send(new SitPayloads.SettingsPayload(Utl.getGson().toJson(Data.getHandConfig())));
    }
}
