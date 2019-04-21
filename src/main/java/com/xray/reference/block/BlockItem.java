package com.xray.reference.block;

import net.minecraft.item.ItemStack;

public class BlockItem {

    private int stateId;
    private ItemStack item;

    public BlockItem(int stateId, ItemStack item) {
        this.stateId = stateId;
        this.item = item;
    }

    public int getStateId() {
        return stateId;
    }

    public ItemStack getItemStack() {
        return item;
    }
}
