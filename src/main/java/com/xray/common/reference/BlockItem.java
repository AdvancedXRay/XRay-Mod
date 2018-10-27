package com.xray.common.reference;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class BlockItem {

    private ResourceLocation name;
    private int stateId;
    private Block block;

    public BlockItem(ResourceLocation name, int stateId, Block block) {
        this.name = name;
        this.stateId = stateId;
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public ResourceLocation getName() {
        return name;
    }

    public int getStateId() {
        return stateId;
    }
}
