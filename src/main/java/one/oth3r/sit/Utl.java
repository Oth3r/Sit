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
    public enum HandType {
        main,
        off
    }
    public static class PlayerSettings {
        public enum Setting {
            requirement,
            block,
            food,
            usable,
            whitelist,
            blacklist
        }
        /**
         * Gets a HashMap of all player configurable settings.
         * @return a map with player hand settings.
         */
        public static HashMap<String,String> getHandSettings() {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            HashMap<String,String> settings = new HashMap<>();
            // main hand
            settings.put("hand.main."+Setting.requirement,String.valueOf(config.mainReq));
            settings.put("hand.main."+Setting.block,String.valueOf(config.mainBlock));
            settings.put("hand.main."+Setting.food,String.valueOf(config.mainFood));
            settings.put("hand.main."+Setting.usable,String.valueOf(config.mainUsable));
            settings.put("hand.main."+Setting.whitelist,gson.toJson(config.mainWhitelist));
            settings.put("hand.main."+Setting.blacklist,gson.toJson(config.mainBlacklist));
            // copy but offhand
            settings.put("hand.off."+Setting.requirement,String.valueOf(config.offReq));
            settings.put("hand.off."+Setting.block,String.valueOf(config.offBlock));
            settings.put("hand.off."+Setting.food,String.valueOf(config.offFood));
            settings.put("hand.off."+Setting.usable,String.valueOf(config.offUsable));
            settings.put("hand.off."+Setting.whitelist,gson.toJson(config.offWhitelist));
            settings.put("hand.off."+Setting.blacklist,gson.toJson(config.offBlacklist));
            return settings;
        }
        // returns specific items from the player config
        public static config.HandRequirement getRequirement(ServerPlayerEntity player, HandType type) {
            return config.HandRequirement.get(Sit.playerSettings.get(player).get("hand."+type+".requirement"));
        }
        public static List<String> getList(ServerPlayerEntity player, HandType type, Setting setting) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            return new Gson().fromJson(Sit.playerSettings.get(player).get("hand."+type+"."+setting),listType);
        }
        public static boolean getToggle(ServerPlayerEntity player, HandType type, Setting setting) {
            return Boolean.parseBoolean(Sit.playerSettings.get(player).get("hand."+type+"."+setting));
        }
    }
    public static class Assets {
        public static final String CUSTOM_BLOCKS = "\"minecraft:campfire|0.255|1|lit=false\"";
        public static final String REQUIREMENT_OPTIONS = String.format("%s, %s, %s",
                config.HandRequirement.empty,config.HandRequirement.restrictive,config.HandRequirement.none);
        public static final String LIST = "\"minecraft:torch\"";
    }
}
