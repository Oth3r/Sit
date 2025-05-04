package one.oth3r.sit.file;

import com.google.common.base.Objects;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Hand;
import one.oth3r.otterlib.file.CustomFile;
import one.oth3r.otterlib.file.FileSettings;
import one.oth3r.sit.utl.Data;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

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
         copyFileData(sittingConfig);
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
    public FileSettings getFileSettings() {
        return new FileSettings();
    }

    @Override
    public Path getFilePath() {
        return Paths.get(Data.CONFIG_DIR, "sitting-config.json");
    }

    @Override
    public void reset() {
        copyFileData(new SittingConfig());
    }

    @Override
    public @NotNull Class<SittingConfig> getFileClass() {
        return SittingConfig.class;
    }

    @Override
    public void copyFileData(SittingConfig sittingConfig) {
        this.version = sittingConfig.version;
        this.enabled = sittingConfig.enabled;
        this.handSitting = sittingConfig.handSitting;
        this.mainHand = new HandSetting(sittingConfig.mainHand);
        this.offHand = new HandSetting(sittingConfig.offHand);
    }

    @Override
    public void update(JsonElement jsonElement) {

    }

    @Override
    public SittingConfig clone() {
        SittingConfig clone = new SittingConfig();
        clone.copyFileData(this);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SittingConfig that = (SittingConfig) o;
        return Objects.equal(version, that.version) && Objects.equal(enabled, that.enabled) && Objects.equal(handSitting, that.handSitting) && Objects.equal(mainHand, that.mainHand) && Objects.equal(offHand, that.offHand);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(version, enabled, handSitting, mainHand, offHand);
    }
}
