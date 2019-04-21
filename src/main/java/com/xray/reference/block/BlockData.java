package com.xray.reference.block;

import com.xray.utils.OutlineColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BlockData {

    private String entryName;
    public OutlineColor color;
    private IBlockState state;
    private ItemStack itemStack;
    private boolean drawing;

    public BlockData(String entryName, OutlineColor color, ItemStack itemStack, boolean drawing) {
        this.entryName = entryName;
        this.color = color;
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

    public OutlineColor getOutline() {
        return color;
    }

    public void setDrawing(boolean drawing) {
        this.drawing = drawing;
    }
}

