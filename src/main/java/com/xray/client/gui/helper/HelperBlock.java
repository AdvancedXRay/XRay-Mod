package com.xray.client.gui.helper;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Created by MiKeY on 07/07/17.
 */
public class HelperBlock {

    private String name;
    public Block block;
    private ItemStack itemStack;
    private Item item;
    private ResourceLocation resourceName;

    public HelperBlock(String name, Block block, ItemStack itemStack, Item item, ResourceLocation resourceName) {
        this.name = name;
        this.block = block;
        this.itemStack = itemStack;
        this.item = item;
        this.resourceName = resourceName;
    }

    public String getName() {
        return name;
    }

    public Block getBlock() {
        return block;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Item getItem() {
        return item;
    }

    public ResourceLocation getResourceName() {
        return resourceName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
