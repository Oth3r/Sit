package one.oth3r.sit.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import one.oth3r.sit.Sit;

public class CustomPayloads {
    public record SettingsPayload(String value) implements CustomPayload {

        public static final Id<SettingsPayload> ID = new Id<>(Identifier.of(Sit.MOD_ID,"settings_v1.1"));

        public static final PacketCodec<RegistryByteBuf, SettingsPayload> CODEC = PacketCodecs.STRING.xmap(SettingsPayload::new, SettingsPayload::value).cast();

        @Override
        public Id<SettingsPayload> getId() {
            return ID;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
