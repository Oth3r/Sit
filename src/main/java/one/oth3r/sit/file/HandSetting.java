package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HandSetting {

    @SerializedName("requirement")
    private SittingRequirement sittingRequirement = SittingRequirement.NONE;
    @SerializedName("requirement-options") @SuppressWarnings("unused")
    private final String sittingRequirementOptions = Arrays.stream(SittingRequirement.values()).map(Enum::toString).collect(Collectors.joining(", "));
    @SerializedName("filter")
    private Filter filter = new Filter();

    public HandSetting() {}

    public HandSetting(SittingRequirement sittingRequirement, Filter filter) {
        this.sittingRequirement = sittingRequirement;
        this.filter = filter;
    }

    public SittingRequirement getSittingRequirement() {
        return sittingRequirement;
    }

    public Filter getFilter() {
        return filter;
    }

    public enum SittingRequirement {
        NONE,
        FILTER,
        EMPTY
    }

    public static class Filter {

        @SerializedName("invert-filter")
        private Boolean invert = false;
        @SerializedName("presets")
        private Presets presets = new Presets();
        @SerializedName("custom-items")
        private CustomItem customItems = new CustomItem();

        public Filter() {}

        public Filter(boolean invert, Presets presets, CustomItem customItems) {
            this.invert = invert;
            this.presets = presets;
            this.customItems = customItems;
        }

        public Boolean isInverted() {
            return invert;
        }

        public Presets getPresets() {
            return presets;
        }

        public CustomItem getCustomItems() {
            return customItems;
        }

        public static class Presets {
            @SerializedName("block")
            private boolean block = false;
            @SerializedName("food")
            private boolean food = false;
            @SerializedName("usable")
            private boolean usable = false;

            public Presets() {}

            public Presets(boolean block, boolean food, boolean usable) {
                this.block = block;
                this.food = food;
                this.usable = usable;
            }

            public boolean isBlock() {
                return block;
            }

            public boolean isFood() {
                return food;
            }

            public boolean isUsable() {
                return usable;
            }
        }
    }
}
