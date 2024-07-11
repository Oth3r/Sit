package one.oth3r.sit.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.Hand;
import one.oth3r.sit.Sit;
import one.oth3r.sit.utl.Utl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class Updater {

    public static class HandConfigFile {

        /**
         * runs the updater from the file reader and sets the loaded settings when finished
         * @param reader the file reader
         * @throws NullPointerException if the file is null
         */
        public static void run(BufferedReader reader)
                throws NullPointerException {
            // try to read the json
            HandConfig handConfig;
            try {
                handConfig = Utl.getGson().fromJson(reader, HandConfig.class);
            } catch (Exception e) {
                throw new NullPointerException();
            }

            // throw null if the fileData is null or version is null
            if (handConfig == null) throw new NullPointerException();

            // get the file version
            Double version = handConfig.getVersion();

            // if there's no version, throw
            if (version == null) throw new NullPointerException();

            // update the config (using the non-null version)
            handConfig = update(handConfig);

            // set the config in the mod data
            Data.setHandConfig(handConfig);
        }

        /**
         * updates the file
         */
        public static HandConfig update(HandConfig old) {
            HandConfig serverConfig = new HandConfig(old);
            return serverConfig;
        }
    }

    public static class ServerConfigFile {
        public static final double VERSION = 1.0;

        /**
         * runs the updater from the file reader and sets the loaded settings when finished
         * @param reader the file reader
         * @throws NullPointerException if the file is null
         */
        public static void run(BufferedReader reader)
                throws NullPointerException {
            // try to read the json
            ServerConfig serverConfig;
            try {
                serverConfig = Utl.getGson().fromJson(reader, ServerConfig.class);
            } catch (Exception e) {
                throw new NullPointerException();
            }

            // throw null if the fileData is null or version is null
            if (serverConfig == null) throw new NullPointerException();

            // get the file version
            Double version = serverConfig.getVersion();

            // if there's no version, throw
            if (version == null) throw new NullPointerException();

            // update the config (using the non-null version)
            serverConfig = update(serverConfig);

            // set the config in the mod data
            Data.setServerConfig(serverConfig);
        }

        /**
         * updates the file
         */
        public static ServerConfig update(ServerConfig old) {
            ServerConfig serverConfig = new ServerConfig(old);
            return serverConfig;
        }

        public static class Legacy {

            /**
             * gets the legacy file, from the old directory for fabric, and the same one for spigot
             */
            public static File getLegacyFile() {
                // strip the new directory
                return new File(Sit.CONFIG_DIR.substring(0,Sit.CONFIG_DIR.length()-5)+"Sit!.properties");
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
                    String ver = (String) properties.computeIfAbsent("version", a -> String.valueOf(VERSION));

                    // if the old version system (v1.0) remove "v"
                    if (ver.contains("v")) ver = ver.substring(1);

                    loadVersion(properties,Double.parseDouble(ver));

                } catch (Exception e) {
                    Sit.LOGGER.error("Error loading legacy config file: {}", e.getMessage());
                }

                // delete the old file
                try {
                    Files.delete(file.toPath());
                    Sit.LOGGER.info("Deleted " + file.getName());
                } catch (Exception e) {
                    Sit.LOGGER.error("Failed to delete the old Sit! config.");
                }
            }

            /**
             * gets a list of custom blocks from the legacy way of entering custom sit blocks
             */
            private static ArrayList<CustomBlock> getCustomBlocks(ArrayList<String> fix) {
                //eg. minecraft:campfire|.46|1|lit=false
                ArrayList<CustomBlock> out = new ArrayList<>();
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
                    out.add(new CustomBlock(
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
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                    ServerConfig defaultConfig = new ServerConfig();

                    // load the latest config
                    ServerConfig serverConfig = new ServerConfig(
                            2.0,
                            (String) properties.computeIfAbsent("lang", a -> defaultConfig.getLang()),
                            Boolean.parseBoolean((String) properties.computeIfAbsent("keep-active", a -> String.valueOf(defaultConfig.isKeepActive()))),
                            Boolean.parseBoolean((String) properties.computeIfAbsent("sit-while-seated", a -> String.valueOf(defaultConfig.isSitWhileSeated()))),
                            new ServerConfig.PresetBlocks(
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

                    HandConfig defaultHandConfig = new HandConfig();

                    HandConfig handConfig = null;
                    // * filters are flipped because the way they work are flipped
                    try {
                        handConfig = new HandConfig(
                                1.0, Boolean.parseBoolean((String) properties.computeIfAbsent("hand.sitting", a -> String.valueOf(defaultHandConfig.canSitWithHand()))),
                                new HandSetting(
                                    Utl.Enum.get(properties.computeIfAbsent("hand.main.requirement", a -> String.valueOf(defaultHandConfig.getHand(Hand.MAIN_HAND).getSittingRequirement())),HandSetting.SittingRequirement.class,HandSetting.SittingRequirement.FILTER),
                                        new HandSetting.Filter(
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.block", a -> String.valueOf(!defaultHandConfig.getHand(Hand.MAIN_HAND).getFilter().isBlock()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.food", a -> String.valueOf(!defaultHandConfig.getHand(Hand.MAIN_HAND).getFilter().isFood()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.main.usable", a -> String.valueOf(!defaultHandConfig.getHand(Hand.MAIN_HAND).getFilter().isUsable()))),
                                                getFilterList(
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.main.whitelist", a -> "[]"), listType),
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.main.blacklist", a -> "[]"), listType)
                                                ),
                                                new ArrayList<>()
                                        )
                                ),
                                new HandSetting(
                                        Utl.Enum.get(properties.computeIfAbsent("hand.off.requirement", a -> String.valueOf(defaultHandConfig.getHand(Hand.OFF_HAND).getSittingRequirement())),HandSetting.SittingRequirement.class,HandSetting.SittingRequirement.FILTER),
                                        new HandSetting.Filter(
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.block", a -> String.valueOf(!defaultHandConfig.getHand(Hand.OFF_HAND).getFilter().isBlock()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.food", a -> String.valueOf(!defaultHandConfig.getHand(Hand.OFF_HAND).getFilter().isFood()))),
                                                !Boolean.parseBoolean((String) properties.computeIfAbsent("hand.off.usable", a -> String.valueOf(!defaultHandConfig.getHand(Hand.OFF_HAND).getFilter().isUsable()))),
                                                getFilterList(
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.off.whitelist", a -> "[]"), listType),
                                                        new Gson().fromJson((String) properties.computeIfAbsent("hand.off.blacklist", a -> "[]"), listType)
                                                ),
                                                new ArrayList<>()
                                        )
                                )
                        );
                    } catch (JsonSyntaxException ignored) {}

                    // load an older version
                    if (version == 1.0) {
                        try {
                            handConfig = new HandConfig(
                                    1.0, defaultHandConfig.canSitWithHand(),
                                    new HandSetting(
                                            Utl.Enum.get(properties.computeIfAbsent("main-hand-requirement", a -> String.valueOf(defaultHandConfig.getHand(Hand.MAIN_HAND).getSittingRequirement())),HandSetting.SittingRequirement.class,HandSetting.SittingRequirement.FILTER),
                                            new HandSetting.Filter(
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-block", a -> String.valueOf(!defaultHandConfig.getHand(Hand.MAIN_HAND).getFilter().isBlock()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-food", a -> String.valueOf(!defaultHandConfig.getHand(Hand.MAIN_HAND).getFilter().isFood()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("main-hand-usable", a -> String.valueOf(!defaultHandConfig.getHand(Hand.MAIN_HAND).getFilter().isUsable()))),
                                                    getFilterList(
                                                            new Gson().fromJson((String) properties.computeIfAbsent("main-hand-whitelist", a -> "[]"), listType),
                                                            new Gson().fromJson((String) properties.computeIfAbsent("main-hand-blacklist", a -> "[]"), listType)
                                                    ),
                                                    new ArrayList<>()
                                            )
                                    ),
                                    new HandSetting(
                                            Utl.Enum.get(properties.computeIfAbsent("off-hand-requirement", a -> String.valueOf(defaultHandConfig.getHand(Hand.OFF_HAND).getSittingRequirement())),HandSetting.SittingRequirement.class,HandSetting.SittingRequirement.FILTER),
                                            new HandSetting.Filter(
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-block", a -> String.valueOf(!defaultHandConfig.getHand(Hand.OFF_HAND).getFilter().isBlock()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-food", a -> String.valueOf(!defaultHandConfig.getHand(Hand.OFF_HAND).getFilter().isFood()))),
                                                    !Boolean.parseBoolean((String) properties.computeIfAbsent("off-hand-usable", a -> String.valueOf(!defaultHandConfig.getHand(Hand.OFF_HAND).getFilter().isUsable()))),
                                                    getFilterList(
                                                            new Gson().fromJson((String) properties.computeIfAbsent("off-hand-whitelist", a -> "[]"), listType),
                                                            new Gson().fromJson((String) properties.computeIfAbsent("off-hand-blacklist", a -> "[]"), listType)
                                                    ),
                                                    new ArrayList<>()
                                            )
                                    )
                            );
                        } catch (JsonSyntaxException ignored) {}
                    }

                    Data.setServerConfig(serverConfig);
                    Data.setHandConfig(handConfig);
                    ServerConfig.save();
                    HandConfig.save();
                } catch (Exception e) {
                    Sit.LOGGER.error("Error loading legacy config: {}", e.getMessage());
                }
            }
        }
    }
}
