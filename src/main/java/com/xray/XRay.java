package com.xray;

import com.xray.gui.GuiOverlay;
import com.xray.keybinding.InputEvent;
import com.xray.keybinding.KeyBindings;
import com.xray.reference.Reference;
import com.xray.reference.block.BlockItem;
import com.xray.store.JsonStore;
import com.xray.xray.Controller;
import com.xray.xray.Events;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Mod(
		modid= Reference.MOD_ID,
		name= Reference.MOD_NAME,
		version=Reference.MOD_VERSION,
		updateJSON= Reference.UPDATE_JSON,

		clientSideOnly = true
)

@Mod.EventBusSubscriber(
		modid = Reference.MOD_ID
)

@SideOnly(Side.CLIENT)
public class XRay
{
	public static ArrayList<BlockItem> blockList = new ArrayList<>();

	public static Minecraft mc = Minecraft.getMinecraft();
	public static JsonStore blockStore = new JsonStore();

	public static Logger logger;
	@Instance(Reference.MOD_ID)
	public static XRay instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		logger.debug(I18n.format("xray.debug.init"));

		KeyBindings.setup();

		MinecraftForge.EVENT_BUS.register( new InputEvent() );
		MinecraftForge.EVENT_BUS.register( new Events() );
		MinecraftForge.EVENT_BUS.register( new GuiOverlay() );

		MinecraftForge.EVENT_BUS.register( this );
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		Block tmpBlock;
		for ( Block block : ForgeRegistries.BLOCKS ) {
			NonNullList<ItemStack> subBlocks = NonNullList.create();
			block.getSubBlocks( block.getCreativeTabToDisplayOn(), subBlocks );
			if ( Blocks.AIR.equals( block ) ||  Controller.blackList.contains(block) )
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


	@EventHandler
	public void onExit(FMLServerStoppingEvent event)
	{
		Controller.shutdownExecutor();
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(Reference.MOD_ID))
		{
			ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
		}
	}
}
