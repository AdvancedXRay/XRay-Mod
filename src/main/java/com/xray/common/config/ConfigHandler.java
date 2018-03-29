package com.xray.common.config;

import com.xray.client.xray.XrayController;
import com.xray.common.XRay;
import com.xray.common.reference.OreInfo;
import com.xray.common.reference.Reference;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Property;

public class ConfigHandler
{
	public static final String CATEGORY_PREFIX_ORES = "ores";
	public static final List<String> ORDER = new ArrayList<String>() {{ // Sort properties in config file
		add("name");
		add("meta");
		add("red");
		add("green");
		add("blue");
		add("enabled");
		add("useoredict");
	}};

	public static void init(File suggestedConfig) {
		// Creates the config
		XRay.config = new Configuration( suggestedConfig, Reference.CONFIG_VERSION );
		XRay.config.load();
	}

	public static void storeCurrentDist()
	{
		XRay.config.getCategory( Configuration.CATEGORY_GENERAL ).put("searchdist", new Property("searchdist", "" + XrayController.getCurrentDist(), Property.Type.INTEGER) );
	}

	public static void syncConfig()
	{
		ConfigCategory oresCat = XRay.config.getCategory( CATEGORY_PREFIX_ORES );
		XRay.config.removeCategory(oresCat); // Cannot rely on ConfigCategory.clear()
		oresCat = XRay.config.getCategory( CATEGORY_PREFIX_ORES );

		for ( OreInfo ore : XrayController.searchList.getOres() )
		{
			ore.addToConfig(oresCat);
		}
	}

	public static void setup()
	{
		XrayController.setCurrentDist( XRay.config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0).getInt() );
		List<OreInfo> oresToAdd = new ArrayList<>();

		String definedVersion = XRay.config.getDefinedConfigVersion();
		String version = XRay.config.getLoadedConfigVersion();

		if ( !XRay.config.getCategoryNames().contains("ores") ) // No config file (not that here, version always equals definedVersion ...
		{
			oresToAdd.addAll( DefaultConfig.DEFAULT_ORES );
		}
		else if ( !definedVersion.equals( version ) ) // Old config found
		{
			Collection<OreInfo> oresFound = parseOldConfig( version );
			if ( !oresFound.isEmpty() )
				oresToAdd.addAll( oresFound );
			else
				oresToAdd.addAll( DefaultConfig.DEFAULT_ORES );
		}
		else // We have a valid config file
		{
			ConfigCategory oresCat = XRay.config.getCategory( "ores" );
			for ( ConfigCategory oreCat : oresCat.getChildren() )
			{
				try { oresToAdd.add( OreInfo.fromConfigCategory( oreCat ) ); }
				catch (Exception e) // Relying on exceptions is very bad but it saves 400 lines of code
				{
					XRay.logger.warn("Invalid or missing parameter in config" );
				}
			}
		}

		XrayController.searchList.addOres( oresToAdd );
		syncConfig();
		XRay.config.save();
	}

	// TODO: Remove by version 2.0, this will be unneeded by that point, if we detect and old config
	// TODO: we should remove it and start again, with the addition of mods this will likely be the smartest move
	private static Collection<OreInfo> parseOldConfig( String version )
	{
		Collection<OreInfo> ores = new HashSet();

		if ( version == null || version.isEmpty() || "null".equals( version ) ) // No version number (v1.4.0 and before)
		{

			ConfigCategory oresCat = XRay.config.getCategory( "ores" );
			for ( ConfigCategory cat : oresCat.getChildren() )
			{
				try
				{
					Map<String, Property> props = cat.getValues();
					String disp = props.get("name").getString();
					int r = props.get("red").getInt();
					int g = props.get("green").getInt();
					int b = props.get("blue").getInt();
					boolean enabled = props.get("enabled").getBoolean();
					int id = props.get("id").getInt();
					int meta = props.get("meta").getInt();

					Block block = Block.getBlockById( id );
					String name = block.getRegistryName().toString();
					ItemStack stack = new ItemStack(block, 1, meta);
					if ( !stack.isEmpty() )
						name = stack.getItem().getRegistryName().toString();

					String nameFound = stack.isEmpty() ? name : stack.getDisplayName();
					if ( disp.equals(nameFound) )
						ores.add( new OreInfo(name, meta, new int[]{r, g, b}, enabled, false) );
				} catch (Exception ignored) {}
			}
		}
		return ores;
	}
}
