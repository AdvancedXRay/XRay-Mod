package com.xray.reference.block;

import com.xray.utils.OutlineColor;
import net.minecraft.item.ItemStack;

public class BlockData {

    private String entryKey;
    private String entryName;
    private int stateId;
    private OutlineColor color;
    private ItemStack itemStack;
    private boolean drawing;
    private int order;

    public BlockData(String entryKey, String entryName, int stateId, OutlineColor color, ItemStack itemStack, boolean drawing, int order) {
        this.entryKey = entryKey;
        this.entryName = entryName;
        this.stateId = stateId;
        this.color = color;
        this.itemStack = itemStack;
        this.drawing = drawing;
        this.order = order;
    }

    public String getEntryKey() {
        return entryKey;
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

    public int getOrder() {
        return order;
    }
}

