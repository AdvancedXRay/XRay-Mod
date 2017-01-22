// Forge proxy for the client side.
package com.fgtxray.proxy;

import com.fgtxray.client.ClientTick;
import com.fgtxray.client.KeyBindingHandler;
import com.fgtxray.client.RenderTick;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import com.fgtxray.FgtXRay;

public class ClientProxy extends ServerProxy
{
	@Override
	public void proxyInit()
	{
		// Setup Keybindings
		FgtXRay.keyBind_keys = new KeyBinding[ FgtXRay.keyBind_descriptions.length ];
		for( int i = 0; i < FgtXRay.keyBind_descriptions.length; ++i )
        {
			FgtXRay.keyBind_keys[i] = new KeyBinding( FgtXRay.keyBind_descriptions[i], FgtXRay.keyBind_keyValues[i], "Fgt X-Ray" );
			ClientRegistry.registerKeyBinding( FgtXRay.keyBind_keys[i] );
		}

		MinecraftForge.EVENT_BUS.register( new KeyBindingHandler() );
		MinecraftForge.EVENT_BUS.register( new ClientTick() );
		MinecraftForge.EVENT_BUS.register( new RenderTick() );	// RenderTick is forge subscribed to onRenderEvent. Which is called when drawing the world.
	}
}