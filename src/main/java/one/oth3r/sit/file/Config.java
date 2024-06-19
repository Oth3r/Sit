package one.oth3r.sit.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import one.oth3r.sit.LangReader;
import one.oth3r.sit.Sit;
import one.oth3r.sit.SitClient;
import one.oth3r.sit.Utl.*;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Config {
    public static String lang = defaults.lang;
    public static boolean keepActive = defaults.keepActive;
    public static boolean sitWhileSeated = defaults.sitWhileSeated;
    public static boolean stairsOn = defaults.stairsOn;
    public static boolean slabsOn = defaults.slabsOn;
    public static boolean carpetsOn = defaults.carpetsOn;
    public static boolean fullBlocksOn = defaults.fullBlocksOn;
    public static boolean customOn = defaults.customOn;
    public static List<String> customBlocks = defaults.customBlocks;
    public enum HandRequirement {
        empty,
        restrictive,
        none;
        public static HandRequirement get(String s) {
            try {
                return HandRequirement.valueOf(s);

            } catch (IllegalArgumentException e) {
                return empty;
            }
        }
    }
    public static HandRequirement mainReq = defaults.mainReq;
    public static boolean mainBlock = defaults.mainBlock;
    public static boolean mainFood = defaults.mainFood;
    public static boolean mainUsable = defaults.mainUsable;
    public static List<String> mainWhitelist = defaults.mainWhitelist;
    public static List<String> mainBlacklist = defaults.mainBlacklist;
    public static HandRequirement offReq = defaults.offReq;
    public static boolean offBlock = defaults.offBlock;
    public static boolean offFood = defaults.offFood;
    public static boolean offUsable = defaults.offUsable;
    public static List<String> offWhitelist = defaults.offWhitelist;
    public static List<String> offBlacklist = defaults.offBlacklist;
    public static File configFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile()+"/Sit!.properties");
    }
    public static void load() {
        if (!configFile().exists() || !configFile().canRead()) {
            save();
            load();
            return;
        }
        try (FileInputStream fileStream = new FileInputStream(configFile())) {
            Properties properties = new Properties();
            properties.load(fileStream);
            String ver = (String) properties.computeIfAbsent("version", a -> String.valueOf(defaults.version));

            // if the old version system (v1.0) remove "v"
            if (ver.contains("v")) ver = ver.substring(1);

            loadVersion(properties,Double.parseDouble(ver));
            LangReader.loadLanguageFile();

            save();
        } catch (Exception e) {
            //read fail
            e.printStackTrace();
            save();
        }
    }
    public static ArrayList<String> validateCustomBlocks(ArrayList<String> fix) {
        ArrayList<String> out = new ArrayList<>();
        for (String entry : fix) {
            String[] split = entry.split("\\|");
            // skip if not the right size
            if (split.length < 3 || split.length > 4) continue;
            // keep going if that block exists
//            if (Registries.BLOCK.stream().anyMatch(match -> Registries.BLOCK.getId(match).toString().equals(split[0]))) {}
            // if the other entries aren't correct, skip
            if (!Num.isFloat(split[1]) || !Num.isInt(split[2])) continue;
            // add if everything is a okay
            out.add(entry);
        }
        return out;
    }
    public static void loadVersion(Properties properties, double version) {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            lang = (String) properties.computeIfAbsent("lang", a -> defaults.lang);
            //CONFIG
            keepActive = Boolean.parseBoolean((String) properties.computeIfAbsent("keep-active", a -> String.valueOf(defaults.keepActive)));
            sitWhileSeated = Boolean.parseBoolean((String) properties.computeIfAbsent("sit-while-seated", a -> String.valueOf(defaults.sitWhileSeated)));
            stairsOn = Boolean.parseBoolean((String) properties.computeIfAbsent("stairs", a -> String.valueOf(defaults.stairsOn)));
            slabsOn = Boolean.parseBoolean((String) properties.computeIfAbsent("slabs", a -> String.valueOf(defaults.slabsOn)));
            carpetsOn = Boolean.parseBoolean((String) properties.computeIfAbsent("carpets", a -> String.valueOf(defaults.carpetsOn)));
            fullBlocksOn = Boolean.parseBoolean((String) properties.computeIfAbsent("full-blocks", a -> String.valueOf(defaults.fullBlocksOn)));
            customOn = Boolean.parseBoolean((String) properties.computeIfAbsent("custom", a -> String.valueOf(defaults.customOn)));
            try {
                customBlocks = validateCustomBlocks(new Gson().fromJson((String)
                        properties.computeIfAbsent("custom-blocks", a -> gson.toJson(defaults.customBlocks)), listType));
            } catch (JsonSyntaxException ignore) {}
            mainReq = HandRequirement.get((String) properties.computeIfAbsent("hand.main.requirement", a -> String.valueOf(defaults.mainReq)));
            mainBlock = Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.block", a -> String.valueOf(defaults.mainBlock)));
            mainFood = Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.food", a -> String.valueOf(defaults.mainFood)));
            mainUsable = Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.usable", a -> String.valueOf(defaults.mainUsable)));
            try {
                mainWhitelist = new Gson().fromJson((String)
                        properties.computeIfAbsent("hand.main.whitelist", a -> gson.toJson(defaults.mainWhitelist)), listType);
            } catch (JsonSyntaxException ignore) {}
            try {
                mainBlacklist = new Gson().fromJson((String)
                        properties.computeIfAbsent("hand.main.blacklist", a -> gson.toJson(defaults.mainBlacklist)), listType);
            } catch (JsonSyntaxException ignore) {}
            offReq = HandRequirement.get((String) properties.computeIfAbsent("hand.off.requirement", a -> String.valueOf(defaults.offReq)));
            offBlock = Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.block", a -> String.valueOf(defaults.offBlock)));
            offFood = Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.food", a -> String.valueOf(defaults.offFood)));
            offUsable = Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.usable", a -> String.valueOf(defaults.offUsable)));
            offWhitelist = new Gson().fromJson((String)
                    properties.computeIfAbsent("hand.off.whitelist", a -> gson.toJson(defaults.offWhitelist)), listType);
            offBlacklist = new Gson().fromJson((String)
                    properties.computeIfAbsent("hand.off.blacklist", a -> gson.toJson(defaults.offBlacklist)), listType);
            if (version == 1.0) {
                mainReq = HandRequirement.get((String) properties.computeIfAbsent("main-hand-requirement", a -> String.valueOf(defaults.mainReq)));
                mainBlock = Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-block", a -> String.valueOf(defaults.mainBlock)));
                mainFood = Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-food", a -> String.valueOf(defaults.mainFood)));
                mainUsable = Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-usable", a -> String.valueOf(defaults.mainUsable)));
                try {
                    mainWhitelist = new Gson().fromJson((String)
                            properties.computeIfAbsent("main-hand-whitelist", a -> gson.toJson(defaults.mainWhitelist)), listType);
                } catch (JsonSyntaxException ignore) {}
                try {
                    mainBlacklist = new Gson().fromJson((String)
                            properties.computeIfAbsent("main-hand-blacklist", a -> gson.toJson(defaults.mainBlacklist)), listType);
                } catch (JsonSyntaxException ignore) {}
                offReq = HandRequirement.get((String) properties.computeIfAbsent("off-hand-requirement", a -> String.valueOf(defaults.offReq)));
                offBlock = Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-block", a -> String.valueOf(defaults.offBlock)));
                offFood = Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-food", a -> String.valueOf(defaults.offFood)));
                offUsable = Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-usable", a -> String.valueOf(defaults.offUsable)));
                try {
                    offWhitelist = new Gson().fromJson((String)
                            properties.computeIfAbsent("off-hand-whitelist", a -> gson.toJson(defaults.offWhitelist)), listType);
                } catch (JsonSyntaxException ignore) {}
                try {
                    offBlacklist = new Gson().fromJson((String)
                            properties.computeIfAbsent("off-hand-blacklist", a -> gson.toJson(defaults.offBlacklist)), listType);
                } catch (JsonSyntaxException ignore) {}
            }
        } catch (Exception e) {
            Sit.LOGGER.info("ERROR LOADING CONFIG - PLEASE REPORT WITH THE ERROR LOG");
            e.printStackTrace();
        }
    }
    public static String lang(String key, Object... args) {
        return Utl.lang("config."+key, args).getString();
    }
    public static void save() {
        try (var file = Files.newBufferedWriter(configFile().toPath(), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            file.write("# Sit! Config\n");
            file.write("\nversion="+defaults.version);
            file.write("\n# all available languages: en_us, ru_ru, es_es");
            file.write("\nlang=" + lang);
            file.write("\n\n# "+lang("general.keep_active.description"));
            file.write("\nkeep-active=" + keepActive);
            file.write("\n# "+lang("general.sit_while_seated.description"));
            file.write("\nsit-while-seated=" + sitWhileSeated);
            file.write("\n# "+lang("general.sittable.description"));
            file.write("\nstairs=" + stairsOn);
            file.write("\nslabs=" + slabsOn);
            file.write("\ncarpets=" + carpetsOn);
            file.write("\nfull-blocks=" + fullBlocksOn);
            file.write("\ncustom=" + customOn);
            file.write("\n# "+ Utl.lang("config."+
                            "general.sittable_blocks.description")
                    .append("\n# ").append(lang("example",Utl.Assets.CUSTOM_BLOCKS))
                    .append("\n# ").append(lang("general.sittable_blocks.description.2"))
                    .append("\n# ").append(lang("general.sittable_blocks.description.3"))
                    .append("\n# ").append(lang("general.sittable_blocks.description.4"))
                    .append("\n# ").append(lang("general.sittable_blocks.description.5"))
                    .append("\n# ").append(lang("general.sittable_blocks.description.6")).getString());
            file.write("\ncustom-blocks="+gson.toJson(customBlocks));
            file.write("\n\n# "+lang("hand"));
            file.write("\n# "+ Utl.lang("config."+
                            "hand.requirement.description")
                    .append("\n# ").append(lang("hand.requirement.description.2",HandRequirement.empty))
                    .append("\n# ").append(lang("hand.requirement.description.3",HandRequirement.restrictive))
                    .append("\n# ").append(lang("hand.requirement.description.4",HandRequirement.none)).getString());
            file.write("\n# "+lang("hand.requirement.options",Utl.Assets.REQUIREMENT_OPTIONS));
            file.write("\nhand.main.requirement=" + mainReq);
            file.write("\n#");
            file.write("\nhand.off.requirement=" + offReq);

            file.write("\n\n# "+lang("hand.restriction"));
            file.write("\n# "+lang("hand.restriction.description"));
            file.write("\nhand.main.block=" + mainBlock);
            file.write("\nhand.main.food=" + mainFood);
            file.write("\nhand.main.usable=" + mainUsable);
            file.write("\n#");
            file.write("\nhand.off.block=" + offBlock);
            file.write("\nhand.off.food=" + offFood);
            file.write("\nhand.off.usable=" + offUsable);

            file.write("\n\n# "+lang("hand.restriction.list"));
            file.write("\n# "+lang("hand.restriction.list.description"));
            file.write("\n# "+lang("example",Utl.Assets.LIST));
            file.write("\nhand.main.whitelist="+gson.toJson(mainWhitelist));
            file.write("\nhand.main.blacklist="+gson.toJson(mainBlacklist));
            file.write("\n#");
            file.write("\nhand.off.whitelist="+gson.toJson(offWhitelist));
            file.write("\nhand.off.blacklist="+gson.toJson(offBlacklist));

            // send packets to update the settings on the server
            SitClient.sendSettingsPackets();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class defaults {
        public static double version = 1.1;
        public static String lang = "en_us";
        public static boolean keepActive = true;
        public static boolean sitWhileSeated = true;
        public static boolean stairsOn = true;
        public static boolean slabsOn = true;
        public static boolean carpetsOn = true;
        public static boolean fullBlocksOn = false;
        public static boolean customOn = false;
        public static List<String> customBlocks = List.of("minecraft:campfire|.46|1|lit=false","minecraft:soul_campfire|.46|1|lit=false,waterlogged=false");
        public static HandRequirement mainReq = HandRequirement.empty;
        public static boolean mainBlock = false;
        public static boolean mainFood = false;
        public static boolean mainUsable = false;
        public static List<String> mainWhitelist = new ArrayList<>();
        public static List<String> mainBlacklist = new ArrayList<>();
        public static HandRequirement offReq = HandRequirement.restrictive;
        public static boolean offBlock = true;
        public static boolean offFood = false;
        public static boolean offUsable = true;
        public static List<String> offWhitelist = List.of("minecraft:torch","minecraft:soul_torch","minecraft:redstone_torch");
        public static List<String> offBlacklist = new ArrayList<>();
    }
}
