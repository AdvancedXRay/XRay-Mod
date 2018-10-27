package com.xray.common.reference;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class BlockData {

    public ResourceLocation name;
    public OutlineColor color;
    public boolean isDefault;
    public IBlockState state;

    public BlockData(ResourceLocation name, OutlineColor color, boolean isDefault, IBlockState state) {
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
        this.state = state;
    }

    public ResourceLocation getName() {
        return name;
    }

    public OutlineColor getOutline() {
        return color;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public IBlockState getState() {
        return state;
    }
}

