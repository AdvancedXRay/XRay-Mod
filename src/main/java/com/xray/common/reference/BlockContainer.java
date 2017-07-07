package com.xray.common.reference;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Created by MiKeY on 07/07/17.
 */
public class BlockContainer {

    public String name;
    public Block block;
    public ItemStack itemStack;
    public Item item;
    public ResourceLocation resourceName;

    public BlockContainer(String name, Block block, ItemStack itemStack, Item item, ResourceLocation resourceName) {
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
}
