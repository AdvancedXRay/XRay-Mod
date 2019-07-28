package com.xray.gui;

import com.xray.XRay;
import com.xray.gui.manage.GuiEdit;
import com.xray.gui.utils.GuiBase;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockData;
import com.xray.xray.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class GuiActiveBlockList extends GuiScrollingList {
    private static final ResourceLocation circle = new ResourceLocation(Reference.PREFIX_GUI + "circle.png");

    private static final int HEIGHT = 35;
    private final GuiSelectionScreen parent;
    private ArrayList<BlockData> itemList;

    GuiActiveBlockList(GuiSelectionScreen parent, int x, int y, ArrayList<BlockData> itemList) {
        super(XRay.mc, 204, 250, y, parent.height / 2 + 100, x, HEIGHT, parent.width, parent.height);

        this.parent = parent;
        this.itemList = itemList;
        this.itemList.sort(Comparator.comparingInt(BlockData::getOrder));
    }

    @Override
    protected int getSize() {
        return this.itemList.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        BlockData data = this.itemList.get(index);
        if( GuiEdit.isShiftKeyDown() ) {
            Controller.getBlockStore().toggleDrawing(data.getEntryKey());
            XRay.blockStore.write(Controller.getBlockStore().getStore());

            return;
        }

        if( doubleClick ) {
            XRay.mc.player.closeScreen();
            XRay.mc.displayGuiScreen( new GuiEdit(data.getEntryKey(), data) );
        }
    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected int getContentHeight() {
        return (this.getSize() * HEIGHT);
    }

    public void setItemList(ArrayList<BlockData> itemList) {
        this.itemList = itemList;
    }

    @Override
    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess) {
        BlockData blockData = this.itemList.get(idx);

        FontRenderer font = this.parent.getFontRender();

        font.drawString(blockData.getEntryName(), this.left + 30, top + 7, 0xFFFFFF);
        font.drawString(blockData.isDrawing() ? "Enabled" : "Disabled", this.left + 30, top + 17, blockData.isDrawing() ? Color.GREEN.getRGB() : Color.RED.getRGB());

        RenderHelper.enableGUIStandardItemLighting();
        this.parent.render.renderItemAndEffectIntoGUI(blockData.getItemStack(), this.left + 5, top + 7);
        RenderHelper.disableStandardItemLighting();

        Minecraft.getMinecraft().renderEngine.bindTexture(circle);
        GuiBase.drawTexturedQuadFit(right - 22, top + (HEIGHT / 2f) - 9, 14, 14, new int[]{255, 255, 255}, 20f);
        GuiBase.drawTexturedQuadFit(right - 20, top + (HEIGHT / 2f) - 7, 10, 10, blockData.getColor().getColor());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY) throws IOException {
        super.handleMouseInput(mouseX, mouseY);
    }
}
