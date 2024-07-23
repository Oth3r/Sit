package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;
import one.oth3r.sit.Sit;
import one.oth3r.sit.utl.Utl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ServerConfig {

    @SerializedName("version")
    private Double version = 2.0;
    @SerializedName("lang")
    private String lang = "en_us";
    @SerializedName("keep-active")
    private Boolean keepActive = true;
    @SerializedName("sit-while-seated")
    private Boolean sitWhileSeated = false;
    @SerializedName("preset-blocks")
    private PresetBlocks presetBlocks = new PresetBlocks();
    @SerializedName("custom-enabled")
    private Boolean customEnabled = false;
    @SerializedName("custom-blocks")
    private ArrayList<CustomBlock> customBlocks = new ArrayList<>();
    @SerializedName("blacklisted-blocks")
    private ArrayList<String> blacklistedBlocks = new ArrayList<>();

    public ServerConfig() {}

    public ServerConfig(ServerConfig serverConfig) {
        this.version = serverConfig.version;
        this.lang = serverConfig.lang;
        this.keepActive = serverConfig.keepActive;
        this.sitWhileSeated = serverConfig.sitWhileSeated;
        this.presetBlocks = serverConfig.presetBlocks;
        this.customEnabled = serverConfig.customEnabled;
        this.customBlocks = serverConfig.customBlocks;
        this.blacklistedBlocks = serverConfig.blacklistedBlocks;
    }

    public ServerConfig(Double version, String lang, boolean keepActive, boolean sitWhileSeated, PresetBlocks presetBlocks, boolean customEnabled, ArrayList<CustomBlock> customBlocks, ArrayList<String> blacklistedBlocks) {
        this.version = version;
        this.lang = lang;
        this.keepActive = keepActive;
        this.sitWhileSeated = sitWhileSeated;
        this.presetBlocks = presetBlocks;
        this.customEnabled = customEnabled;
        this.customBlocks = customBlocks;
        this.blacklistedBlocks = blacklistedBlocks;
    }

    public Double getVersion() {
        return version;
    }

    public String getLang() {
        return lang;
    }

    public boolean isKeepActive() {
        return keepActive;
    }

    public boolean isSitWhileSeated() {
        return sitWhileSeated;
    }

    public PresetBlocks getPresetBlocks() {
        return presetBlocks;
    }

    public Boolean isCustomEnabled() {
        return customEnabled;
    }

    public ArrayList<CustomBlock> getCustomBlocks() {
        return customBlocks;
    }

    public ArrayList<String> getBlacklistedBlocks() {
        return blacklistedBlocks;
    }

    public static class PresetBlocks {

        @SerializedName("stairs")
        private boolean stairs = true;
        @SerializedName("slabs")
        private boolean slabs = true;
        @SerializedName("carpets")
        private boolean carpets = true;
        @SerializedName("full-blocks")
        private boolean fullBlocks = false;

        public PresetBlocks() {}

        public PresetBlocks(boolean stairs, boolean slabs, boolean carpets, boolean fullBlocks) {
            this.stairs = stairs;
            this.slabs = slabs;
            this.carpets = carpets;
            this.fullBlocks = fullBlocks;
        }

        public boolean isStairs() {
            return stairs;
        }

        public boolean isSlabs() {
            return slabs;
        }

        public boolean isCarpets() {
            return carpets;
        }

        public boolean isFullBlocks() {
            return fullBlocks;
        }
    }



    public static File getFile() {
        return new File(Sit.CONFIG_DIR+"server-config.json");
    }

    /**
     * loads the directionhud Config file to Data.config
     */
    public static void load(boolean tryLegacy) {

        File file = getFile();
        if (!file.exists()) {
            // try to make the config directory
            try {
                Files.createDirectories(Paths.get(Sit.CONFIG_DIR));
            } catch (Exception e) {
                Sit.LOGGER.error("Failed to create config directory. Canceling all config loading...");
                return;
            }
            // if loading from legacy, try checking the old config directory for the file
            if (tryLegacy && Updater.ServerConfigFile.Legacy.getLegacyFile().exists()) {
                Sit.LOGGER.info("Updating Sit!.properties to sit!/config.json");
                Updater.ServerConfigFile.Legacy.run();
            }
            save();
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            Updater.ServerConfigFile.run(reader);
        } catch (Exception e) {
            Sit.LOGGER.error(String.format("ERROR LOADING '%s`: %s", file.getName(),e.getMessage()));
        }
        // save after loading
        save();
    }

    /**
     * saves Data.config to config.json
     */
    public static void save() {
        if (!getFile().exists()) {
            Sit.LOGGER.info(String.format("Creating new `%s`", getFile().getName()));
        }
        try (BufferedWriter writer = Files.newBufferedWriter(getFile().toPath(), StandardCharsets.UTF_8)) {
            writer.write(Utl.getGson().toJson(FileData.getServerConfig()));
        } catch (Exception e) {
            Sit.LOGGER.info(String.format("ERROR SAVING '%s`: %s", getFile().getName(), e.getMessage()));
        }
    }
}
