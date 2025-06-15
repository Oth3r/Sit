package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Objects;

public class CustomItem {
    @SerializedName("item-ids")
    private ArrayList<String> itemIDs = new ArrayList<>();
    @SerializedName("item-tags")
    private ArrayList<String> itemTags = new ArrayList<>();

    public CustomItem() {}

    public CustomItem(ArrayList<String> itemIDs, ArrayList<String> itemTags) {
        this.itemIDs = itemIDs;
        this.itemTags = itemTags;
    }

    public CustomItem(CustomItem customItem) {
        this.itemIDs = new ArrayList<>(customItem.itemIDs);
        this.itemTags = new ArrayList<>(customItem.itemTags);
    }

    public ArrayList<String> getItemIDs() {
        return itemIDs;
    }

    public ArrayList<String> getItemTags() {
        return itemTags;
    }

    /**
     * returns if the block is the correct type or not
     * @param itemStack the blockstate to check
     * @return if the type of block is matching the CustomBlock rules (e.g. if it is wood, ect.)
     */
    public boolean checkItem(ItemStack itemStack) {
        String itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
        // check the custom item ids
        for (String id : itemIDs) {
            // if there is a match for the NOT(!) item, its filtered, false
            if (id.startsWith("!") && id.substring(1).equalsIgnoreCase(itemId)) return false;
            // if there is a match for the item, return true immediately
            if (id.equalsIgnoreCase(itemId)) return true;
        }

        // a boolean to check if one of the items are in a filtered tag
        // & a switch for if there is only not(!) tags
        boolean tagCheck = false, hasPositiveTags = false;

        for (String tag : itemTags) {
            // substring to remove # and if needed, "!"
            if (tag.startsWith("!")) {
                // if there is a math for the NOT(!) tag, return false
                Identifier id = Identifier.tryParse(tag.substring(2));
                if (id != null && itemStack.isIn(TagKey.of(Registries.ITEM.getKey(), id))) return false;
            } else {
                // flip the hasPositiveTags boolean
                hasPositiveTags = true;
                // else (normal tag), if there is a match, set tagCheck to true
                Identifier id = Identifier.tryParse(tag.substring(1));
                if (id != null && itemStack.isIn(TagKey.of(Registries.ITEM.getKey(), id))) tagCheck = true;
            }
        }

        // if there were any required tags, return whether we matched one
        // if there were only not(!) tags, and we didn't violate any, return true
        return hasPositiveTags? tagCheck : true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomItem that = (CustomItem) o;
        return Objects.equals(itemIDs, that.itemIDs) && Objects.equals(itemTags, that.itemTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemIDs, itemTags);
    }
}
