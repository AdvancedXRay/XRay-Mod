package com.xray.common.config;

import com.xray.common.reference.OreInfo;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultConfig {
	// Below are the 'default' ores/blocks to add through the ore dictionary.
	private final static Map<String, OreInfo> defaults = new HashMap<String, OreInfo>()
	{{
		put("oreLapis", new OreInfo("Lapis", "Lapis", 0, 0, new int[]{0, 0, 255}, false) );
		put("oreCopper", new OreInfo("Copper", "Copper", 0, 0, new int[]{204, 102, 0}, true) );
		put("oreTin", new OreInfo("Tin", "Tin", 0, 0, new int[]{161, 161, 161}, true) );
		put("oreCobalt", new OreInfo("Cobalt", "Cobalt", 0, 0, new int[]{0, 0, 255}, false) );
		put("oreArdite", new OreInfo("Ardite", "Ardite", 0, 0, new int[]{255, 153, 0}, false) );
		put("oreCertusQuartz", new OreInfo("Certus Quartz", "CertusQuartz", 0, 0, new int[]{255, 255, 255}, false) );
		put("oreUranium", new OreInfo("Uranium", "Uranium", 0, 0, new int[]{0, 255, 0}, true) );
		put("oreDiamond", new OreInfo("Diamond", "Diamond", 0, 0, new int[]{136, 136, 255}, false) );
		put("blockDiamond", new OreInfo("Diamond Block",  "DiamondBlock", 0, 0, new int[]{136, 136, 255}, false) );
		put("oreEmerald", new OreInfo("Emerald", "Emerald", 0, 0, new int[]{0, 136, 10}, true) );
		put("oreGold", new OreInfo("Gold", "Gold", 0, 0, new int[]{255, 255, 0}, false) );
		put("oreRedstone", new OreInfo("Redstone", "Redstone", 0, 0, new int[]{255, 0, 0}, false) );
		put("oreIron", new OreInfo("Iron", "Iron", 0, 0, new int[]{170, 117, 37}, false) );
		put("oreSilver", new OreInfo("Silver", "Silver", 0, 0, new int[]{143,143,143}, false) );
		put("oreQuartz", new OreInfo("Quartz", "Quartz", 0, 0, new int[]{30,74,0}, false) );
		put("oreCoal", new OreInfo("Coal", "Coal", 0, 0, new int[]{0, 0, 0}, false ) );
	}};

	public static void create(Configuration config) // Put default blocks and settings into the config file.
	{
		config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0); // Default search distance is index 0 (8)

		for( Entry<String, OreInfo> ore : defaults.entrySet() )
		{
			String key = ore.getKey();
			OreInfo value = ore.getValue();

			if( !OreDictionary.doesOreNameExist( key ) )
				continue;

			String category = value.oreName;

			config.get("ores."+category, "name", "").set( value.displayName );
			config.get("ores."+category, "id", -1).set( OreDictionary.getOreID( key ) );
			config.get("ores."+category, "meta", -1).set( value.meta );
			config.get("ores."+category, "red", -1).set( value.color[0] );
			config.get("ores."+category, "green", -1).set( value.color[1] );
			config.get("ores."+category, "blue", -1).set( value.color[2] );
			config.get("ores."+category, "enabled", false).set( value.draw );
		}
	}
}
