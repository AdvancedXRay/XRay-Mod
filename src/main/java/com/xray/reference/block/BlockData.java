package com.xray.reference.block;

import com.xray.utils.OutlineColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BlockData {

    private String entryName;
    private int stateId;
    private OutlineColor color;
    private ItemStack itemStack;
    private boolean drawing;

    public BlockData(String entryName, int stateId, OutlineColor color, ItemStack itemStack, boolean drawing) {
        this.entryName = entryName;
        this.stateId = stateId;
        this.color = color;
        this.itemStack = itemStack;
        this.drawing = drawing;
    }

    public String getEntryName() {
        return entryName;
    }

    public int getStateId() {
        return stateId;
    }

    public OutlineColor getColor() {
        return color;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void setDrawing(boolean drawing) {
        this.drawing = drawing;
    }

    public void setColor(OutlineColor color) {
        this.color = color;
    }
}

