package com.xray.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xray.client.OresSearch;
import com.xray.common.proxy.ServerProxy;
import com.xray.client.gui.helper.HelperBlock;
import com.xray.common.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;

import net.minecraftforge.common.config.Configuration;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.xray.common.reference.OreInfo;
import com.xray.common.config.DefaultConfig;
import com.xray.common.config.ConfigHandler;

@Mod(modid= Reference.MOD_ID, name= Reference.MOD_NAME, version=Reference.MOD_VERSION)
public class XRay
{
	public static int localPlyX, localPlyY, localPlyZ, localPlyXPrev, localPlyZPrev; // For internal use in the ClientTick thread.
	public static boolean drawOres = false; // Off by default
	public static ArrayList<HelperBlock> blockList = new ArrayList<>();

	public static final String[] distStrings = new String[] // Strings for use in the GUI Render Distance button
		{ "8", "16", "32", "48", "64", "80", "128", "256" };
    public static final int[] distNumbers = new int[] // Radius +/- around the player to search. So 8 is 8 on left and right of player plus under the player. So 17x17 area.
		{8, 16, 32, 48, 64, 80, 128, 256};

    public static int currentDist = 0; // Index for the distNumers array. Default search distance.

	// Keybindings
	public static final int keyIndex_toggleXray = 0;
	public static final int keyIndex_showXrayMenu = 1;
	public static final int[] keyBind_keyValues = 
	{
		Keyboard.KEY_BACKSLASH,
		Keyboard.KEY_Z
	};
	public static final String[] keyBind_descriptions =
	{
		"Toggle X-Ray",
		"Open X-Ray Menu"
	};
	public static KeyBinding[] keyBind_keys = null;

	public static Map<String, OreInfo> oredictOres = new HashMap<String, OreInfo>();
		/* Ores to check through the ore dictionary and add each instance found to the searchList. 
		 * put( "oreType", new OreInfo(...) ) oreType is the ore dictionary string id. Press Print OreDict and check console to see list.
		 * OreInfo( String "Gui Name", // The name to be displayed in the GUI.
		 *     int id, int meta, // Leave these at 0. The OresSearch will set them through the ore dictionary.
		 *     int color, // 0x RED GREEN BLUE (0xRRGGBB)
		 *     bool enabled) // Should the be on by default. Its then set internally by GuiList.
		 * Open DefaultConfig.java for more info.
		 */
	
	public static List<OreInfo> customOres = new ArrayList<OreInfo>();
		/* List of custom id:meta to add.
		 * OreInfo( String "Gui Name", // Displayed in the GUI.
		 *     int id, int meta, // Set these to whatever the id:meta is for your block.
		 *     int color, // color 0xRRGGBB
		 *     bool enabled) // On by default? 
		 */
	
	// The instance of your mod that Forge uses.
	@Instance(Reference.MOD_ID)
	public static XRay instance;
	
	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="com.xray.client.proxy.ClientProxy", serverSide="com.xray.common.proxy.ServerProxy")
	private static ServerProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
    {
		Configuration config = new Configuration( event.getSuggestedConfigurationFile() );
		config.load();
		
		if( config.getCategoryNames().isEmpty() )
        {
			System.out.println("[XRay] Config file not found. Creating now.");
			DefaultConfig.create( config );
			config.save();
		}
		
		ConfigHandler.setup( event ); // Read the config file and setup environment.
        System.out.println("[XRay] PreInit ");
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
    {
		proxy.proxyInit();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
    {
		for ( Block block : ForgeRegistries.BLOCKS ) {
			NonNullList<ItemStack> subBlocks = NonNullList.create();
			block.getSubBlocks( block.getCreativeTabToDisplayOn(), subBlocks );
			for( ItemStack subBlock : subBlocks ) {
				if (subBlock.isEmpty())
					continue;

				Block tmpBlock = Block.getBlockFromItem( subBlock.getItem() );
				blockList.add( new HelperBlock( subBlock.getDisplayName(), tmpBlock, subBlock, subBlock.getItem(), subBlock.getItem().getRegistryName() ));
			}
		}

		if (OresSearch.searchList.isEmpty()) // Populate the OresSearch.searchList
		{
			OresSearch.get();
		}
	}
}
