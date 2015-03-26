package com.fgtXray.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import com.fgtXray.FgtXRay;
import com.fgtXray.client.gui.GuiSettings;

public class KeyBindingHandler
{
	private Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void onKeyInput( KeyInputEvent event )
    {
		if( (!FMLClientHandler.instance().isGUIOpen( GuiChat.class )) && (mc.currentScreen == null) && (mc.theWorld != null) )
        {
			if (OresSearch.searchList.isEmpty()) // Populate the OresSearch.searchList
			{
				OresSearch.get();
			}
			if( FgtXRay.keyBind_keys[ FgtXRay.keyIndex_toggleXray ].isPressed() )
			{
				FgtXRay.drawOres = !FgtXRay.drawOres;
				RenderTick.ores.clear();
			}
			else if( FgtXRay.keyBind_keys[ FgtXRay.keyIndex_showXrayMenu ].isPressed() )
			{
				mc.displayGuiScreen( new GuiSettings() );
			}
		}
	}
}
