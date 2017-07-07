package com.xray.client.gui;

import com.xray.common.reference.OreInfo;
import net.minecraft.client.gui.GuiButton;

/**
 * Created by MiKeY on 05/07/17.
 */
public class GuiList {
    public int x;
    public int y;
    public OreInfo ore;
    public GuiButton button;
    public GuiButton delete;

    public GuiList( int x, int y, OreInfo ore, GuiButton button, GuiButton delete ) {

        this.x = x;
        this.y = y;
        this.ore = ore;
        this.button = button;
        this.delete = delete;

    }
}
