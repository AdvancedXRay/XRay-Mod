package com.xray.gui.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public abstract class AbstractScrollingList implements IGuiEventListener, IRenderable
{
    private final Minecraft client;

    protected final int listWidth;
    protected final int listHeight;
    protected final int screenWidth;
    protected final int screenHeight;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    protected final int slotHeight;
    protected int mouseX;
    protected int mouseY;

    private float scrollDistance;
    protected AbstractListEntry selected = null;
    private ArrayList<AbstractListEntry> listEntries = new ArrayList<>();

    public AbstractScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight)
    {
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;

        this.screenWidth = client.mainWindow.getWidth();
        this.screenHeight = client.mainWindow.getHeight();
    }

    public void setListEntries(ArrayList<AbstractListEntry> listEntries) {
        this.listEntries = listEntries;
    }

    public void addEntry(AbstractListEntry entry) {
        this.listEntries.add(entry);
    }

    protected abstract boolean isSelected(int index);

    protected int getContentHeight()
    {
        return this.listEntries.size() * this.slotHeight;
    }

    private void applyScrollLimits()
    {
        int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);
        if (listHeight < 0)
            listHeight /= 2;

        if (this.scrollDistance < 0.0F)
            this.scrollDistance = 0.0F;

        if (this.scrollDistance > (float)listHeight)
            this.scrollDistance = (float)listHeight;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double direction) {
        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
                mouseY >= this.top && mouseY <= this.bottom;

        if (!isHovering)
            return false;

        this.scrollDistance += (float)((-1 * direction) * this.slotHeight / 2);
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButtonId, double speedDelta, double direction) {
        System.out.println("--------------");
        System.out.println(mouseX);
        System.out.println(mouseY);
        System.out.println(mouseButtonId);
        System.out.println(speedDelta);
        System.out.println(direction);
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
                mouseY >= this.top && mouseY <= this.bottom;
        int scrollBarWidth = 6;
        int scrollBarRight = this.left + this.listWidth;
        int scrollBarLeft  = scrollBarRight - scrollBarWidth;
        int entryLeft      = this.left;
        int entryRight     = scrollBarLeft - 1;
        int viewHeight     = this.bottom - this.top;
        int border         = 4;

        this.applyScrollLimits();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();

        double scaleW = (double) client.mainWindow.getWidth() / client.mainWindow.getScaledWidth();
        double scaleH = (double) client.mainWindow.getHeight() / client.mainWindow.getScaledHeight();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left      * scaleW), (int)(client.mainWindow.getHeight() - (bottom * scaleH)),
                (int)(listWidth * scaleW), (int)(viewHeight * scaleH));

        GuiUtils.drawGradientRect(0, this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);

        int baseY = this.top + border - (int)this.scrollDistance;
        for (int slotIdx = 0; slotIdx < listEntries.size(); ++slotIdx)
        {
            int slotTop = baseY + slotIdx * this.slotHeight;
            int slotBuffer = this.slotHeight - border;

            if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top)
            {
                if (this.isSelected(slotIdx))
                {
                    int min = this.left;
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableTexture();
                    worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldr.pos(min,     slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(entryRight,     slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(entryRight,     slotTop              - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min,     slotTop              - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(entryRight - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(entryRight - 1, slotTop              - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(min + 1, slotTop              - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    tess.draw();
                    GlStateManager.enableTexture();
                }

                listEntries.get(slotIdx).render(slotIdx, slotTop, left, listWidth, slotHeight, mouseX, mouseY, false, partialTicks);
            }
        }

        GlStateManager.disableDepthTest();

        int extraHeight = (this.getContentHeight() + border) - viewHeight;
        if (extraHeight > 0)
        {
            int height = (viewHeight * viewHeight) / this.getContentHeight();
            if (height < 32)
                height = 32;

            if (height > viewHeight - border*2)
                height = viewHeight - border*2;

            int barTop = (int)this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
            if (barTop < this.top)
                barTop = this.top;

            GlStateManager.disableTexture();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,  this.bottom, 0.0D).tex(0.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.top,    0.0D).tex(1.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,  this.top,    0.0D).tex(0.0D, 0.0D).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,  barTop + height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight, barTop + height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight, barTop,          0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,  barTop,          0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft,      barTop + height - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarRight - 1, barTop + height - 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarRight - 1, barTop,              0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarLeft,      barTop,              0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.draw();
        }

        GlStateManager.enableTexture();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public abstract static class AbstractListEntry implements IGuiEventListener {
        public abstract void render(int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks);
    }
}