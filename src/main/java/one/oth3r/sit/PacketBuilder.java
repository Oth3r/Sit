package one.oth3r.sit;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketBuilder {
    public static final String SETTINGS = "settings_v1.0";
    private final String message;
    private PacketByteBuf packetByteBuf = PacketByteBufs.create();
    public PacketBuilder(PacketByteBuf buf) {
        // Read any data sent in the packet
        message = buf.toString(StandardCharsets.UTF_8);
        packetByteBuf = buf;
    }
    public PacketBuilder(String message) {
        this.message = message;
        packetByteBuf.writeBytes(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)).array());
    }
    public static Identifier getIdentifier() {
        // only 1 packet rn
        return new Identifier(Sit.MOD_ID, SETTINGS);
    }
    public void send() {
        ClientPlayNetworking.send(getIdentifier(), packetByteBuf);
    }
    public String getMessage() {
        return this.message;
    }
}
