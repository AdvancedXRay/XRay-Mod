// Forge proxy for the client side.
package com.xray.common.proxy;

import com.xray.client.KeyBindingHandler;
import com.xray.client.gui.GuiOverlay;
import com.xray.client.xray.XrayController;
import com.xray.client.xray.XrayEventHandler;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.BlockItem;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);

		ConfigHandler.init( event.getSuggestedConfigurationFile() );

		// Setup Keybindings
		XRay.keyBind_keys = new KeyBinding[ XRay.keyBind_descriptions.length ];
		for(int i = 0; i < XRay.keyBind_descriptions.length; ++i )
		{
			XRay.keyBind_keys[i] = new KeyBinding( XRay.keyBind_descriptions[i], XRay.keyBind_keyValues[i], "X-Ray" );
			ClientRegistry.registerKeyBinding( XRay.keyBind_keys[i] );
		}

		MinecraftForge.EVENT_BUS.register( new KeyBindingHandler() );
		MinecraftForge.EVENT_BUS.register( new XrayEventHandler() );
		MinecraftForge.EVENT_BUS.register( new GuiOverlay() );
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);

		ConfigHandler.setup(); // Read the config file and setup environment.

		Block tmpBlock;
		for ( Block block : ForgeRegistries.BLOCKS ) {
			NonNullList<ItemStack> subBlocks = NonNullList.create();
			block.getSubBlocks( block.getCreativeTabToDisplayOn(), subBlocks );
			if ( Blocks.AIR.equals( block ) )
				continue; // avoids troubles

			if( !subBlocks.isEmpty() ) {
				for( ItemStack subBlock : subBlocks ) {
					tmpBlock = Block.getBlockFromItem(subBlock.getItem());

					XRay.blockList.add(new BlockItem(tmpBlock.getRegistryName(), Block.getStateId(tmpBlock.getBlockState().getBaseState()), tmpBlock));
				}
			} else
				XRay.blockList.add( new BlockItem( block.getRegistryName(), Block.getStateId(block.getDefaultState()), block) );
		}
	}

	@Override
	public void onExit(FMLServerStoppingEvent event)
	{
		XrayController.shutdownExecutor(); // Make sure threads don't lock the JVM
	}
}