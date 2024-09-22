package one.oth3r.sit.file;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.Hand;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Utl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class ServerConfig implements CustomFile<ServerConfig> {

    @SerializedName("version")
    private Double version = 2.0;
    @SerializedName("lang")
    private String lang = "en_us";
    @SerializedName("lang-options")
    private String langOptions = "en_us";
    @SerializedName("keep-active")
    private Boolean keepActive = true;
    @SerializedName("sit-while-seated")
    private Boolean sitWhileSeated = false;
    @SerializedName("preset-blocks")
    private PresetBlocks presetBlocks = new PresetBlocks();
    @SerializedName("custom-enabled")
    private Boolean customEnabled = false;
    @SerializedName("custom-blocks")
    private ArrayList<SittingBlock> sittingBlocks = FileData.Defaults.SITTING_BLOCKS;
    @SerializedName("blacklisted-blocks")
    private ArrayList<String> blacklistedBlocks = new ArrayList<>();
    @SerializedName("blacklisted-interactions")
    private ArrayList<String> blacklistedInteractions = new ArrayList<>();

    public ServerConfig() {}

    public ServerConfig(ServerConfig serverConfig) {
        this.version = serverConfig.version;
        this.lang = serverConfig.lang;
        this.keepActive = serverConfig.keepActive;
        this.sitWhileSeated = serverConfig.sitWhileSeated;
        this.presetBlocks = serverConfig.presetBlocks;
        this.customEnabled = serverConfig.customEnabled;
        this.sittingBlocks = serverConfig.sittingBlocks;
        this.blacklistedBlocks = serverConfig.blacklistedBlocks;
    }

    public ServerConfig(Double version, String lang, boolean keepActive, boolean sitWhileSeated, PresetBlocks presetBlocks, boolean customEnabled, ArrayList<SittingBlock> sittingBlocks, ArrayList<String> blacklistedBlocks) {
        this.version = version;
        this.lang = lang;
        this.keepActive = keepActive;
        this.sitWhileSeated = sitWhileSeated;
        this.presetBlocks = presetBlocks;
        this.customEnabled = customEnabled;
        this.sittingBlocks = sittingBlocks;
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

    public boolean canSitWhileSeated() {
        return sitWhileSeated;
    }

    public PresetBlocks getPresetBlocks() {
        return presetBlocks;
    }

    public Boolean isCustomEnabled() {
        return customEnabled;
    }

    public ArrayList<SittingBlock> getSittingBlocks() {
        return sittingBlocks;
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

    @Override
    public void reset() {
        updateToNewFile(new ServerConfig());
    }

    @Override
    public @NotNull Class<ServerConfig> getFileClass() {
        return ServerConfig.class;
    }

    @Override
    public void updateToNewFile(ServerConfig newFile) {
        this.version = newFile.version;
        this.lang = newFile.lang;
        this.keepActive = newFile.keepActive;
        this.sitWhileSeated = newFile.sitWhileSeated;
        this.presetBlocks = newFile.presetBlocks;
        this.customEnabled = newFile.customEnabled;
        this.sittingBlocks = newFile.sittingBlocks;
        this.blacklistedBlocks = newFile.blacklistedBlocks;
    }

    @Override
    public String getFileName() {
        return "server-config.json";
    }

    @Override
    public String getDirectory() {
        return Data.CONFIG_DIR;
    }

    @Override
    public void fileNotExist() {
        CustomFile.super.fileNotExist();
        // try checking the old/legacy config directory for the file
        if (Legacy.getLegacyFile().exists()) {
            Data.LOGGER.info("Updating Sit!.properties to sit!/config.json");
            Legacy.run();
        }
    }

    protected static class Legacy {
        /**
         * gets the legacy file, from the old directory for fabric, and the same one for spigot
         */
        public static File getLegacyFile() {
            // strip the new directory
            return new File(Data.CONFIG_DIR.substring(0, Data.CONFIG_DIR.length()-5)+"Sit!.properties");
        }

        /**
         * updates the old Sit!.properties to config.json
         */
        public static void run() {
            // shouldn't happen, only call if the file exists
            File file = getLegacyFile();
            if (!file.exists()) return;

            // update to the new system
            try (FileInputStream fileStream = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(fileStream);
                String ver = (String) properties.computeIfAbsent("version", a -> String.valueOf(new ServerConfig().getVersion()));

                // if the old version system (v1.0) remove "v"
                if (ver.contains("v")) ver = ver.substring(1);

                loadVersion(properties,Double.parseDouble(ver));

            } catch (Exception e) {
                Data.LOGGER.error("Error loading legacy config file: {}", e.getMessage());
            }

            // delete the old file
            try {
                Files.delete(file.toPath());
                Data.LOGGER.info("Deleted " + file.getName());
            } catch (Exception e) {
                Data.LOGGER.error("Failed to delete the old Sit! config.");
            }
        }

        /**
         * converts the legacy hand requirement enum to the new one
         * @param requirement the old string
         */
        private static HandSetting.SittingRequirement handRequirementUpdater(String requirement) {
            return switch (requirement) {
                case "restrictive" -> HandSetting.SittingRequirement.FILTER;
                case "none" -> HandSetting.SittingRequirement.NONE;
                default -> HandSetting.SittingRequirement.EMPTY;
            };
        }

        /**
         * gets a list of custom blocks from the legacy way of entering custom sit blocks
         */
        private static ArrayList<SittingBlock> getCustomBlocks(ArrayList<String> fix) {
            //eg. minecraft:campfire|.46|1|lit=false
            ArrayList<SittingBlock> out = new ArrayList<>();
            for (String entry : fix) {
                String[] split = entry.split("\\|");
                // skip if not the right size
                if (split.length < 3 || split.length > 4) continue;
                // if the other entries aren't correct, skip
                if (!Utl.Num.isNum(split[2])) continue;

                // make the block states list if possible
                ArrayList<String> blockstates = new ArrayList<>();
                // if there are blockstates
                if (split.length == 4) {
                    blockstates.addAll(Arrays.asList(split[3].split(",")));
                }

                // add if everything is A-OK
                out.add(new SittingBlock(
                        new ArrayList<>(Arrays.asList(split[0])),
                        new ArrayList<>(),blockstates,Double.parseDouble(split[1])));
            }
            return out;
        }

        private static ArrayList<String> getFilterList(ArrayList<String> whitelist, ArrayList<String> blacklist) {
            ArrayList<String> out = new ArrayList<>(whitelist);
            // add a ! in front of every entry of the blacklist
            out.addAll(blacklist.stream().map(e -> "!"+e).toList());
            return out;
        }

        public static void loadVersion(Properties properties, double version) {
            try {
                Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                ServerConfig defaultConfig = new ServerConfig();

                // load the latest config
                ServerConfig serverConfig = new ServerConfig(
                        2.0,
                        (String) properties.computeIfAbsent("lang", a -> defaultConfig.getLang()),
                        Boolean.parseBoolean((String) properties.computeIfAbsent("keep-active", a -> String.valueOf(defaultConfig.isKeepActive()))),
                        Boolean.parseBoolean((String) properties.computeIfAbsent("sit-while-seated", a -> String.valueOf(defaultConfig.canSitWhileSeated()))),
                        new PresetBlocks(
                                Boolean.parseBoolean((String) properties.computeIfAbsent("stairs", a -> String.valueOf(defaultConfig.getPresetBlocks().isStairs()))),
                                Boolean.parseBoolean((String) properties.computeIfAbsent("slabs", a -> String.valueOf(defaultConfig.getPresetBlocks().isSlabs()))),
                                Boolean.parseBoolean((String) properties.computeIfAbsent("carpets", a -> String.valueOf(defaultConfig.getPresetBlocks().isCarpets()))),
                                Boolean.parseBoolean((String) properties.computeIfAbsent("full-blocks", a -> String.valueOf(defaultConfig.getPresetBlocks().isFullBlocks())))
                        ),
                        Boolean.parseBoolean((String) properties.computeIfAbsent("custom", a -> String.valueOf(defaultConfig.isCustomEnabled()))),
                        getCustomBlocks(new Gson().fromJson((String)
                                properties.computeIfAbsent("custom-blocks", a -> "[]"), listType)),
                        new ArrayList<>()
                );

                SittingConfig defaultSittingConfig = new SittingConfig();

                SittingConfig sittingConfig = new SittingConfig();
                // * filters are flipped because the way they work are flipped
                try {
                    sittingConfig = new SittingConfig(
                            1.0, true, Boolean.parseBoolean((String) properties.computeIfAbsent("hand.sitting", a -> String.valueOf(defaultSittingConfig.canSitWithHand()))),
                            new HandSetting(
                                    handRequirementUpdater((String) properties.computeIfAbsent("hand.main.requirement", a -> String.valueOf(defaultSittingConfig.getHand(Hand.MAIN_HAND).getSittingRequirement()))),
                                    new HandSetting.Filter(
                                            !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().isBlock()))),
                                            !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().isFood()))),
                                            !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().isUsable()))),
                                            new CustomItem(getFilterList(
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.main.whitelist", a -> "[]"), listType),
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.main.blacklist", a -> "[]"), listType)
                                                ),
                                                new ArrayList<>())
                                    )
                            ),
                            new HandSetting(
                                    handRequirementUpdater((String) properties.computeIfAbsent("hand.off.requirement", a -> String.valueOf(defaultSittingConfig.getHand(Hand.OFF_HAND).getSittingRequirement()))),
                                    new HandSetting.Filter(
                                            !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().isBlock()))),
                                            !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().isFood()))),
                                            !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().isUsable()))),
                                            new CustomItem(getFilterList(
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.off.whitelist", a -> "[]"), listType),
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.off.blacklist", a -> "[]"), listType)
                                                ),
                                                new ArrayList<>())
                                    )
                            )
                    );
                } catch (JsonSyntaxException ignored) {}

                // load an older version
                if (version == 1.0) {
                    try {
                        sittingConfig = new SittingConfig(
                                1.0, true, defaultSittingConfig.canSitWithHand(),
                                new HandSetting(
                                        handRequirementUpdater((String) properties.computeIfAbsent("main-hand-requirement", a -> String.valueOf(defaultSittingConfig.getHand(Hand.MAIN_HAND).getSittingRequirement()))),
                                        new HandSetting.Filter(
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().isBlock()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().isFood()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().isUsable()))),
                                                new CustomItem(getFilterList(
                                                            new Gson().fromJson((String) properties.computeIfAbsent("main-hand-whitelist", a -> "[]"), listType),
                                                            new Gson().fromJson((String) properties.computeIfAbsent("main-hand-blacklist", a -> "[]"), listType)
                                                    ),
                                                    new ArrayList<>())
                                        )
                                ),
                                new HandSetting(
                                        handRequirementUpdater((String) properties.computeIfAbsent("off-hand-requirement", a -> String.valueOf(defaultSittingConfig.getHand(Hand.OFF_HAND).getSittingRequirement()))),
                                        new HandSetting.Filter(
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().isBlock()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().isFood()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().isUsable()))),
                                                new CustomItem(getFilterList(
                                                            new Gson().fromJson((String) properties.computeIfAbsent("off-hand-whitelist", a -> "[]"), listType),
                                                            new Gson().fromJson((String) properties.computeIfAbsent("off-hand-blacklist", a -> "[]"), listType)
                                                    ),
                                                    new ArrayList<>())
                                        )
                                )
                        );
                    } catch (JsonSyntaxException ignored) {}
                }

                FileData.setServerConfig(serverConfig);
                FileData.setSittingConfig(sittingConfig);
                serverConfig.save();
                sittingConfig.save();
            } catch (Exception e) {
                Data.LOGGER.error("Error loading legacy config: {}", e.getMessage());
            }
        }
    }
}
