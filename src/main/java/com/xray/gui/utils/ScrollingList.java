package com.xray.gui.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;
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
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        double scale = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(this.x0  * scale), (int)(Minecraft.getInstance().getMainWindow().getFramebufferHeight() - ((this.y0 + this.height) * scale)),
                (int)(this.width * scale), (int)(this.height * scale));

        super.render(stack, mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override // @mcp: func_230952_d_ = getScrollbarPosition
    protected int getScrollbarPosition() {
        return (this.x0 + this.width) - 6;
    }
}
