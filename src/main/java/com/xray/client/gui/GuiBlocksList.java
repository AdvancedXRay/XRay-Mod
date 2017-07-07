package com.xray.client.gui;

import com.xray.common.reference.BlockContainer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.ArrayList;

/**
 * Created by MiKeY on 07/07/17.
 */
public class GuiBlocksList extends GuiScrollingList {

    private static final int HEIGHT = 35;
    private GuiBlocks parent;
    private ArrayList<BlockContainer> blockList;

    GuiBlocksList(GuiBlocks parent, ArrayList<BlockContainer> blockList) {
        super( parent.getMinecraftInstance(), 200, 210, parent.height / 2 - 105, parent.height / 2 + 80, parent.width / 2 - 97, HEIGHT, parent.width, parent.height);

        this.parent = parent;
        this.blockList = blockList;
    }

    @Override
    protected int getSize() {
        return this.blockList.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {

    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {
//        this.parent.drawDefaultBackground();
    }

    @Override
    protected int getContentHeight()
    {
        return (this.getSize() * HEIGHT);
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess) {
        BlockContainer block = blockList.get( idx );
        FontRenderer font = this.parent.getFontRender();

        font.drawString(block.getName(), this.left + 30 , top +  2, 0xFFFFFF);
        font.drawString(block.getResourceName().getResourceDomain(), this.left + 30 , top + 12, 0xD1CFCF);

        if( block.getItemStack() != null ) {
            RenderHelper.enableGUIStandardItemLighting();
            this.parent.getRender().renderItemAndEffectIntoGUI(block.getItemStack(), this.left + 5, top);
            RenderHelper.disableStandardItemLighting();
        }
    }
}
