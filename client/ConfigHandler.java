package fgtXray.client;

import java.io.File;

import scala.util.parsing.combinator.testing.Str;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import fgtXray.FgtXRay;
import fgtXray.OreInfo;

public class ConfigHandler {
	static Configuration config = null; // Save the config file handle for use later.
	
	public ConfigHandler() {}
	
	public void saveAll(){}// I shouldnt need this. Placeholder for saving all options at once.

	public static void setup(FMLPreInitializationEvent event ) {
		config = new Configuration( event.getSuggestedConfigurationFile() );
		config.load();
		FgtXRay.distIndex = config.get(config.CATEGORY_GENERAL, "searchdist", 0).getInt(); // Get our search distance.
		
		for( String category : config.getCategoryNames() ){ // Iterate through each category in our config file.
			ConfigCategory cat = config.getCategory( category );
			
			if( category.contains("oredict.") ){ // Dont iterate over the base category and make sure were on the oredict category.
				String dictName = cat.get("dictname").getString();
				String guiName = cat.get("guiname").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int color = cat.get("color").getInt();
				boolean enabled = cat.get("enabled").getBoolean(false);
				
				FgtXRay.oredictOres.put(dictName, new OreInfo( guiName, id, meta, color, enabled ) );
				
			} else if( category.contains("customores.") ){
				String name = cat.get("name").getString();
				int id = cat.get("id").getInt();
				int meta = cat.get("meta").getInt();
				int color = cat.get("color").getInt();
				boolean enabled = cat.get("enabled").getBoolean(false);
				
				FgtXRay.customOres.add( new OreInfo( name, id, meta, color, enabled ) );
			}
		}
		config.save();
	}
	
	public static void update(String string, boolean draw){
		if( string == "searchdist" ){ // Save the new render distance.
			config.get(config.CATEGORY_GENERAL, "searchdist", 0).set( FgtXRay.distIndex );
			config.save();
			return;
		}
		
		for( String category : config.getCategoryNames() ){ // Figure out if this is a custom or dictionary ore.
			String cleanStr = string.replaceAll("\\s+", "").toLowerCase(); // No whitespace or capitals in the config file categories.
			
			if( category.contains( cleanStr ) ){
				if( category.contains("oredict") ){
					config.get("oredict."+cleanStr, "enabled", false).set( draw );
				} else {
					config.get("customores."+cleanStr, "enabled", false).set( draw );
				}
			}
		}
		config.save();
	}
}