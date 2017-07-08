package com.xray.client;

import java.util.*;

import com.xray.common.XRay;
import com.xray.common.config.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.oredict.OreDictionary;

import com.xray.common.reference.OreInfo;

public class OresSearch
{
	public static ArrayList<OreInfo> searchList = new ArrayList<>(); // List of ores/blocks to search for.
	private static Minecraft mc = Minecraft.getMinecraft();
	
	private static boolean checkList( List<OreInfo> temp, OreInfo value, ItemStack stack ) // Used to check if a OreInfo already exists in the searchList
	{
		for( OreInfo oreCheck : temp )
		{
			if( (Objects.equals(oreCheck.oreName, value.oreName)) && (oreCheck.id == Item.getIdFromItem( stack.getItem() ) ) && (oreCheck.meta == stack.getItemDamage()) )
			{
				return true; // This ore already exists in the temp list. (Sometimes the OreDict returns duplicate entries, like gold twice) 
			}
		}
		return false;
	}

	public static void add( int oreId, int oreMeta, String name, int[] color ) // Takes a string of id:meta or oreName to add to our search list.
	{
		if( name.equals("") ) {
			mc.ingameGUI.getChatGUI().printChatMessage( new TextComponentString( "[XRay] You need to have all the inputs filled" ));
			return;
		}

		for( OreInfo info : OresSearch.searchList ) {
			if( info.getId() == oreId && info.getMeta() == oreMeta) {
				mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("[XRay] This block has already been added to the block list"));
				return;
			}
		}

		OresSearch.searchList.add( new OreInfo( name, oreId, oreMeta, color, true ) );
		String notify = String.format( "[XRay] successfully added %s.", name );
		mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(notify));

		ConfigHandler.add(name, oreId, oreMeta, color);
	}

	public static void update( OreInfo original, String name, int[] color ) {
		if( !OresSearch.searchList.contains( original ) ) {
			// This really shouldn't happen but hay, lets support it anyway.
			mc.ingameGUI.getChatGUI().printChatMessage( new TextComponentString( "[XRay] Looks like the Ore you've tried to edit does not exist?" ));
			return;
		}

		OreInfo tmpNew = null;
		for ( OreInfo ore : OresSearch.searchList ) {
			if( ore == original ) {
				ore.oreName = name;
				ore.color = color;
				tmpNew = ore;
				break;
			}
		}

		if( tmpNew != null ) {
			ConfigHandler.updateInfo(original, tmpNew);
			String notify = String.format( "[XRay] successfully updated %s.", original.getOreName() );
			mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(notify));
		} else {
			mc.ingameGUI.getChatGUI().printChatMessage( new TextComponentString( "[XRay] Looks like the Ore you've tried to edit does not exist?" ));
		}
	}
	
	public static List<OreInfo> get() // Return the searchList, create it if needed.
	{
		if( OresSearch.searchList.isEmpty() )
		{
			System.out.println( "[XRay] --- Populating the searchList with the ore dictionary --- ");
			List<OreInfo> temp = new ArrayList<>(); // Temporary array of OreInfos to replace searchList
			Map<String, OreInfo> tempOredict = new HashMap<String, OreInfo>(); // Temporary oredict map to replace oredictOres
			
			// OreDictionary.getOres("string") adds the ore if it doesn't exist.
			// Here we check our oredictOres with the untouched oredict and delete any that dont exist already. This avoids polluting the oredict.
			for( String oreName : OreDictionary.getOreNames() )
			{
				if( XRay.oredictOres.containsKey( oreName ) )
				{
					tempOredict.put( oreName, XRay.oredictOres.get( oreName ) );
					//System.out.println( String.foramt( "[Fgt XRay]: Found ore %s in dictionary, adding.", oreName ) );
				}
			}
			// Debug loop to notify of invalid and removed oreDict names.
			for( Map.Entry<String, OreInfo> entry : XRay.oredictOres.entrySet() )
			{
				String key = entry.getKey();
				if( !tempOredict.containsKey( key ) )
				{
					System.out.println( String.format( "[XRay] Ore %s doesn't exist in dictionary! Deleting.", key ) );
				}
			}
			XRay.oredictOres.clear();
			XRay.oredictOres.putAll( tempOredict );
			tempOredict.clear();
			
			// Now we can iterate over the clean oredictOres and get all the different types of oreName
			for( Map.Entry<String, OreInfo> entry : XRay.oredictOres.entrySet() )
			{
				String key = entry.getKey(); // oreName string
				OreInfo value = entry.getValue(); // OreInfo class
				
				List<ItemStack> oreDictOres = OreDictionary.getOres( key ); // Get an itemstack array of all the oredict ores for 'key'
				if( oreDictOres.size() < 1 )
				{
					System.out.println( String.format( "[XRay] Ore %s doesn't exist! Skipping. (We shouldn't have this issue here! Please tell me about this!)", key ) );
					continue;
				}
				for (ItemStack oreItem : oreDictOres) {
					if (checkList(temp, value, oreItem)) {
						System.out.println("[XRay] Duplicate ore found in Ore Dictionary!!! (" + key + ")");
						continue;
					}
					temp.add(new OreInfo(value.oreName, Item.getIdFromItem(oreItem.getItem()), oreItem.getItemDamage(), value.color, value.draw));
					//System.out.println( String.format("[Fgt XRay] Adding OreInfo( %s, %d, %d, %s, %b ) ", value.oreName, Item.getIdFromItem( oreItem.getItem() ), oreItem.getItemDamage(), value.color[0], value.draw ) );
				}
			}
			System.out.println( "[XRay] --- Done populating searchList! --- ");
			System.out.println( "[XRay] --- Adding custom blocks --- ");
			
			for( OreInfo ore : XRay.customOres ) //TODO: Check if custom already exists
			{
				System.out.println( String.format( "[XRay] Adding OreInfo( %s, %d, %d, %b ) ", ore.oreName, ore.id, ore.meta, ore.draw ) );
				temp.add( ore );
			}
			System.out.println( "[XRay] --- Done adding custom blocks --- ");
			
			OresSearch.searchList.clear();
			OresSearch.searchList.addAll( temp );
			
		}
		return OresSearch.searchList;
	}
}
