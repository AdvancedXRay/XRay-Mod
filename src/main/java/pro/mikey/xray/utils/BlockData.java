package pro.mikey.xray.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import pro.mikey.xray.XRay;

/**
 * Block Data with additional in game engine information
 */
public class BlockData extends SerializableBlockData{
    private Block block;
    private ItemStack itemStack;

    public BlockData(String entryName, String blockName, int color, boolean drawing, int order) {
        super(entryName, blockName, color, drawing, order);
        try {
            this.block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(blockName.split(":")[0], blockName.split(":")[1]));
        } catch (Exception e){
            XRay.logger.error("Failed to fetch block entity from item ID : {}", blockName);
        }

        this.itemStack = new ItemStack(this.block);
    }

    public BlockData(String entryName, String blockName, int color, ItemStack itemStack, boolean drawing, int order) {
        super(entryName, blockName, color, drawing, order);
        this.itemStack = itemStack;
        try {
            this.block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(blockName.split(":")[0], blockName.split(":")[1]));
        } catch (Exception e){
            XRay.logger.error("Failed to fetch block entity from item ID : {}", blockName);
        }
    }

    public Block getBlock(){
        return block;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}

