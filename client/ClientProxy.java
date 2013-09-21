// Forge proxy for the client side.
package fgtXray.client;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import fgtXray.ServerProxy;

public class ClientProxy extends ServerProxy {
	@Override
	public void proxyInit() {
		TickRegistry.registerTickHandler(new ClientTick(), Side.CLIENT); // ClientTick gets called on every Player event.
		TickRegistry.registerTickHandler(new RenderTick(), Side.CLIENT); // RenderTick gets called on every Render event.
		KeyBindingRegistry.registerKeyBinding( new KeyBindingHandler() );// KeyBindingHandler handles the key events.
		MinecraftForge.EVENT_BUS.register( new RenderTick() );	// RenderTick is forge subscribed to onRenderEvent. Which is called when drawing the world.
	}
}