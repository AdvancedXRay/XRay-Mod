// Forge proxy for the client side.
package com.xray.client.proxy;

import com.xray.client.render.CaveFinder;
import com.xray.client.render.CaveRenderer;
import com.xray.client.render.ClientTick;
import com.xray.client.KeyBindingHandler;
import com.xray.client.render.XrayRenderer;
import com.xray.common.proxy.ServerProxy;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import com.xray.common.XRay;

public class ClientProxy extends ServerProxy
{
	@Override
	public void proxyInit()
	{
		// Setup Keybindings
		XRay.keyBind_keys = new KeyBinding[ XRay.keyBind_descriptions.length ];
		for(int i = 0; i < XRay.keyBind_descriptions.length; ++i )
        {
			XRay.keyBind_keys[i] = new KeyBinding( XRay.keyBind_descriptions[i], XRay.keyBind_keyValues[i], "X-Ray" );
			ClientRegistry.registerKeyBinding( XRay.keyBind_keys[i] );
		}

		MinecraftForge.EVENT_BUS.register( new KeyBindingHandler() );
		MinecraftForge.EVENT_BUS.register( new ClientTick() );
		MinecraftForge.EVENT_BUS.register( new CaveRenderer() );
		MinecraftForge.EVENT_BUS.register( new CaveFinder() );
		MinecraftForge.EVENT_BUS.register( new XrayRenderer() );	// XrayRenderer is forge subscribed to onRenderEvent. Which is called when drawing the world.
	}
}