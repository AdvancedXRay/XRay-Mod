package com.xray.client;

import com.xray.client.render.XrayRenderer;
import com.xray.common.XRay;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import com.xray.client.gui.GuiList;

public class KeyBindingHandler
{
	private Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void onKeyInput( KeyInputEvent event )
    {
		if( (!FMLClientHandler.instance().isGUIOpen( GuiChat.class )) && (mc.currentScreen == null) && (mc.world != null) )
        {
			if( XRay.keyBind_keys[ XRay.keyIndex_toggleXray ].isPressed() )
			{
				XRay.drawOres = !XRay.drawOres;
				XrayRenderer.ores.clear();
			}
			else if( XRay.keyBind_keys[ XRay.keyIndex_showXrayMenu ].isPressed() )
			{
				mc.displayGuiScreen( new GuiList() );
			}
		}
	}
}
