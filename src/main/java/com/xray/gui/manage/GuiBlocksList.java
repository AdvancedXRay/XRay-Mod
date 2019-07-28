package com.xray.gui.manage;

import com.xray.reference.block.BlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.LanguageScreen;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.util.List;

public class GuiBlocksList  {
    private static final int HEIGHT = 1;

    //
//    private static final int HEIGHT = 35;
//    private final GuiBlockListScrollable parent;
//    private List<BlockItem> blockList;
////
//    GuiBlocksList(GuiBlockListScrollable parent, List<BlockItem> blockList) {
////        super(Minecraft.getInstance(), 202, 210, parent.height / 2 - 105, parent.height / 2 + 80, HEIGHT);
//
////        this.parent = parent;
////        this.blockList = blockList;
//    }

//    @Override
//    protected int getSize() {
//        return this.blockList.size();
//    }
//
//    @Override
//    protected void elementClicked(int index, boolean doubleClick) {
//        this.parent.selectBlock( index );
//    }
//
//    @Override
//    protected boolean isSelected(int index) {
//        return parent.blockSelected( index );
//    }
//
//    @Override
//    protected void drawBackground() {
//    }
//
//    @Override
//    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {
//        BlockItem block = blockList.get( idx );
//
//        FontRenderer font = this.parent.getFontRender();
//
//        font.drawString(block.getItemStack().getDisplayName().getFormattedText(), this.left + 30 , top +  7, 0xFFFFFF);
//        font.drawString(Objects.requireNonNull(block.getItemStack().getItem().getRegistryName()).getNamespace(), this.left + 30 , top + 17, 0xD1CFCF);
//
//    }
//
//
//
//    @Override
//    protected int getContentHeight() {
//        return (this.getSize() * HEIGHT);
//    }
//
//    void updateBlockList(List<BlockItem> blocks) {
//	    blockList = blocks;
//    }
//
//    @Override
//    public Object get(int index) {
//        return this.blockList.get(index);
//    }
//
//    @Override
//    public int size() {
//        return this.blockList.size();
//    }
}
