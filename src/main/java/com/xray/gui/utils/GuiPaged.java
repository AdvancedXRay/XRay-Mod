package com.xray.gui.utils;

import com.xray.reference.block.BlockData;
import net.minecraft.client.gui.GuiButton;

public class GuiPaged {

    public int id;
    public int x;
    public int y;
    private int pageId;
    private BlockData block;
    private String storeKey;
    private GuiButton button;

    public GuiPaged(int id, int pageId, int x, int y, String storeKey, BlockData block ) {

        this.id = id;
        this.pageId = pageId;
        this.x = x;
        this.y = y;
        this.block = block;
        this.storeKey = storeKey;
        this.button = new GuiButton(id, x+25, y, 181, 20, block.getEntryName() + ": " + (block.isDrawing() ? "On" : "Off"));

    }

    public String getStoreKey() {
        return storeKey;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPageId() {
        return pageId;
    }

    public BlockData getBlock() {
        return block;
    }

    public GuiButton getButton() {
        return button;
    }

}
