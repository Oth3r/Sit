package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Hand;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Utl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class SittingConfig {

    @SerializedName("version")
    private Double version = 1.0;
    @SerializedName("enabled")
    private Boolean enabled = true;
    @SerializedName("hand-sitting")
    private Boolean handSitting = true;
    @SerializedName("main-hand")
    private HandSetting mainHand = new HandSetting(HandSetting.SittingRequirement.EMPTY, new HandSetting.Filter());
    @SerializedName("off-hand")
    private HandSetting offHand = new HandSetting(HandSetting.SittingRequirement.FILTER,
            new HandSetting.Filter(false,true,false,new ArrayList<>(),new ArrayList<>())); // todo fill out some fox examples sake

    public SittingConfig() {}

    public SittingConfig(double version, boolean enabled, boolean handSitting, HandSetting mainHand, HandSetting offHand) {
        this.version = version;
        this.enabled = enabled;
        this.handSitting = handSitting;
        this.mainHand = mainHand;
        this.offHand = offHand;
    }

    public SittingConfig(SittingConfig sittingConfig) {
        this.version = sittingConfig.version;
        this.enabled = sittingConfig.enabled;
        this.handSitting = sittingConfig.handSitting;
        this.mainHand = sittingConfig.mainHand;
        this.offHand = sittingConfig.offHand;
    }

    public Double getVersion() {
        return version;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean canSitWithHand() {
        return handSitting;
    }

    public void setHandSitting(Boolean handSitting) {
        this.handSitting = handSitting;
    }

    public HandSetting getHand(Hand handType) {
        return handType.equals(Hand.MAIN_HAND) ? mainHand : offHand;
    }

    public HandSetting getMainHand() {
        return mainHand;
    }

    public HandSetting getOffHand() {
        return offHand;
    }

    public static File getFile() {
        return new File(Data.CONFIG_DIR+"sitting-config.json");
    }

    /**
     * loads the Config file to Data
     */
    public static void load() {

        File file = getFile();
        if (!file.exists()) save();
        // try reading the file
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            Updater.SittingConfigFile.run(reader);
        } catch (Exception e) {
            Data.LOGGER.error(String.format("ERROR LOADING '%s`: %s", file.getName(),e.getMessage()));
        }
        // save after loading
        save();
    }

    /**
     * saves Data.config to config.json
     */
    public static void save() {
        if (!getFile().exists()) {
            Data.LOGGER.info(String.format("Creating new `%s`", getFile().getName()));
        }
        try (BufferedWriter writer = Files.newBufferedWriter(getFile().toPath(), StandardCharsets.UTF_8)) {
            writer.write(Utl.getGson().toJson(FileData.getSittingConfig()));
        } catch (Exception e) {
            Data.LOGGER.error(String.format("ERROR SAVING '%s`: %s", getFile().getName(), e.getMessage()));
        }
    }
}
