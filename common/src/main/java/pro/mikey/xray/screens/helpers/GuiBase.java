package pro.mikey.xray.screens.helpers;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import pro.mikey.xray.XRay;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiBase extends Screen {
    public static final ResourceLocation BG_NORMAL = XRay.assetLocation("gui/bg.png");
    public static final ResourceLocation BG_LARGE = XRay.assetLocation("gui/bg-help.png");

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

        if (keyTyped == 1 && minecraft.player != null) {
            minecraft.player.closeContainer();
        }

        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        super.renderBackground(guiGraphics, x, y, partialTicks);

        int width = this.width;
        int height = this.height;
        if (this.hasSide) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getBackground(), width / 2 + 60, height / 2 - 180 / 2, 0, 0, 150, 180, 150, 180);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getBackground(), width / 2 - 150, height / 2 - 118, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

            if (hasSideTitle())
                guiGraphics.drawString(getFontRender(), this.sideTitle, width / 2 + 80, height / 2 - 77, 0xFFFFFF00);
        }

        if (!this.hasSide)
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getBackground(), width / 2 - this.backgroundWidth / 2 + 1, height / 2 - this.backgroundHeight / 2, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

        if (hasTitle()) {
            if (this.hasSide)
                guiGraphics.drawString(getFontRender(), title(), width / 2 - 138, height / 2 - 105, 0xFFFFFF00);
            else
                guiGraphics.drawString(getFontRender(), title(), width / 2 - (this.backgroundWidth / 2) + 14, height / 2 - (this.backgroundHeight / 2) + 13, 0xFFFFFF00);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        super.render(guiGraphics, x, y, partialTicks);

        renderExtra(guiGraphics, x, y, partialTicks);

        for (GuiEventListener button : this.children()) {
            if (button instanceof SupportButton && ((SupportButton) button).isHovered()) {
                var visualItems = Language.getInstance().getVisualOrder(((SupportButton) button).getSupport());
                List<ClientTooltipComponent> tooltipComponents = new ArrayList<>();
                for (FormattedCharSequence component : visualItems) {
                    tooltipComponents.add(ClientTooltipComponent.create(component));
                }

                guiGraphics.renderTooltip(getFontRender(), tooltipComponents, x, y, DefaultTooltipPositioner.INSTANCE, null);
            }
        }
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
        return minecraft.font;
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
