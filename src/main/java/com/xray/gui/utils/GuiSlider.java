package com.xray.gui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.AbstractSlider;

public class GuiSlider extends AbstractSlider
{
    private float maxValue;
    private String label;

    public GuiSlider(int x, int y, String label, float startingValue, float maxValue)
    {
        super(Minecraft.getInstance().gameSettings, x, y, 202, 20, startingValue);
        this.label = label;
        this.maxValue = maxValue;

        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.label + ": " + (int) (this.value * this.maxValue));
    }

    @Override
    protected void applyValue() {}

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}