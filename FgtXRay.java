/* TODO:
 * Remake the GUI
 * Implement custom ores/ids though textbox
 *    Saving/loading of custom ores to file
 * Optimize
 * Add +/- to render distance.
 */

package fgtXray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import fgtXray.OreInfo;
import fgtXray.client.OresSearch;

@Mod(modid="FgtXray", name="Fgt X-Ray", version="0.0.1")
@NetworkMod(clientSideRequired=true)
public class FgtXRay {
	public static int localPlyX, localPlyY, localPlyZ; // For internal use in the ClientTick thread.
	public static boolean drawOres = false; // Off by default
	public static boolean skipGenericBlocks = true; // See ClientTick.run() thread. Skip common blocks in overworld/nether/end.
	
	public static String[] distStrings = new String[]{ "8", "16", "32", "48", "64", "80", "128", "256" }; // Strings for use in the GUI
	public static int[] distNumbers = new int[]{ 8, 16, 32, 48, 64, 80, 128, 256 }; // Radius +/- around the player to search. So 8 is 8 on left and right of player plus under the player. So 17x17 area. 
	public static int distIndex = 2; // Index for the distNumers array. Default search distance.
	
	public static Map<String, OreInfo> defaultOres = new HashMap<String, OreInfo>(){{
		/* Default ores to check through the ore dictionary and add each instance found to the searchList. 
		 * put( "oreType", new OreInfo(...) ) oreType is the ore dictionary string id. Press Print OreDict and check console to see list.
		 * OreInfo( String "Gui Name", // The name to be displayed in the GUI.
		 *     int id, int meta, // Leave these at 0. The OresSearch will set them through the ore dictionary.
		 *     int color, // 0x RED GREEN BLUE (0xRRGGBB)
		 *     bool enabled) // Should the be on by default. Its then set internally by GuiSettings.
		 */
		put("oreLapis", new OreInfo("Lapis", 0, 0, 0x0000FF, false) );
		put("oreCopper", new OreInfo("Copper", 0, 0, 0xCC6600, true) );
		put("oreTin", new OreInfo("Tin", 0, 0, 0xA1A1A1, true) );
		put("oreCobalt", new OreInfo("Cobalt", 0, 0, 0x0000FF, false) );
		put("oreArdite", new OreInfo("Ardite", 0, 0, 0xFF9900, false) );
		put("oreCertusQuartz", new OreInfo("Certus Quartz", 0, 0, 0xFFFFFF, false) );
		put("oreUranium", new OreInfo("Uranium", 0, 0, 0x00FF00, true) );
		put("oreDiamond", new OreInfo("Diamond", 0, 0, 0x8888FF, false) );
		put("oreEmerald", new OreInfo("Emerald", 0, 0, 0x008810, true) );
		put("oreGold", new OreInfo("Gold", 0, 0, 0xFFFF00, false) );
		put("oreRedstone", new OreInfo("Redstone", 0, 0, 0xFF0000, false) );
		put("oreIron", new OreInfo("Iron", 0, 0, 0xAA7525, false) );
		put("oreSilver", new OreInfo("Silver", 0, 0, 0x8F8F8F, false) );
		put("mossystone", new OreInfo("Mossy Stone", 0, 0, 0x1E4A00, false) );
	}};
	
	public static List<OreInfo> customOres = new ArrayList<OreInfo>(){{
		/* List of custom id:meta to add.
		 * OreInfo( String "Gui Name", // Displayed in the GUI.
		 *     int id, int meta, // Set these to whatever the id:meta is for your block.
		 *     int color, // color 0xRRGGBB
		 *     bool enabled) // On by default? 
		 */
		add( new OreInfo("Chest", Block.chest.blockID, 0, 0x663000, false) );
		add( new OreInfo("Redstone Wire", Block.redstoneWire.blockID, 0, 0xFF0000, false) );
	}};
	
	// The instance of your mod that Forge uses.
	@Instance(value = "FgtXray")
	public static FgtXRay instance;
	
	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="fgtXray.client.ClientProxy", serverSide="fgtXray.ServerProxy")
	public static ServerProxy proxy;
	
	//@PreInit    // used in 1.5.2
	public void preInit(FMLPreInitializationEvent event) {
	        // Stub Method
	}

	@Init       // used in 1.5.2
	public void load(FMLInitializationEvent event) {
		proxy.proxyInit();
		//OreDictionary.registerOre("oreGold", Block.oreGold); // Testing Duplicate OreDict bug.
		if (OresSearch.searchList.isEmpty()){ // Populate the OresSearch.searchList
			OresSearch.get();
		}
	}
	
	@PostInit   // used in 1.5.2
	public void postInit(FMLPostInitializationEvent event) {
	}
}
