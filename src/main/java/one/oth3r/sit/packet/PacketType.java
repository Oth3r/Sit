package one.oth3r.sit.packet;

public enum PacketType {
    RESPONSE("response_v1.0"),
    SETTINGS("settings_v2.0");

    final String id;

    PacketType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
