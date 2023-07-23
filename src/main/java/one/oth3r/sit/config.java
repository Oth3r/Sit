package one.oth3r.sit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.MutableText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class config {
    public static String lang = defaults.lang;
    public static boolean keepActive = defaults.keepActive;
    public static boolean sitWhileSeated = defaults.sitWhileSeated;
    public static boolean stairsOn = defaults.stairsOn;
    public static boolean slabsOn = defaults.slabsOn;
    public static boolean carpetsOn = defaults.carpetsOn;
    public static boolean fullBlocksOn = defaults.fullBlocksOn;
    public static boolean customOn = defaults.customOn;
    public static List<String> customBlocks = defaults.customBlocks;
    enum MainReq {
        empty,
        restrictive,
        none
    }
    public static MainReq mainReq = defaults.mainReq;
    public static boolean mainBlock = defaults.mainBlock;
    public static boolean mainFood = defaults.mainFood;
    public static boolean mainUsable = defaults.mainUsable;
    public static List<String> mainWhitelist = defaults.mainWhitelist;
    public static List<String> mainBlacklist = defaults.mainBlacklist;
    enum OffReq {
        empty,
        restrictive,
        none
    }
    public static OffReq offReq = defaults.offReq;
    public static boolean offBlock = defaults.offBlock;
    public static boolean offFood = defaults.offFood;
    public static boolean offUsable = defaults.offUsable;
    public static List<String> offWhitelist = defaults.offWhitelist;
    public static List<String> offBlacklist = defaults.offBlacklist;
    public static void resetDefaults() {
        lang = defaults.lang;
        keepActive = defaults.keepActive;
        sitWhileSeated = defaults.sitWhileSeated;
        stairsOn = defaults.stairsOn;
        slabsOn = defaults.slabsOn;
        carpetsOn = defaults.carpetsOn;
        fullBlocksOn = defaults.fullBlocksOn;
        customOn = defaults.customOn;
        customBlocks = defaults.customBlocks;
        mainReq = defaults.mainReq;
        mainBlock = defaults.mainBlock;
        mainFood = defaults.mainFood;
        mainUsable = defaults.mainUsable;
        mainWhitelist = defaults.mainWhitelist;
        mainBlacklist = defaults.mainBlacklist;
        offReq = defaults.offReq;
        offBlock = defaults.offBlock;
        offFood = defaults.offFood;
        offUsable = defaults.offUsable;
        offWhitelist = defaults.offWhitelist;
        offBlacklist = defaults.offBlacklist;
        save();
    }
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
            loadVersion(properties,(String) properties.computeIfAbsent("version", a -> defaults.version+""));
            save();
        } catch (Exception f) {
            //read fail
            f.printStackTrace();
            resetDefaults();
        }
    }
    public static void loadVersion(Properties properties, String version) {
        Type mapType = new TypeToken<ArrayList<String>>() {}.getType();
        lang = (String) properties.computeIfAbsent("lang", a -> defaults.lang+"");
        //CONFIG
        keepActive = Boolean.parseBoolean((String) properties.computeIfAbsent("keep-active", a -> defaults.keepActive+""));
        sitWhileSeated = Boolean.parseBoolean((String) properties.computeIfAbsent("sit-while-seated", a -> defaults.sitWhileSeated+""));
        stairsOn = Boolean.parseBoolean((String) properties.computeIfAbsent("stairs", a -> defaults.stairsOn+""));
        slabsOn = Boolean.parseBoolean((String) properties.computeIfAbsent("slabs", a -> defaults.slabsOn+""));
        carpetsOn = Boolean.parseBoolean((String) properties.computeIfAbsent("carpets", a -> defaults.carpetsOn+""));
        fullBlocksOn = Boolean.parseBoolean((String) properties.computeIfAbsent("full-blocks", a -> defaults.fullBlocksOn+""));
        customOn = Boolean.parseBoolean((String) properties.computeIfAbsent("custom", a -> defaults.customOn+""));
        customBlocks = new Gson().fromJson((String)
                properties.computeIfAbsent("custom-blocks", a -> defaults.customBlocks+""),mapType);
        mainReq = MainReq.valueOf((String) properties.computeIfAbsent("main-hand-requirement", a -> defaults.mainReq+""));
        mainBlock = Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-block", a -> defaults.mainBlock+""));
        mainFood = Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-food", a -> defaults.mainFood+""));
        mainUsable = Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-usable", a -> defaults.mainUsable +""));
        mainWhitelist = new Gson().fromJson((String)
                properties.computeIfAbsent("main-hand-whitelist", a -> defaults.mainWhitelist+""),mapType);
        mainBlacklist = new Gson().fromJson((String)
                properties.computeIfAbsent("main-hand-blacklist", a -> defaults.mainBlacklist+""),mapType);
        offReq = OffReq.valueOf((String) properties.computeIfAbsent("off-hand-requirement", a -> defaults.offReq+""));
        offBlock = Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-block", a -> defaults.offBlock+""));
        offFood = Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-food", a -> defaults.offFood+""));
        offUsable = Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-usable", a -> defaults.offUsable +""));
        offWhitelist = new Gson().fromJson((String)
                properties.computeIfAbsent("off-hand-whitelist", a -> defaults.offWhitelist+""),mapType);
        offBlacklist = new Gson().fromJson((String)
                properties.computeIfAbsent("off-hand-blacklist", a -> defaults.offBlacklist+""),mapType);
    }
    public static MutableText lang(String key, Object... args) {
        LangReader.loadLanguageFile();
        return LangReader.of("config.sit."+key, args).getTxT();
    }
    public static void save() {
        try (var file = new FileOutputStream(configFile(), false)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            file.write("# Sit! Config\n".getBytes());
            file.write(("version="+defaults.version).getBytes());
            file.write(("\n# all available languages: en_us").getBytes());
            file.write(("\nlang=" + lang).getBytes());
            file.write(("\n\n# "+lang("general.keep_active.description").getString()).getBytes());
            file.write(("\nkeep-active=" + keepActive).getBytes());
            file.write(("\n# "+lang("general.sit_while_seated.description").getString()).getBytes());
            file.write(("\nsit-while-seated=" + sitWhileSeated).getBytes());
            file.write(("\n# "+lang("general.sittable.description").getString()).getBytes());
            file.write(("\nstairs=" + stairsOn).getBytes());
            file.write(("\nslabs=" + slabsOn).getBytes());
            file.write(("\ncarpets=" + carpetsOn).getBytes());
            file.write(("\nfull-blocks=" + fullBlocksOn).getBytes());
            file.write(("\ncustom=" + customOn).getBytes());
            file.write(("\n# "+lang("general.sittable_blocks.description")
                    .append("\n# ").append(lang("general.sittable_blocks.description_2"))
                    .append(lang("general.sittable_blocks.description_3",
                            lang("general.sittable_blocks.description_3_2"),
                            lang("general.sittable_blocks.description_3_3"),
                            lang("general.sittable_blocks.description_3_4"),
                            lang("general.sittable_blocks.description_3_5")))
                    .append("\n# ").append(lang("general.sittable_blocks.description_4"))
                    .append("\n# ").append(lang("general.sittable_blocks.description_5"))
                    .append("\n# ").append(lang("general.sittable_blocks.description_6"))
                    .append("\n# ").append(lang("general.sittable_blocks.description_7"))
                    .append("\n# ").append(lang("general.sittable_blocks.description_8")).getString()).getBytes());
            file.write(("\ncustom-blocks="+gson.toJson(customBlocks)).getBytes());
            file.write(("\n\n# "+lang("hand.requirements.description")
                    .append("\n# ").append(lang("hand.requirements.description_2"))
                    .append("\n# ").append(lang("hand.requirements.description_3"))
                    .append("\n# ").append(lang("hand.requirements.description_4")).getString()).getBytes());
            file.write(("\nmain-hand-requirement=" + mainReq).getBytes());
            file.write(("\nmain-hand-block=" + mainBlock).getBytes());
            file.write(("\nmain-hand-food=" + mainFood).getBytes());
            file.write(("\nmain-hand-usable=" + mainUsable).getBytes());
            file.write(("\nmain-hand-whitelist="+gson.toJson(mainWhitelist)).getBytes());
            file.write(("\nmain-hand-blacklist="+gson.toJson(mainBlacklist)).getBytes());
            file.write(("\noff-hand-requirement=" + offReq).getBytes());
            file.write(("\noff-hand-block=" + offBlock).getBytes());
            file.write(("\noff-hand-food=" + offFood).getBytes());
            file.write(("\noff-hand-usable=" + offUsable).getBytes());
            file.write(("\noff-hand-whitelist="+gson.toJson(offWhitelist)).getBytes());
            file.write(("\noff-hand-blacklist="+gson.toJson(offBlacklist)).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class defaults {
        public static String version = "v1.0";
        public static String lang = "en_us";
        public static boolean keepActive = true;
        public static boolean sitWhileSeated = true;
        public static boolean stairsOn = true;
        public static boolean slabsOn = true;
        public static boolean carpetsOn = true;
        public static boolean fullBlocksOn = false;
        public static boolean customOn = false;
        public static List<String> customBlocks = List.of("minecraft:campfire|.46|1|lit=false","minecraft:soul_campfire|.46|1|lit=false");
        public static MainReq mainReq = MainReq.empty;
        public static boolean mainBlock = false;
        public static boolean mainFood = false;
        public static boolean mainUsable = false;
        public static List<String> mainWhitelist = new ArrayList<>();
        public static List<String> mainBlacklist = new ArrayList<>();
        public static OffReq offReq = OffReq.restrictive;
        public static boolean offBlock = true;
        public static boolean offFood = false;
        public static boolean offUsable = true;
        public static List<String> offWhitelist = List.of("minecraft:torch","minecraft:soul_torch","minecraft:redstone_torch");
        public static List<String> offBlacklist = new ArrayList<>();
    }
}
