package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HandSetting {

    @SerializedName("requirement")
    private SittingRequirement sittingRequirement = SittingRequirement.NONE;
    @SerializedName("requirement-options")
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

        @SerializedName("block")
        private boolean block = false;
        @SerializedName("food")
        private boolean food = false;
        @SerializedName("usable")
        private boolean usable = false;
        @SerializedName("custom-items")
        private CustomItem customItems = new CustomItem();

        public Filter() {}

        public Filter(boolean block, boolean food, boolean usable, CustomItem customItems) {
            this.block = block;
            this.food = food;
            this.usable = usable;
            this.customItems = customItems;
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

        public CustomItem getCustomItems() {
            return customItems;
        }
    }
}
