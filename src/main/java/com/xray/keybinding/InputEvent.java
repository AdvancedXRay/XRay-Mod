package com.xray.keybinding;

import com.xray.XRay;
import com.xray.gui.GuiSelectionScreen;
import com.xray.reference.Reference;
import com.xray.xray.Controller;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class InputEvent
{
	@SubscribeEvent
	public static void eventInput(TickEvent event)
    {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || event.phase == TickEvent.Phase.START)
			return;

		if( XRay.mc.currentScreen == null && XRay.mc.world != null )
        {
			if( KeyBindings.keyBind_keys[ KeyBindings.keyIndex_toggleXray ].isPressed() )
			{
				Controller.toggleDrawOres();
//				Controller.getBlockStore().getStore().entrySet().forEach(e -> System.out.printf("%s %s %s\n\n", e.getValue().getBlockName(), e.getValue().getItemStack(), e.getValue().getOrder()));
			}
			else if( KeyBindings.keyBind_keys[ KeyBindings.keyIndex_showXrayMenu ].isPressed() )
			{
				XRay.mc.displayGuiScreen( new GuiSelectionScreen() );
			}
		}
	}
}
