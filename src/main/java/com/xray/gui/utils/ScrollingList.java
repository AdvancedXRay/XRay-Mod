package com.xray.gui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraftforge.client.gui.ScrollPanel;
import org.lwjgl.opengl.GL11;

/**
 * A bare bones implementation of the {@link AbstractList} / {@link net.minecraft.client.gui.widget.list.ExtendedList}
 * without the background or borders. With GL_SCISSOR to crop out the overflow
 *
 * This is how an abstract implementation should look... :cry:
 */
public class ScrollingList<E extends AbstractList.AbstractListEntry<E>> extends AbstractList<E> {
    public ScrollingList(int x, int y, int width, int height, int slotHeightIn) {
        super(Minecraft.getInstance(), width, height, y - (height / 2), (y - (height / 2)) + height, slotHeightIn);
        this.setLeftPos(x - (width / 2));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        double scale = Minecraft.getInstance().mainWindow.getGuiScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(this.x0  * scale), (int)(Minecraft.getInstance().mainWindow.getFramebufferHeight() - ((this.y0 + height) * scale)),
                (int)(width * scale), (int)(height * scale));

        super.render(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    protected int getScrollbarPosition() {
        return (this.x0 + this.width) - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) { }
}
