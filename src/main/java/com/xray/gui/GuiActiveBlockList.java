package com.xray.gui;

import com.xray.XRay;
import com.xray.reference.block.BlockData;
import com.xray.xray.Controller;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

public class GuiActiveBlockList extends GuiScrollingList {

    private static final int HEIGHT = 35;
    private final GuiSelectionScreen parent;
    private ArrayList<BlockData> itemList;

    private int selectedIndex = -1;
    private long lastClick = 0L;

    GuiActiveBlockList(GuiSelectionScreen parent, int x, int y) {
        super(XRay.mc, 204, 210, y, parent.height / 2 + 80, x, HEIGHT, parent.width, parent.height);

        this.parent = parent;
        this.itemList = new ArrayList<>(Controller.getBlockStore().getStore().values());
    }

    @Override
    protected int getSize() {
        return this.itemList.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        if( this.selectedIndex == index )
            this.selectedIndex = -1;
        else
            this.selectedIndex = index;
    }

    private void eventRightClick(int mouseX, int mouseY, float partialTicks) {
        System.out.println("hi");
    }

    @Override
    protected boolean isSelected(int index) {
        return index == this.selectedIndex;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected int getContentHeight() {
        return (this.getSize() * HEIGHT);
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess) {
        BlockData blockData = this.itemList.get(idx);

        FontRenderer font = this.parent.getFontRender();

        font.drawString(blockData.getEntryName(), this.left + 30, top + 7, 0xFFFFFF);
        font.drawString(blockData.isDrawing() ? "Enabled" : "Disabled", this.left + 30, top + 17, 0xD1CFCF);

        RenderHelper.enableGUIStandardItemLighting();
        this.parent.render.renderItemAndEffectIntoGUI(blockData.getItemStack(), this.left + 5, top + 7);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if( Mouse.isButtonDown(1) && System.currentTimeMillis() - this.lastClick > 500L ) {
            this.eventRightClick(mouseX, mouseY, partialTicks);
            this.lastClick = System.currentTimeMillis();
        }
    }
}
