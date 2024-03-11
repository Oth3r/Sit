package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utl {
    enum HandType {
        main,
        off
    }
    public static HashMap<String,String> getHandSettings() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        HashMap<String,String> settings = new HashMap<>();
        settings.put("hand.main.requirement",String.valueOf(config.mainReq));
        settings.put("hand.main.block",String.valueOf(config.mainBlock));
        settings.put("hand.main.food",String.valueOf(config.mainFood));
        settings.put("hand.main.usable",String.valueOf(config.mainUsable));
        settings.put("hand.main.whitelist",gson.toJson(config.mainWhitelist));
        settings.put("hand.main.blacklist",gson.toJson(config.mainBlacklist));
        settings.put("hand.off.requirement",String.valueOf(config.offReq));
        settings.put("hand.off.block",String.valueOf(config.offBlock));
        settings.put("hand.off.food",String.valueOf(config.offFood));
        settings.put("hand.off.usable",String.valueOf(config.offUsable));
        settings.put("hand.off.whitelist",gson.toJson(config.offWhitelist));
        settings.put("hand.off.blacklist",gson.toJson(config.offBlacklist));
        return settings;
    }
    public static config.HandRequirement getReq(ServerPlayerEntity player, HandType type) {
        return config.HandRequirement.get(Sit.playerSettings.get(player).get("hand."+type+".requirement"));
    }
    public static List<String> getList(ServerPlayerEntity player, HandType type, String setting) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(Sit.playerSettings.get(player).get("hand."+type+"."+setting),listType);
    }
    public static boolean getBool(ServerPlayerEntity player, HandType type, String setting) {
        return Boolean.parseBoolean(Sit.playerSettings.get(player).get("hand."+type+"."+setting));
    }
}
