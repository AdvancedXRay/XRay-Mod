package com.fgtxray.client.gui;

import com.fgtxray.reference.OreInfo;
import net.minecraft.client.gui.GuiButton;

public class GuiPage
{
    public int page;
    public GuiButton button;
    public OreInfo ore;
    public int x;
    public int y;

    public GuiPage( int x, int y, int page, GuiButton button, OreInfo ore )
    {
        this.page = page;
        this.button = button;
        this.ore = ore;
        this.x = x;
        this.y = y;
    }

    public int getPage()
    {
        return page;
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public GuiButton getButton()
    {
        return button;
    }

    public void setButton(GuiButton button)
    {
        this.button = button;
    }
}
