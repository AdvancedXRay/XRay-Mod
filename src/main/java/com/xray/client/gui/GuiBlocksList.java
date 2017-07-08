package com.xray.client.gui;

import com.xray.client.gui.helper.HelperBlock;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MiKeY on 07/07/17.
 */
public class GuiBlocksList extends GuiScrollingList {

    private static final int HEIGHT = 35;
    private GuiBlocks parent;
    private List<HelperBlock> blockList;

    GuiBlocksList(GuiBlocks parent, List<HelperBlock> blockList) {
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
        this.parent.selectBlock( index );
    }

    @Override
    protected boolean isSelected(int index) {
        return parent.blockSelected( index );
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected int getContentHeight()
    {
        return (this.getSize() * HEIGHT);
    }

    void updateBlockList(ArrayList<HelperBlock> blockList) {
        this.blockList = blockList;
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess) {
        HelperBlock block = blockList.get( idx );
        FontRenderer font = this.parent.getFontRender();

        font.drawString(block.getName(), this.left + 30 , top +  7, 0xFFFFFF);
        font.drawString(block.getResourceName().getResourceDomain(), this.left + 30 , top + 17, 0xD1CFCF);

        if( block.getItemStack() != null ) {
            RenderHelper.enableGUIStandardItemLighting();
            this.parent.getRender().renderItemAndEffectIntoGUI(block.getItemStack(), this.left + 5, top+7);
            RenderHelper.disableStandardItemLighting();
        }
    }
}
