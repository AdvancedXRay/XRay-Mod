package com.xray.gui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

public class GuiSlider extends GuiButton
{
    public float sliderValue;
    private float sliderMaxValue;
    private boolean dragging = false;
    private String label;

    public GuiSlider(int id, int x, int y, String label, float startingValue, float maxValue)
    {
        super(id, x, y, 202, 20, label);
        this.label = label;
        this.sliderValue = startingValue;
        this.sliderMaxValue = maxValue;
    }

    public int getHoverState(boolean par1)
    {
        return 0;
    }

    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.dragging)
            updateValue(par2);

        this.displayString = label + ": " + (int) (sliderValue * sliderMaxValue);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)), this.y, 0, 66, 4, 20);
        this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
    }

    private void updateValue(int value) {
        this.sliderValue = (float) (value - (this.x + 4)) / (float) (this.width - 8);
        if (this.sliderValue < 0.0F)
            this.sliderValue = 0.0F;

        if (this.sliderValue > 1.0F)
            this.sliderValue = 1.0F;
    }

    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
    {
        if (super.mousePressed(par1Minecraft, par2, par3))
        {
            updateValue(par2);

            this.dragging = true;
            return true;
        }
        else
            return false;
    }

    public void mouseReleased(int par1, int par2)
    {
        this.dragging = false;
    }
}