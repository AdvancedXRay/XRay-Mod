package pro.mikey.xray.gui.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pro.mikey.xray.Utils;
import pro.mikey.xray.XRay;

public abstract class GuiBase extends Screen {
    public static final ResourceLocation BG_NORMAL = Utils.rlFull(XRay.PREFIX_GUI + "bg.png");
    public static final ResourceLocation BG_LARGE = Utils.rlFull(XRay.PREFIX_GUI + "bg-help.png");

    private boolean hasSide;
    private String sideTitle = "";
    private int backgroundWidth = 229;
    private int backgroundHeight = 235;

    public GuiBase(boolean hasSide) {
        super(Component.literal(""));
        this.hasSide = hasSide;
    }

    public abstract void renderExtra(GuiGraphics guiGraphics, int x, int y, float partialTicks);

    @Override
    public boolean charTyped(char keyTyped, int __unknown) {
        super.charTyped(keyTyped, __unknown);

        if (keyTyped == 1 && getMinecraft().player != null)
            getMinecraft().player.closeContainer();

        return false;
    }

    // mouseX and mouseY indicate the scaled coordinates of where the cursor is in on the screen
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Submit things on the background stratum
        this.renderTransparentBackground(graphics);


        int width = this.width;
        int height = this.height;
        if (this.hasSide) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, getBackground(), width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
            graphics.blit(RenderPipelines.GUI_TEXTURED, getBackground(), width / 2 - 150, height / 2 - 118, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

            if (hasSideTitle())
                graphics.drawString(getFontRender(), this.sideTitle, width / 2 + 80, height / 2 - 77, 0xffff00);
        }

        if (!this.hasSide)
            graphics.blit(RenderPipelines.GUI_TEXTURED, getBackground(), width / 2 - this.backgroundWidth / 2 + 1, height / 2 - this.backgroundHeight / 2, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

        if (hasTitle()) {
            if (this.hasSide)
                graphics.drawString(getFontRender(), title(), width / 2 - 138, height / 2 - 105, 0xffff00);
            else
                graphics.drawString(getFontRender(), title(), width / 2 - (this.backgroundWidth / 2) + 14, height / 2 - (this.backgroundHeight / 2) + 13, 0xffff00);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        renderExtra(graphics, mouseX, mouseY, partialTick);

        // Not sure what this does. Seems to work so far without it.
        // TODO investigate
        //for (GuiEventListener button : this.children()) {
        //    if (button instanceof SupportButton && ((SupportButton) button).isHovered()) {
        //        graphics.renderTooltip(getFontRender(), Language.getInstance().getVisualOrder(((SupportButton) button).getSupport()), mouseX, mouseY);
        //    }
        //}
    }

    public ResourceLocation getBackground() {
        return BG_NORMAL;
    }

    public boolean hasTitle() {
        return false;
    }

    public String title() {
        return "";
    }

    private boolean hasSideTitle() {
        return !this.sideTitle.isEmpty();
    }

    protected void setSideTitle(String title) {
        this.sideTitle = title;
    }

    public void setSize(int width, int height) {
        this.backgroundWidth = width;
        this.backgroundHeight = height;
    }

    public Font getFontRender() {
        return getMinecraft().font;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
