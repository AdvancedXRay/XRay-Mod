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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ClientProxy
{
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.init( event.getSuggestedConfigurationFile() );

		// Setup the keybindings
		KeyBindingHandler.setup();

		MinecraftForge.EVENT_BUS.register( new KeyBindingHandler() );
		MinecraftForge.EVENT_BUS.register( new XrayEventHandler() );
		MinecraftForge.EVENT_BUS.register( new GuiOverlay() );
	}

	public void init(FMLInitializationEvent event) {

	}

	public void postInit(FMLPostInitializationEvent event) {
		ConfigHandler.setup(); // Read the config file and setup environment.

		Block tmpBlock;
		for ( Block block : ForgeRegistries.BLOCKS ) {
			NonNullList<ItemStack> subBlocks = NonNullList.create();
			block.getSubBlocks( block.getCreativeTabToDisplayOn(), subBlocks );
			if ( Blocks.AIR.equals( block ) )
				continue; // avoids troubles

			if( !subBlocks.isEmpty() ) {
				for( ItemStack subBlock : subBlocks ) {
					if( subBlock.equals(ItemStack.EMPTY) || subBlock.getItem() == Items.AIR )
						continue;

					XRay.blockList.add(new BlockItem(Block.getStateId(Block.getBlockFromItem(subBlock.getItem()).getBlockState().getBaseState()), subBlock));
				}
			} else
				XRay.blockList.add( new BlockItem( Block.getStateId(block.getDefaultState()), new ItemStack(block)) );
		}
	}

	public void onExit(FMLServerStoppingEvent event)
	{
		XrayController.shutdownExecutor(); // Make sure threads don't lock the JVM
	}
}