package com.xray.gui.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;
import org.lwjgl.opengl.GL11;

/**
 * A bare bones implementation of the {@link AbstractList} / {@link net.minecraft.client.gui.widget.list.ExtendedList}
 * without the background or borders. With GL_SCISSOR to crop out the overflow
 *
 * This is how an abstract implementation should look... :cry:
 */

// @mcp: field_230675_l_ = this.x0
// @mcp: field_230672_i_ = this.y0
// @mcp: field_230670_d_ = this.width
// @mcp: field_230671_e_ = this.height
public class ScrollingList<E extends AbstractList.AbstractListEntry<E>> extends AbstractList<E> {
    public ScrollingList(int x, int y, int width, int height, int slotHeightIn) {
        super(Minecraft.getInstance(), width, height, y - (height / 2), (y - (height / 2)) + height, slotHeightIn);
        this.func_230959_g_(x - (width / 2)); // @mcp: func_230959_g_ = setLeftPro
    }

    @Override // @mcp: func_230430_a_ = render
    public void func_230430_a_(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        double scale = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(this.field_230675_l_  * scale), (int)(Minecraft.getInstance().getMainWindow().getFramebufferHeight() - ((this.field_230672_i_ + this.field_230671_e_) * scale)),
                (int)(this.field_230670_d_ * scale), (int)(this.field_230671_e_ * scale));

        super.func_230430_a_(stack, mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override // @mcp: func_230952_d_ = getScrollbarPosition
    protected int func_230952_d_() {
        return (this.field_230675_l_ + this.field_230670_d_) - 6;
    }
}
