package com.xray.client.render;

import com.xray.common.XRay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by MiKeY on 29/12/17.
 */
public class CaveRenderer {

    private static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void worldRenderTick(RenderWorldLastEvent event) {

        if ( mc.world == null || !XRay.drawCaves )
            return;

        // start render
    }

}
