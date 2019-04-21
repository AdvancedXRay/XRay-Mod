package com.xray.common.reference.block;

import com.xray.common.utils.OutlineColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BlockData {

    public ResourceLocation name;
    private String entryName;
    public OutlineColor color;
    public boolean isDefault;
    public IBlockState state;
    public ItemStack itemStack;
    public boolean drawing;

    public BlockData(ResourceLocation name, String entryName, OutlineColor color, boolean isDefault, IBlockState state, ItemStack itemStack, boolean drawing) {
        this.name = name;
        this.entryName = entryName;
        this.color = color;
        this.isDefault = isDefault;
        this.state = state;
        this.itemStack = itemStack;
        this.drawing = drawing;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
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

