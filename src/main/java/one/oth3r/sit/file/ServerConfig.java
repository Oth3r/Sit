package one.oth3r.sit.file;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.Hand;
import one.oth3r.otterlib.base.Num;
import one.oth3r.otterlib.file.CustomFile;
import one.oth3r.otterlib.file.FileSettings;
import one.oth3r.otterlib.registry.LanguageReg;
import one.oth3r.sit.utl.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class ServerConfig implements CustomFile<ServerConfig> {
    public static final String ID = "server-config";

    @SerializedName("version")
    private Double version = 2.3;

    @SerializedName("lang")
    private String lang = "en_us";
    @SerializedName("lang-options")
    private final String langOptions = "en_us, it_it, pt_br, tr_tr, zh_tw, zh_ch, de_de";

    @SerializedName("keep-active")
    private Boolean keepActive = true;
    @SerializedName("sit-while-seated")
    private Boolean sitWhileSeated = false;
    @SerializedName("preset-blocks")
    private PresetBlocks presetBlocks = new PresetBlocks();

    @SerializedName("height-difference-limit")
    private YDifferenceLimit yDifferenceLimit = new YDifferenceLimit();

    @SerializedName("custom-enabled")
    private Boolean customEnabled = false;
    @SerializedName("custom-blocks")
    private ArrayList<SittingBlock> sittingBlocks = FileData.Defaults.SITTING_BLOCKS;

    @SerializedName("blacklisted-blocks")
    private ArrayList<CustomBlock> blacklistedBlocks = FileData.Defaults.BLACKLISTED_BLOCKS;
    @SerializedName("interaction-blocks")
    private ArrayList<CustomBlock> interactionBlocks = FileData.Defaults.INTERACTION_BLOCKS;
    @SerializedName("allowed-above-seat")
    private ArrayList<CustomBlock> allowedAboveSeat = FileData.Defaults.ALLOWED_ABOVE_SEAT;

    public ServerConfig() {}

    public ServerConfig(ServerConfig serverConfig) {
        copyFileData(serverConfig);
    }

    public ServerConfig(Double version, String lang, boolean keepActive, boolean sitWhileSeated,
                        PresetBlocks presetBlocks, boolean customEnabled,
                        ArrayList<SittingBlock> sittingBlocks, ArrayList<CustomBlock> blacklistedBlocks,
                        ArrayList<CustomBlock> interactionBlocks, ArrayList<CustomBlock> allowedAboveSeat) {
        this.version = version;
        this.lang = lang;
        this.keepActive = keepActive;
        this.sitWhileSeated = sitWhileSeated;
        this.presetBlocks = presetBlocks;
        this.customEnabled = customEnabled;
        this.sittingBlocks = sittingBlocks;
        this.blacklistedBlocks = blacklistedBlocks;
        this.interactionBlocks = interactionBlocks;
        this.allowedAboveSeat = allowedAboveSeat;
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

    public YDifferenceLimit getYDifferenceLimit() {
        return yDifferenceLimit;
    }

    public Boolean isCustomEnabled() {
        return customEnabled;
    }

    public ArrayList<SittingBlock> getSittingBlocks() {
        return sittingBlocks;
    }

    public ArrayList<CustomBlock> getBlacklistedBlocks() {
        return blacklistedBlocks;
    }

    public ArrayList<CustomBlock> getInteractionBlocks() {
        return interactionBlocks;
    }

    public ArrayList<CustomBlock> getAllowedAboveSeat() {
        return allowedAboveSeat;
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

        public PresetBlocks(PresetBlocks presetBlocks) {
            this.stairs = presetBlocks.stairs;
            this.slabs = presetBlocks.slabs;
            this.carpets = presetBlocks.carpets;
            this.fullBlocks = presetBlocks.fullBlocks;
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

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PresetBlocks that = (PresetBlocks) o;
            return stairs == that.stairs && slabs == that.slabs && carpets == that.carpets && fullBlocks == that.fullBlocks;
        }

        @Override
        public int hashCode() {
            return Objects.hash(stairs, slabs, carpets, fullBlocks);
        }
    }

    public static class YDifferenceLimit {
        @SerializedName("above")
        private Double above = 1.0;
        @SerializedName("below")
        private Double below = 1.0;

        public YDifferenceLimit() {
        }

        public YDifferenceLimit(Double above, Double below) {
            this.above = above;
            this.below = below;
        }

        public YDifferenceLimit(YDifferenceLimit yDifferenceLimit) {
            this.above = yDifferenceLimit.above;
            this.below = yDifferenceLimit.below;
        }

        public Double getAbove() {
            return above;
        }

        public void setAbove(Double above) {
            this.above = above;
        }

        public Double getBelow() {
            return below;
        }

        public void setBelow(Double below) {
            this.below = below;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            YDifferenceLimit that = (YDifferenceLimit) o;
            return Objects.equals(above, that.above) && Objects.equals(below, that.below);
        }

        @Override
        public int hashCode() {
            return Objects.hash(above, below);
        }
    }

    @Override
    public FileSettings getFileSettings() {
        return new FileSettings(Data.LOGGER);
    }

    /**
     * the path to the file - including the extension ex. usr/config/custom-file.json
     */
    @Override
    public Path getFilePath() {
        return Paths.get(Data.CONFIG_DIR, "server-config.json");
    }

    @Override
    public void reset() {
        copyFileData(new ServerConfig());
    }

    @Override
    public @NotNull Class<ServerConfig> getFileClass() {
        return ServerConfig.class;
    }

    /**
     * loads the data from the file object into the current object - DEEP COPY
     *
     * @param newFile the file to take the properties from
     */
    @Override
    public void copyFileData(ServerConfig newFile) {
        this.version = newFile.version;
        this.lang = newFile.lang;
        this.keepActive = newFile.keepActive;
        this.sitWhileSeated = newFile.sitWhileSeated;

        this.presetBlocks = new PresetBlocks(newFile.presetBlocks);

        this.yDifferenceLimit = new YDifferenceLimit(newFile.yDifferenceLimit);

        this.customEnabled = newFile.customEnabled;
        this.sittingBlocks = newFile.sittingBlocks.stream().map(SittingBlock::new).collect(Collectors.toCollection(ArrayList::new));
        this.blacklistedBlocks = newFile.blacklistedBlocks.stream().map(CustomBlock::new).collect(Collectors.toCollection(ArrayList::new));
        this.interactionBlocks = newFile.interactionBlocks.stream().map(CustomBlock::new).collect(Collectors.toCollection(ArrayList::new));
        this.allowedAboveSeat = newFile.allowedAboveSeat.stream().map(CustomBlock::new).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void updateInstance() {
        /// update to 2.1, just a new list, nothing to change
        /// update to 2.2, new settings, no changes
        if (version >= 2.0 && version <= 2.1) {
            version = 2.2;
        }
        if (version == 2.2) {
            // make sure that the lang is all lowercase
            version = 2.3;
            this.lang = this.lang.substring(0,3)+this.lang.substring(3).toLowerCase();
        }

        // update the language reader
        LanguageReg.getLang(Data.MOD_ID).updateLanguage(lang);
    }

    @Override
    public void createDirectory() {
        CustomFile.super.createDirectory();
        // try checking the old/legacy config directory for the file
        if (Legacy.getLegacyFile().exists()) {
            Data.LOGGER.info("Updating Sit!.properties to sit!/config.json");
            Legacy.run();
        }
    }

    @Override
    public ServerConfig clone() {
        ServerConfig clone = new ServerConfig();
        clone.copyFileData(this);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerConfig that = (ServerConfig) o;
        return Objects.equals(version, that.version) && Objects.equals(lang, that.lang) && Objects.equals(keepActive, that.keepActive) && Objects.equals(sitWhileSeated, that.sitWhileSeated) && Objects.equals(presetBlocks, that.presetBlocks) && Objects.equals(yDifferenceLimit, that.yDifferenceLimit) && Objects.equals(customEnabled, that.customEnabled) && Objects.equals(sittingBlocks, that.sittingBlocks) && Objects.equals(blacklistedBlocks, that.blacklistedBlocks) && Objects.equals(interactionBlocks, that.interactionBlocks) && Objects.equals(allowedAboveSeat, that.allowedAboveSeat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, lang, langOptions, keepActive, sitWhileSeated, presetBlocks, yDifferenceLimit, customEnabled, sittingBlocks, blacklistedBlocks, interactionBlocks, allowedAboveSeat);
    }

    /**
     * legacy updater
     */
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

                loadAndUpdateVersion(properties,Double.parseDouble(ver));

            } catch (Exception e) {
                Data.LOGGER.error("Error loading legacy config file: %s", e.getMessage());
            }

            // continue loading as normal...
        }

        private static void deleteLegacyFile() {
            File file = getLegacyFile();
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
                if (!Num.isNum(split[2])) continue;

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

        public static void loadAndUpdateVersion(Properties properties, double version) {
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
                        new ArrayList<>(), FileData.Defaults.INTERACTION_BLOCKS, FileData.Defaults.ALLOWED_ABOVE_SEAT
                );

                SittingConfig defaultSittingConfig = new SittingConfig();

                SittingConfig sittingConfig = new SittingConfig();
                // * filters are flipped because the way they work are flipped
                try {
                    sittingConfig = new SittingConfig(
                            1.0, true, Boolean.parseBoolean((String) properties.computeIfAbsent("hand.sitting", a -> String.valueOf(defaultSittingConfig.canSitWithHand()))),
                            new HandSetting(
                                    handRequirementUpdater((String) properties.computeIfAbsent("hand.main.requirement", a -> String.valueOf(defaultSittingConfig.getHand(Hand.MAIN_HAND).getSittingRequirement()))),
                                    new HandSetting.Filter(false,
                                            new HandSetting.Filter.Presets(
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().getPresets().isBlock()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().getPresets().isFood()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().getPresets().isUsable())))),
                                            new CustomItem(getFilterList(
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.main.whitelist", a -> "[]"), listType),
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.main.blacklist", a -> "[]"), listType)
                                                ),
                                                new ArrayList<>())
                                    )
                            ),
                            new HandSetting(
                                    handRequirementUpdater((String) properties.computeIfAbsent("hand.off.requirement", a -> String.valueOf(defaultSittingConfig.getHand(Hand.OFF_HAND).getSittingRequirement()))),
                                    new HandSetting.Filter(false,
                                            new HandSetting.Filter.Presets(
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().getPresets().isBlock()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().getPresets().isFood()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().getPresets().isUsable())))),
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
                                        new HandSetting.Filter(false,
                                                new HandSetting.Filter.Presets(
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().getPresets().isBlock()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().getPresets().isFood()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.MAIN_HAND).getFilter().getPresets().isUsable())))),
                                                new CustomItem(getFilterList(
                                                            new Gson().fromJson((String) properties.computeIfAbsent("main-hand-whitelist", a -> "[]"), listType),
                                                            new Gson().fromJson((String) properties.computeIfAbsent("main-hand-blacklist", a -> "[]"), listType)
                                                    ),
                                                    new ArrayList<>())
                                        )
                                ),
                                new HandSetting(
                                        handRequirementUpdater((String) properties.computeIfAbsent("off-hand-requirement", a -> String.valueOf(defaultSittingConfig.getHand(Hand.OFF_HAND).getSittingRequirement()))),
                                        new HandSetting.Filter(false,
                                                new HandSetting.Filter.Presets(
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-block", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().getPresets().isBlock()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-food", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().getPresets().isFood()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-usable", a -> String.valueOf(!defaultSittingConfig.getHand(Hand.OFF_HAND).getFilter().getPresets().isUsable())))),
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

                // delete the old file
                deleteLegacyFile();

                // update and save the new files
                FileData.getServerConfig().copyFileData(serverConfig);
                FileData.getServerConfig().save();

                FileData.getSittingConfig().copyFileData(sittingConfig);
                FileData.getSittingConfig().save();
            } catch (Exception e) {
                Data.LOGGER.error("Error loading legacy config: %s", e.getMessage());
            }
        }
    }
}
