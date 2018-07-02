package com.xray.client.gui;

import com.xray.client.xray.XrayController;
import com.xray.common.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiOverlay extends Gui {

    private Minecraft mc = Minecraft.getMinecraft();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void RenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {

        // Draw Indicator
        if(!XrayController.drawOres())
            return;

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "circle.png"));
        drawModalRectWithCustomSizedTexture(5, 5, 0f, 0f, 10, 10, 10, 10);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

}
