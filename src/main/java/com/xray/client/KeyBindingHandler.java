package com.xray.client;

import com.xray.client.gui.GuiSelectionScreen;
import com.xray.client.xray.XrayController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

// TODO: Refactor the static block
public class KeyBindingHandler
{
	private Minecraft mc = Minecraft.getMinecraft();

	// Keybindings
	private static final int keyIndex_toggleXray = 0;
	private static final int keyIndex_showXrayMenu = 1;
	private static final int[] keyBind_keyValues = { Keyboard.KEY_BACKSLASH, Keyboard.KEY_Z };
	private static final String[] keyBind_descriptions = { I18n.format("xray.config.toggle"), I18n.format("xray.config.open")};
	private static KeyBinding[] keyBind_keys = null;

	@SubscribeEvent
	public void onKeyInput( KeyInputEvent event )
    {
		if( (!FMLClientHandler.instance().isGUIOpen( GuiChat.class )) && (mc.currentScreen == null) && (mc.world != null) )
        {
			if( keyBind_keys[ keyIndex_toggleXray ].isPressed() )
			{
				XrayController.toggleDrawOres();
			}
			else if( keyBind_keys[ keyIndex_showXrayMenu ].isPressed() )
			{
				mc.displayGuiScreen( new GuiSelectionScreen() );
			}
		}
	}

	public static void setup() {
		// Setup Keybindings
		keyBind_keys = new KeyBinding[ keyBind_descriptions.length ];
		for(int i = 0; i < keyBind_descriptions.length; ++i )
		{
			keyBind_keys[i] = new KeyBinding( keyBind_descriptions[i], keyBind_keyValues[i], "X-Ray" );
			ClientRegistry.registerKeyBinding( keyBind_keys[i] );
		}
	}
}
