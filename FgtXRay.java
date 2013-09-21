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

@Mod(modid="FindIt", name="Find It", version="0.0.1")
@NetworkMod(clientSideRequired=true)
public class FgtXRay {
	public static int localPlyX, localPlyY, localPlyZ;
	public static boolean drawOres = false;
	public static boolean skipGenericBlocks = true;
	
	public static String[] distStrings = new String[]{ "8", "16", "32", "48", "64", "80", "128", "256" };
	public static int[] distNumbers = new int[]{ 8, 16, 32, 48, 64, 80, 128, 256 };
	public static int distIndex = 2;
	
	public static Map<String, OreInfo> defaultOres = new HashMap<String, OreInfo>(){{
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
		// add( OreInfo("GUI Name", id, meta, color, enabled ) );
		add( new OreInfo("Chest", Block.chest.blockID, 0, 0x663000, false) );
		add( new OreInfo("Redstone Wire", Block.redstoneWire.blockID, 0, 0xFF0000, false) );
	}};
	
	// The instance of your mod that Forge uses.
	@Instance(value = "FindIt")
	public static FgtXRay instance;
	
	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="findIt.client.ClientProxy", serverSide="findIt.ServerProxy")
	public static ServerProxy proxy;
	
	//@PreInit    // used in 1.5.2
	public void preInit(FMLPreInitializationEvent event) {
	        // Stub Method
	}

	@Init       // used in 1.5.2
	public void load(FMLInitializationEvent event) {
		proxy.proxyInit();
		//OreDictionary.registerOre("oreGold", Block.oreGold); // Testing Duplicate OreDict bug.
		if (OresSearch.searchList.isEmpty()){
			OresSearch.get();
		}
	}
	
	@PostInit   // used in 1.5.2
	public void postInit(FMLPostInitializationEvent event) {
	}
}
