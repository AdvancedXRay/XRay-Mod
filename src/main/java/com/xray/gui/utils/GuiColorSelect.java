package com.xray.gui.utils;

import com.xray.reference.Reference;
import com.xray.utils.OutlineColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiColorSelect extends Gui {

    private int x;
    private int y;

    private int width = 15;
    private int height = 10;

    private String name;
    private OutlineColor color;

    private static ResourceLocation color_bg = new ResourceLocation(Reference.PREFIX_GUI + "color-bg.png");

    private static Minecraft mc = Minecraft.getMinecraft();

    public GuiColorSelect(String name, OutlineColor color, int x, int y) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.color = color;
    }

    public void drawSelect() {
        mc.renderEngine.bindTexture(color_bg);

        GlStateManager.color((float)this.color.getRed() / 255, (float)this.color.getGreen() / 255, (float)this.color.getBlue() / 255, 255f);
        drawModalRectWithCustomSizedTexture(this.x, this.y, 0f, 0f, 15, 10, this.width, this.height);
    }

    public boolean isMouseOver(int x, int y) {
        return  x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
    }

    public String getName() {
        return name;
    }

    public OutlineColor getColor() {
        return color;
    }
}
