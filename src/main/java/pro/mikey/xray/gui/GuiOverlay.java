package pro.mikey.xray.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import pro.mikey.xray.Configuration;
import pro.mikey.xray.XRay;
import pro.mikey.xray.xray.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XRay.MOD_ID, value = Dist.CLIENT)
public class GuiOverlay {
    private static final ResourceLocation circle = new ResourceLocation(XRay.PREFIX_GUI + "circle.png");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void RenderGameOverlayEvent(RenderGameOverlayEvent event) {
        // Draw Indicator
        if(!Controller.isXRayActive() || !Configuration.general.showOverlay.get() || event.isCanceled() || event.getType() != RenderGameOverlayEvent.ElementType.TEXT )
            return;

        RenderSystem.color3f(0, 255, 0);
        Minecraft.getInstance().getTextureManager().bindTexture(circle);
        Screen.blit(event.getMatrixStack(), 5, 5, 0f, 0f, 5, 5, 5, 5);

        Minecraft.getInstance().fontRenderer.drawStringWithShadow(event.getMatrixStack(), I18n.format("xray.overlay"), 15, 4, 0xffffffff);
    }
}
