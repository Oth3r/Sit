package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.CustomPayload;

public class SitClient implements ClientModInitializer {
    public static boolean inGame = false;
    @Override
    public void onInitializeClient() {
        Sit.isClient = true;
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            inGame = true;
            // send a data packet whenever joining a server
            ClientPlayNetworking.send(sendPackets());
        });
        // reset inGame
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inGame = false);
    }
    public static CustomPayload sendPackets() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return new Sit.SettingsPayload(gson.toJson(Utl.HandSettings.getHandSettings()));
    }
}
