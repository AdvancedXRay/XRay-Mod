package com.xray.common.reference.block;

import com.xray.common.utils.OutlineColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class BlockData {

    public ResourceLocation name;
    public OutlineColor color;
    public boolean isDefault;
    public IBlockState state;
    public boolean drawing;

    public BlockData(ResourceLocation name, OutlineColor color, boolean isDefault, IBlockState state, boolean draw) {
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
        this.state = state;
        this.drawing = draw;
    }

    public boolean isDrawing() {
        return drawing;
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

