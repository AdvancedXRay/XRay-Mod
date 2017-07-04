package com.fgtXray.config;

import com.fgtXray.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultConfig {
	// Below are the 'default' ores/blocks to add through the ore dictionary.
	private final static Map<String, OreInfo> defaults = new HashMap<String, OreInfo>()
	{{
		put("oreLapis", new OreInfo("Lapis", 0, 0, new int[]{0, 0, 255}, false) );
		put("oreCopper", new OreInfo("Copper", 0, 0, new int[]{204, 102, 0}, true) );
		put("oreTin", new OreInfo("Tin", 0, 0, new int[]{161, 161, 161}, true) );
		put("oreCobalt", new OreInfo("Cobalt", 0, 0, new int[]{0, 0, 255}, false) );
		put("oreArdite", new OreInfo("Ardite", 0, 0, new int[]{255, 153, 0}, false) );
		put("oreCertusQuartz", new OreInfo("Certus Quartz", 0, 0, new int[]{255, 255, 255}, false) );
		put("oreUranium", new OreInfo("Uranium", 0, 0, new int[]{0, 255, 0}, true) );
		put("oreDiamond", new OreInfo("Diamond", 0, 0, new int[]{136, 136, 255}, false) );
		put("blockDiamond", new OreInfo("Diamond Block", 0, 0, new int[]{136, 136, 255}, false) );
		put("oreEmerald", new OreInfo("Emerald", 0, 0, new int[]{0, 136, 10}, true) );
		put("oreGold", new OreInfo("Gold", 0, 0, new int[]{255, 255, 0}, false) );
		put("oreRedstone", new OreInfo("Redstone", 0, 0, new int[]{255, 0, 0}, false) );
		put("oreIron", new OreInfo("Iron", 0, 0, new int[]{170, 117, 37}, false) );
		put("oreSilver", new OreInfo("Silver", 0, 0, new int[]{143,143,143}, false) );
		put("oreQuartz", new OreInfo("Quartz", 0, 0, new int[]{30,74,0}, false) );
		put("oreCoal", new OreInfo("Coal", 0, 0, new int[]{0, 0, 0}, false ) );
		put("blockGlass", new OreInfo("Glass", 0, 0, new int[]{136, 136, 255}, false) );
	}};
	
	// Default block to add. Mostly just so people can add custom blocks manually through the config until I setup a gui for it.
	private final static List<OreInfo> custom = new ArrayList<OreInfo>()
	{{
		add( new OreInfo("Redstone Wire", Block.getIdFromBlock( Blocks.REDSTONE_WIRE ), 0, new int[]{255, 0, 0}, false) );
		add( new OreInfo("Chest", Block.getIdFromBlock( Blocks.CHEST ), 0, new int[]{255, 0, 255}, true) );
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

			String category = value.oreName.replaceAll("\\s+", "").toLowerCase(); // No whitespace or capitals in the config file categories

			config.get("oredict."+category, "dictname", "SOMETHINGBROKE").set( key ); // We need the capitals for the ore dictionary.
			config.get("oredict."+category, "guiname", "SOMETHINGBROKE").set( value.oreName );
			config.get("oredict."+category, "id", -1).set( OreDictionary.getOreID( key ) );
			config.get("oredict."+category, "meta", -1).set( value.meta );
			config.get("oredict."+category, "red", -1).set( value.color[0] );
			config.get("oredict."+category, "green", -1).set( value.color[1] );
			config.get("oredict."+category, "blue", -1).set( value.color[2] );
			config.get("oredict."+category, "enabled", false).set( value.draw );
		}
		
		for( OreInfo ore : custom ) // Put custom block into the config file.
		{
			String name = ore.oreName.replaceAll("\\s+", "").toLowerCase(); // No whitespace or capitals in the config file categories.
			config.get("customores."+name, "name", "SOMETHINGBROKE").set( ore.oreName );
			config.get("customores."+name, "id", -1).set( ore.id );
			config.get("customores."+name, "meta", -1).set( ore.meta );
			config.get("customores."+name, "red", -1).set( ore.color[0] );
			config.get("customores."+name, "green", -1).set( ore.color[1] );
			config.get("customores."+name, "blue", -1).set( ore.color[2] );
			config.get("customores."+name, "enabled", false).set( ore.draw );
		}
		
	}
}
