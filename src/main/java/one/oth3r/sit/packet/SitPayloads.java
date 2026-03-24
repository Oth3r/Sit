package one.oth3r.sit.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import one.oth3r.sit.utl.Data;

public class SitPayloads {
    /**
     * the packet that the client sends to the server
     * @param value the sitting settings for the client
     */
    public record SettingsPayload(String value) implements CustomPacketPayload {

        public static final Type<SettingsPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Data.MOD_ID,"settings_v2.0"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SettingsPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(SettingsPayload::new, SettingsPayload::value).cast();

        @Override
        public Type<SettingsPayload> type() {
            return ID;
        }
    }

    /**
     * the packet that the server sends to the client when responding to the settings payload
     */
    public record ResponsePayload(String value) implements CustomPacketPayload {

        public static final String VERSION = "response_v1.0";

        public static final Type<ResponsePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Data.MOD_ID,VERSION));

        public static final StreamCodec<RegistryFriendlyByteBuf, ResponsePayload> CODEC = ByteBufCodecs.STRING_UTF8.map(ResponsePayload::new, ResponsePayload::value).cast();

        @Override
        public Type<ResponsePayload> type() {
            return ID;
        }
    }
}
