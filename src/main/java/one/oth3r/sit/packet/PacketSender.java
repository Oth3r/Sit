package one.oth3r.sit.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import one.oth3r.sit.utl.Data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketSender {
    private final PacketByteBuf data = PacketByteBufs.create();
    private final PacketType type;

    public PacketSender(PacketType type, String data) {
        this.type = type;
        this.data.writeBytes(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)));
    }

    public void sendToPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player,getIdentifier(type),data);
    }

    public void sendToServer() {
        ClientPlayNetworking.send(getIdentifier(type),data);
    }

    public static Identifier getIdentifier(PacketType packetType) {
        return new Identifier(Data.MOD_ID, packetType.getId());
    }

    public static String getPacketData(PacketByteBuf buf) {
        return buf.toString(StandardCharsets.UTF_8);
    }
}
