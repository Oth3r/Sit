package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import one.oth3r.sit.file.Config;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utl {
    public static class HandSettings {
        public static HashMap<String,String> getHandSettings() {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            HashMap<String,String> settings = new HashMap<>();
            settings.put("hand.main.requirement",String.valueOf(Config.mainReq));
            settings.put("hand.main.block",String.valueOf(Config.mainBlock));
            settings.put("hand.main.food",String.valueOf(Config.mainFood));
            settings.put("hand.main.usable",String.valueOf(Config.mainUsable));
            settings.put("hand.main.whitelist",gson.toJson(Config.mainWhitelist));
            settings.put("hand.main.blacklist",gson.toJson(Config.mainBlacklist));
            settings.put("hand.off.requirement",String.valueOf(Config.offReq));
            settings.put("hand.off.block",String.valueOf(Config.offBlock));
            settings.put("hand.off.food",String.valueOf(Config.offFood));
            settings.put("hand.off.usable",String.valueOf(Config.offUsable));
            settings.put("hand.off.whitelist",gson.toJson(Config.offWhitelist));
            settings.put("hand.off.blacklist",gson.toJson(Config.offBlacklist));
            return settings;
        }

        public static Config.HandRequirement getReq(ServerPlayerEntity player, HandType type) {
            return Config.HandRequirement.get(Sit.playerSettings.get(player).get("hand."+type+".requirement"));
        }

        public static List<String> getList(ServerPlayerEntity player, HandType type, String setting) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            return new Gson().fromJson(Sit.playerSettings.get(player).get("hand."+type+"."+setting),listType);
        }

        public static boolean getBool(ServerPlayerEntity player, HandType type, String setting) {
            return Boolean.parseBoolean(Sit.playerSettings.get(player).get("hand."+type+"."+setting));
        }

        public enum HandType {
            main,
            off
        }
    }
    public static class Num {
        public static boolean isInt(String string) {
            try {
                Integer.parseInt(string);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }
        public static boolean isFloat(String string) {
            try {
                Float.parseFloat(string);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }
    }
}
