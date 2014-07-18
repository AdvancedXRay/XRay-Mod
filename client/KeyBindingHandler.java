package fgtXray.client;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import fgtXray.FgtXRay;

public class KeyBindingHandler {
	private Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void onKeyInput( KeyInputEvent event ){
		if( (!FMLClientHandler.instance().isGUIOpen( GuiChat.class )) && (mc.currentScreen == null) && (mc.theWorld != null) ){
			if (OresSearch.searchList.isEmpty()){ // Populate the OresSearch.searchList
				OresSearch.get();
			}
			if( FgtXRay.keyBind_keys[ FgtXRay.keyIndex_toggleXray ].isPressed() ){
				FgtXRay.drawOres = !FgtXRay.drawOres;
				RenderTick.ores.clear();
			}else if( FgtXRay.keyBind_keys[ FgtXRay.keyIndex_showXrayMenu ].isPressed() ){
				mc.displayGuiScreen( new OldGuiSettings() );
				//mc.displayGuiScreen( new GuiXraySettings() );
			}
		}
	}
}
