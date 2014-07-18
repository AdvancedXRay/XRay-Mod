// Forge proxy for the client side.
package fgtXray.client;

import org.lwjgl.input.Keyboard;

import sun.security.krb5.Config;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import fgtXray.FgtXRay;
import fgtXray.ServerProxy;

public class ClientProxy extends ServerProxy {
	@Override
	public void proxyInit() {
		// Setup Keybindings
		FgtXRay.keyBind_keys = new KeyBinding[ FgtXRay.keyBind_descriptions.length ];
		for( int i = 0; i < FgtXRay.keyBind_descriptions.length; ++i ){
			FgtXRay.keyBind_keys[i] = new KeyBinding( FgtXRay.keyBind_descriptions[i], FgtXRay.keyBind_keyValues[i], "Fgt X-Ray" );
			ClientRegistry.registerKeyBinding( FgtXRay.keyBind_keys[i] );
		}
		
		FMLCommonHandler.instance().bus().register( new KeyBindingHandler() );
		FMLCommonHandler.instance().bus().register( new ClientTick() );
		MinecraftForge.EVENT_BUS.register( new RenderTick() );	// RenderTick is forge subscribed to onRenderEvent. Which is called when drawing the world.
	}
}