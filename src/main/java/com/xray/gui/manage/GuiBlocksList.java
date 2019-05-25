package com.xray.gui.manage;

import com.xray.reference.block.BlockItem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.List;
import java.util.Objects;

public class GuiBlocksList extends GuiScrollingList {

    private static final int HEIGHT = 35;
    private final GuiBlockListScrollable parent;
    private List<BlockItem> blockList;

    GuiBlocksList(GuiBlockListScrollable parent, List<BlockItem> blockList) {
        super( parent.getMinecraftInstance(), 202, 210, parent.height / 2 - 105, parent.height / 2 + 80, parent.width / 2 - 100, HEIGHT, parent.width, parent.height);

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
    protected int getContentHeight() {
        return (this.getSize() * HEIGHT);
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess) {
        BlockItem block = blockList.get( idx );

        FontRenderer font = this.parent.getFontRender();

        font.drawString(block.getItemStack().getDisplayName(), this.left + 30 , top +  7, 0xFFFFFF);
        font.drawString(Objects.requireNonNull(block.getItemStack().getItem().getRegistryName()).getResourceDomain(), this.left + 30 , top + 17, 0xD1CFCF);

        RenderHelper.enableGUIStandardItemLighting();
        this.parent.getRender().renderItemAndEffectIntoGUI(block.getItemStack(), this.left + 5, top+7);
        RenderHelper.disableStandardItemLighting();
    }

    void updateBlockList(List<BlockItem> blocks) {
	    blockList = blocks;
    }
}
