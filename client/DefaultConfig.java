package fgtXray.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fgtXray.OreInfo;

import net.minecraft.block.Block;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;

public class DefaultConfig {
	// Below are the 'default' ores/blocks to add through the ore dictionary.
	final static Map<String, OreInfo> defaults = new HashMap<String, OreInfo>(){{
		put("oreLapis", new OreInfo("Lapis", 0, 0, 0x0000FF, false) );
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
	
	// Default block to add. Mostly just so people can add custom blocks manually through the config until I setup a gui for it.
	final static List<OreInfo> custom = new ArrayList<OreInfo>(){{
		add( new OreInfo("Redstone Wire", Block.redstoneWire.blockID, 0, 0xFF0000, false) );
		add( new OreInfo("Chest", Block.chest.blockID, 0, 0xFF00FF, true) );
	}};
	
	public DefaultConfig() {}

	public static void create(Configuration config) { // Put default blocks and settings into the config file.
		config.get(config.CATEGORY_GENERAL, "searchdist", 0); // Default search distance is index 0 (8)
		
		for( Entry<String, OreInfo> ore : defaults.entrySet() ){
			String key = ore.getKey();
			OreInfo value = ore.getValue();
			String category = value.oreName.replaceAll("\\s+", "").toLowerCase(); // No whitespace or capitals in the config file categories
			
			config.get("oredict."+category, "dictname", "SOMETHINGBROKE").set( key ); // We need the capitals for the ore dictionary.
			config.get("oredict."+category, "guiname", "SOMETHINGBROKE").set( value.oreName );
			config.get("oredict."+category, "id", -1).set( value.id );
			config.get("oredict."+category, "meta", -1).set( value.meta );
			config.get("oredict."+category, "color", -1).set( value.color );
			config.get("oredict."+category, "enabled", false).set( value.draw );
		}
		
		for( OreInfo ore : custom ){ // Put custom block into the config file.
			String name = ore.oreName.replaceAll("\\s+", "").toLowerCase(); // No whitespace or capitals in the config file categories.
			config.get("customores."+name, "name", "SOMETHINGBROKE").set( ore.oreName );
			config.get("customores."+name, "id", -1).set( ore.id );
			config.get("customores."+name, "meta", -1).set( ore.meta );
			config.get("customores."+name, "color", -1).set( ore.color );
			config.get("customores."+name, "enabled", false).set( ore.draw );
		}
		
	}
}
