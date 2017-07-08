package com.xray.client.gui.helper;

import com.xray.common.reference.OreInfo;
import net.minecraft.client.gui.GuiButton;

/**
 * Created by MiKeY on 08/07/17.
 */
public class HelperGuiList {

    public int id;
    public int x;
    public int y;
    private int pageId;
    public OreInfo ore;
    private GuiButton button;

    public HelperGuiList(int id, int pageId, int x, int y, OreInfo ore ) {

        this.id = id;
        this.pageId = pageId;
        this.x = x;
        this.y = y;
        this.ore = ore;
        this.button = new GuiButton(id, x+25, y, 181, 20, ore.oreName + ": " + (ore.draw ? "On" : "Off"));

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
