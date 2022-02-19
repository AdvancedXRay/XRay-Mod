package pro.mikey.xray.utils;

import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class BlockData {

    private String entryName;
    private String blockName;
    private int color;
    private ItemStack itemStack;
    private boolean drawing;
    private int order;

    public BlockData(String entryName, String blockName, int color, ItemStack itemStack, boolean drawing, int order) {
        this.entryName = entryName;
        this.blockName = blockName;
        this.color = color;
        this.itemStack = itemStack;
        this.drawing = drawing;
        this.order = order;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getBlockName() {
        return blockName;
    }

    public int getColor() {
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

    public void setColor(int color) {
        this.color = color;
    }

    public int getOrder() {
        return order;
    }

    // It's pretty annoying to serialize an ItemStack so we dont :D
    public static class SerializableBlockData {

        private String name;
        private String blockName;
        private int order;

        private int color;
        private boolean drawing;

        public SerializableBlockData(String name, String blockName, int color, boolean drawing, int order) {
            this.name = name;
            this.blockName = blockName;
            this.color = color;
            this.drawing = drawing;
            this.order = order;
        }

        public String getName() {
            return name;
        }

        public String getBlockName() {
            return blockName;
        }

        public int getColor() {
            return color;
        }

        public boolean isDrawing() {
            return drawing;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }
}

