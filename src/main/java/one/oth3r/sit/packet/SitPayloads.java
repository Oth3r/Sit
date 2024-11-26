package one.oth3r.sit.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import one.oth3r.sit.utl.Data;

public class SitPayloads {
    /**
     * the packet that the client sends to the server
     * @param value the sitting settings for the client
     */
    public record SettingsPayload(String value) implements CustomPayload {

        public static final Id<SettingsPayload> ID = new Id<>(Identifier.of(Data.MOD_ID,"settings_v2.0"));

        public static final PacketCodec<RegistryByteBuf, SettingsPayload> CODEC = PacketCodecs.STRING.xmap(SettingsPayload::new, SettingsPayload::value).cast();

        @Override
        public Id<SettingsPayload> getId() {
            return ID;
        }
    }

    /**
     * the packet that the server sends to the client when responding to the settings payload
     */
    public record ResponsePayload(String value) implements CustomPayload {

        public static final String VERSION = "response_v1.0";

        public static final Id<ResponsePayload> ID = new Id<>(Identifier.of(Data.MOD_ID,VERSION));

        public static final PacketCodec<RegistryByteBuf, ResponsePayload> CODEC = PacketCodecs.STRING.xmap(ResponsePayload::new, ResponsePayload::value).cast();

        @Override
        public Id<ResponsePayload> getId() {
            return ID;
        }
    }
}
