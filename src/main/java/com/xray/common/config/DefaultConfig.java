package com.xray.common.config;

import com.xray.common.reference.OreInfo;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultConfig {
	// Below are the 'default' ores/blocks to add through the ore dictionary.
	private final static Map<String, OreInfo> defaults = new HashMap<String, OreInfo>()
	{{
		put("oreLapis", 		new OreInfo("lapis", new int[]{0, 0, 255}, false) );
		put("oreCopper", 		new OreInfo("copper", new int[]{204, 102, 0}, true) );
		put("oreTin", 			new OreInfo("tin", new int[]{161, 161, 161}, true) );
		put("oreCobalt", 		new OreInfo("cobalt", new int[]{0, 0, 255}, false) );
		put("oreArdite", 		new OreInfo("ardite", new int[]{255, 153, 0}, false) );
		put("oreCertusQuartz", 	new OreInfo("certusquartz",new int[]{255, 255, 255}, false) );
		put("oreUranium", 		new OreInfo("uranium", new int[]{0, 255, 0}, true) );
		put("oreDiamond", 		new OreInfo("diamond", new int[]{136, 136, 255}, false) );
		put("oreEmerald", 		new OreInfo("emerald",new int[]{0, 136, 10}, true) );
		put("oreGold", 			new OreInfo("gold", new int[]{255, 255, 0}, false) );
		put("oreRedstone", 		new OreInfo("redstone", new int[]{255, 0, 0}, false) );
		put("oreIron", 			new OreInfo("iron", new int[]{170, 117, 37}, false) );
		put("oreSilver", 		new OreInfo("silver", new int[]{143,143,143}, false) );
		put("oreQuartz", 		new OreInfo("quartz", new int[]{30,74,0}, false) );
		put("oreCoal", 			new OreInfo("coal", new int[]{0, 0, 0}, false ) );
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

			List<ItemStack> listStack = OreDictionary.getOres(key);
			if( listStack.size() > 1 || listStack.isEmpty() )
				continue;

			ItemStack stack = listStack.get(0); // We only want the main block. This might be different once mod support is added
			Block tmpBlock = Block.getBlockFromItem( stack.getItem() );

			String category = value.oreName; // some what useless but cleaner..
			config.get("ores."+category, "name", "").set( stack.getDisplayName() );
			config.get("ores."+category, "id", -1).set( Block.getIdFromBlock( tmpBlock ) );
			config.get("ores."+category, "meta", -1).set( stack.getMetadata() );
			config.get("ores."+category, "red", -1).set( value.color[0] );
			config.get("ores."+category, "green", -1).set( value.color[1] );
			config.get("ores."+category, "blue", -1).set( value.color[2] );
			config.get("ores."+category, "enabled", false).set( value.draw );
		}
	}
}