package one.oth3r.sit.file;

import net.minecraft.server.network.ServerPlayerEntity;
import one.oth3r.otterlib.file.LanguageReader;
import one.oth3r.sit.Sit;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Utl;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FileData {
    /**
     * Sit! config file
     */
    private static ServerConfig serverConfig = new ServerConfig();

    public static ServerConfig getServerConfig() {
        return serverConfig;
    }

    public static void setServerConfig(ServerConfig newServerConfig) {
        serverConfig = new ServerConfig(newServerConfig);
    }

    /**
     * The default sitting config for all new players
     */
    private static SittingConfig sittingConfig = new SittingConfig();

    public static SittingConfig getSittingConfig() {
        return sittingConfig;
    }

    public static void setSittingConfig(SittingConfig newSittingConfig) {
        sittingConfig = new SittingConfig(newSittingConfig);
    }

    /**
     * the sitting config stored per player on the server
     */
    private static final HashMap<ServerPlayerEntity, SittingConfig> playerSettings = new HashMap<>();

    public static void clearPlayerSettings() {
        playerSettings.clear();
    }

    public static void setPlayerSetting(ServerPlayerEntity player, SittingConfig config) {
        playerSettings.put(player, config);
    }

    public static void removePlayerSetting(ServerPlayerEntity player) {
        playerSettings.remove(player);
    }

    public static SittingConfig getPlayerSetting(ServerPlayerEntity player) {
        return playerSettings.getOrDefault(player, sittingConfig);
    }

    /// the language / text system for the mod
    private static final LanguageReader langReader = new LanguageReader(
            getLangPath(),Path.of(Data.CONFIG_DIR),"en_us","en_us");

    public static LanguageReader getLangReader() {
        return langReader;
    }

    /**
     * loads all config files to memory
     */
    public static void loadFiles() {
        getServerConfig().load();
        // load the language reader
        langReader.updateLanguage(getServerConfig().getLang());

        getSittingConfig().load();
        // if loading file and is on supported server on client, send the new settings over
        if (Data.isClient() && Data.isSupportedServer()) {
            Utl.sendSettingsPackets();
        }
    }

    /**
     * saves all config files
     */
    public static void saveFiles() {
        getSittingConfig().save();
        getServerConfig().save();
    }

    private static Path getLangPath() {
        ClassLoader classLoader = Sit.class.getClassLoader();
        URL resource = classLoader.getResource("assets/sit-oth3r/lang/");
        if (resource == null) {
            throw new RuntimeException("Language file not found.");
        }

        try {
            return Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Defaults {
        public static final ArrayList<SittingBlock> SITTING_BLOCKS = new ArrayList<>(Arrays.asList(
                new SittingBlock(new ArrayList<>(),new ArrayList<>(Arrays.asList("#minecraft:campfires")), new ArrayList<>(Arrays.asList("lit=false")),.437),
                new SittingBlock(new ArrayList<>(Arrays.asList("!minecraft:crimson_stem","!minecraft:warped_stem","minecraft:polished_basalt")), new ArrayList<>(Arrays.asList("#minecraft:logs","!#minecraft:oak_logs")), new ArrayList<>(Arrays.asList("!axis=y")),1.0),
                new SittingBlock(new ArrayList<>(Arrays.asList()), new ArrayList<>(Arrays.asList("#minecraft:beds")), new ArrayList<>(Arrays.asList("part=foot","occupied=false")),.5625)
        ));

        public static final ArrayList<CustomBlock> BLACKLISTED_BLOCKS = new ArrayList<>(Arrays.asList(
                new CustomBlock(new ArrayList<>(),new ArrayList<>(Arrays.asList("#minecraft:shulker_boxes")),new ArrayList<>())
        ));

        public static final ArrayList<CustomBlock> INTERACTION_BLOCKS = new ArrayList<>(Arrays.asList(
                new CustomBlock(new ArrayList<>(Arrays.asList(
                        "minecraft:crafter","minecraft:repeating_command_block","minecraft:chain_command_block","minecraft:command_block")),
                        new ArrayList<>(Arrays.asList(
                        "#minecraft:shulker_boxes","#c:player_workstations/furnaces","#c:player_workstations/crafting_tables",
                        "#c:villager_job_sites","#minecraft:trapdoors","#c:chests")),
                        new ArrayList<>())
        ));

        public static final HandSetting MAIN_HAND = new HandSetting(HandSetting.SittingRequirement.EMPTY, new HandSetting.Filter(
                false,new HandSetting.Filter.Presets(),
                new CustomItem(
                        new ArrayList<>(),
                        new ArrayList<>(Arrays.asList("#minecraft:bookshelf_books","!#minecraft:lectern_books")))));

        public static final HandSetting OFF_HAND = new HandSetting(HandSetting.SittingRequirement.FILTER, new HandSetting.Filter(
                false, new HandSetting.Filter.Presets(false, true, false),
                new CustomItem(new ArrayList<>(Arrays.asList("minecraft:filled_map",
                        "minecraft:torch", "minecraft:soul_torch","minecraft:redstone_torch",
                        "minecraft:lantern", "minecraft:soul_lantern")),
                        new ArrayList<>())));
    }
}
