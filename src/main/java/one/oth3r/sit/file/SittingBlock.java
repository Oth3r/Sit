package one.oth3r.sit.file;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SittingBlock extends CustomBlock {
    @SerializedName("sitting-height")
    private Double sittingHeight = 0.5;

    /**
     * gets the sitting height of a block, limiting the size from 0 - 1
     * @return the sitting height, clamped
     */
    public Double getSittingHeight() {
        return Math.max(0f, Math.min(1f, sittingHeight));
    }

    public SittingBlock() {}

    public SittingBlock(ArrayList<String> blockIds, ArrayList<String> blockTags, ArrayList<String> blockStates, Double sittingHeight) {
        super(blockIds, blockTags, blockStates);
        this.sittingHeight = sittingHeight;
    }
}
