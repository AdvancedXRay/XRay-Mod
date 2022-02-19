package pro.mikey.xray.gui.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.lwjgl.opengl.GL11;

/**
 * A bare bones implementation of the {@link AbstractList} / {@link net.minecraft.client.gui.widget.list.ExtendedList}
 * without the background or borders. With GL_SCISSOR to crop out the overflow
 *
 * This is how an abstract implementation should look... :cry:
 */
public class ScrollingList<E extends AbstractSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    public ScrollingList(int x, int y, int width, int height, int slotHeightIn) {
        super(Minecraft.getInstance(), width, height, y - (height / 2), (y - (height / 2)) + height, slotHeightIn);
        this.setLeftPos(x - (width / 2));
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false); // removes background
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(this.x0  * scale), (int)(Minecraft.getInstance().getWindow().getHeight() - ((this.y0 + this.height) * scale)),
                (int)(this.width * scale), (int)(this.height * scale));

        super.render(stack, mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override // @mcp: getScrollbarPosition = getScrollbarPosition
    protected int getScrollbarPosition() {
        return (this.x0 + this.width) - 6;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
