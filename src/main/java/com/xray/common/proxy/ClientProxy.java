package com.xray.common.proxy;

import com.xray.client.keybinding.InputEvent;
import com.xray.client.gui.GuiOverlay;
import com.xray.client.keybinding.KeyBindings;
import com.xray.client.xray.XRayController;
import com.xray.client.xray.XRayEventHandler;
import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.block.BlockItem;
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

import java.util.ArrayList;
import java.util.List;

public class ClientProxy
{
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.init( event.getSuggestedConfigurationFile() );

		// Setup the keyBindings
		KeyBindings.setup();

		MinecraftForge.EVENT_BUS.register( new InputEvent() );
		MinecraftForge.EVENT_BUS.register( new XRayEventHandler() );
		MinecraftForge.EVENT_BUS.register( new GuiOverlay() );
	}

	public void init(FMLInitializationEvent event) {

	    // Create a tmp way of adding blocks
        ArrayList<Block> blocks = new ArrayList<Block>() {{
           add(Blocks.CONCRETE);
           add(Blocks.COBBLESTONE);
        }};

        for( Block b : blocks ) {

        }
//        XRayController.getBlockStore().putBlock();
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
		XRayController.shutdownExecutor(); // Make sure threads don't lock the JVM
	}
}