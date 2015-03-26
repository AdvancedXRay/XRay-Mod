package com.fgtXray.client.gui;

import net.minecraft.client.gui.GuiButton;

public class GuiPage
{
    public int page;
    public GuiButton button;

    public GuiPage( int page, GuiButton button )
    {
        this.page = page;
        this.button = button;
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
