package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Objects;
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

    public HandSetting(HandSetting handSetting) {
        this.sittingRequirement = handSetting.sittingRequirement;
        this.filter = new Filter(handSetting.filter);
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

        public Filter(Filter filter) {
            this.invert = filter.invert;
            this.presets = new Presets(filter.presets);
            this.customItems = new CustomItem(filter.customItems);
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

            public Presets(Presets presets) {
                this.block = presets.block;
                this.food = presets.food;
                this.usable = presets.usable;
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

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;
                Presets presets = (Presets) o;
                return block == presets.block && food == presets.food && usable == presets.usable;
            }

            @Override
            public int hashCode() {
                return Objects.hash(block, food, usable);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Filter filter = (Filter) o;
            return Objects.equals(invert, filter.invert) && Objects.equals(presets, filter.presets) && Objects.equals(customItems, filter.customItems);
        }

        @Override
        public int hashCode() {
            return Objects.hash(invert, presets, customItems);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HandSetting that = (HandSetting) o;
        return sittingRequirement == that.sittingRequirement && Objects.equals(sittingRequirementOptions, that.sittingRequirementOptions) && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sittingRequirement, sittingRequirementOptions, filter);
    }
}
