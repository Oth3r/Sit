package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.State;
import net.minecraft.util.Identifier;
import one.oth3r.sit.utl.Utl;

import java.util.ArrayList;
import java.util.Objects;

public class CustomBlock {

    @SerializedName("block-ids")
    private ArrayList<String> blockIds = new ArrayList<>();
    @SerializedName("block-tags")
    private ArrayList<String> blockTags = new ArrayList<>();
    @SerializedName("blockstates")
    private ArrayList<String> blockStates = new ArrayList<>();


    public CustomBlock() {}

    public CustomBlock(ArrayList<String> blockIds, ArrayList<String> blockTags, ArrayList<String> blockStates) {
        this.blockIds = blockIds;
        this.blockTags = blockTags;
        this.blockStates = blockStates;
    }

    public CustomBlock(CustomBlock customBlock) {
        this.blockIds = new ArrayList<>(customBlock.blockIds);
        this.blockTags = new ArrayList<>(customBlock.blockTags);
        this.blockStates = new ArrayList<>(customBlock.blockStates);
    }

    public ArrayList<String> getBlockIds() {
        return blockIds;
    }

    public ArrayList<String> getBlockTags() {
        return blockTags;
    }

    public ArrayList<String> getBlockStates() {
        return blockStates;
    }



    /**
     * checks if the blockstate matches the CustomBlock params
     * @param blockState the blockState to check
     * @return if the blockstate is allowed by the CustomBlock rules
     */
    public boolean isValid(BlockState blockState) {
        boolean blockType = checkBlockType(blockState);
        if (!blockType) return false;

        /// BLOCK STATE CHECKER
        // now check if the state is one of the acceptable states
        for (String state : blockStates) {
            // if there is a NOT (!) blockstate
            if (state.startsWith("!")) {
                // if it is contained in the block, return false
                // remove the '!'
                String fixedState = state.substring(1);
                if (blockState.getEntries().entrySet().stream().map(State.PROPERTY_MAP_PRINTER).anyMatch(s -> s.equals(fixedState))) return false;
            }
            // else check if the blockstate matches, if not return false
            else if (blockState.getEntries().entrySet().stream().map(State.PROPERTY_MAP_PRINTER).noneMatch(s -> s.equals(state))) return false;
        }

        // if here, all passes have passed
        return true;
    }

    /**
     * returns if the block is the correct type or not
     * @param blockState the blockstate to check
     * @return if the type of block is matching the CustomBlock rules (e.g. if it is wood, ect.)
     */
    private boolean checkBlockType(BlockState blockState) {
        // for all the entered blocks
        for (String id : blockIds) {
            // if there is a match for the NOT(!) blocks, return false immediately
            if (id.startsWith("!") && id.substring(1).equals(Utl.getBlockID(blockState))) return false;
            // if there is a match for the block, return true immediately
            if (id.equalsIgnoreCase(Utl.getBlockID(blockState))) return true;
        }

        // a boolean to check if one of the blocks are in a filtered tag
        // & a switch for if there is only not(!) tags
        boolean tagCheck = false, hasPositiveTags = false;

        for (String tag : blockTags) {
            // substring to remove # and if needed, !
            if (tag.startsWith("!")) {
                // if there is a match for the NOT(!) tag, return false
                Identifier id = Identifier.tryParse(tag.substring(2));
                if (id != null && blockState.isIn(TagKey.of(Registries.BLOCK.getKey(), id))) return false;
            } else {
                // flip the hasPositiveTags boolean
                hasPositiveTags = true;
                // if there is a match, return true
                Identifier id = Identifier.tryParse(tag.substring(1));
                if (id != null && blockState.isIn(TagKey.of(Registries.BLOCK.getKey(), id))) tagCheck = true;
            }
        }

        // if there were any required tags, return whether we matched one
        // if there were only not(!) tags, and we didn't violate any, return true
        return hasPositiveTags? tagCheck : true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CustomBlock that = (CustomBlock) o;
        return Objects.equals(blockIds, that.blockIds) && Objects.equals(blockTags, that.blockTags) && Objects.equals(blockStates, that.blockStates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockIds, blockTags, blockStates);
    }
}
