package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Hand;
import one.oth3r.sit.utl.Data;
import one.oth3r.sit.utl.Utl;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class SittingConfig implements CustomFile<SittingConfig> {

    @SerializedName("version")
    private Double version = 1.0;
    @SerializedName("enabled")
    private Boolean enabled = true;
    @SerializedName("hand-sitting")
    private Boolean handSitting = true;
    @SerializedName("main-hand")
    private HandSetting mainHand = FileData.Defaults.MAIN_HAND;
    @SerializedName("off-hand")
    private HandSetting offHand = FileData.Defaults.OFF_HAND;

    public SittingConfig() {}

    public SittingConfig(double version, boolean enabled, boolean handSitting, HandSetting mainHand, HandSetting offHand) {
        this.version = version;
        this.enabled = enabled;
        this.handSitting = handSitting;
        this.mainHand = mainHand;
        this.offHand = offHand;
    }

    public SittingConfig(SittingConfig sittingConfig) {
        updateToNewFile(sittingConfig);
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

    @Override
    public void reset() {
        updateToNewFile(new SittingConfig());
    }

    @Override
    public @NotNull Class<SittingConfig> getFileClass() {
        return SittingConfig.class;
    }

    @Override
    public void updateToNewFile(SittingConfig newFile) {
        this.version = newFile.version;
        this.enabled = newFile.enabled;
        this.handSitting = newFile.handSitting;
        this.mainHand = newFile.mainHand;
        this.offHand = newFile.offHand;
    }

    @Override
    public String getFileName() {
        return "sitting-config.json";
    }

    @Override
    public String getDirectory() {
        return Data.CONFIG_DIR;
    }
}
