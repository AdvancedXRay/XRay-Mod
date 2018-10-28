package com.xray.client.gui.utils;

import com.xray.common.reference.OreInfo;
import net.minecraft.client.gui.GuiButton;

public class GuiPaged {

    public int id;
    public int x;
    public int y;
    private int pageId;
    private OreInfo ore;
    private GuiButton button;

    public GuiPaged(int id, int pageId, int x, int y, OreInfo ore ) {

        this.id = id;
        this.pageId = pageId;
        this.x = x;
        this.y = y;
        this.ore = ore;
        this.button = new GuiButton(id, x+25, y, 181, 20, ore.getDisplayName() + ": " + (ore.isDrawable() ? "On" : "Off"));

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

    public OreInfo getOre() {
        return ore;
    }

    public GuiButton getButton() {
        return button;
    }

}