package com.xray.common.config;

import com.xray.client.xray.XRayController;
import com.xray.common.XRay;
import com.xray.common.reference.Reference;
import com.xray.common.reference.block.BlockData;
import com.xray.common.utils.OutlineColor;
import com.xray.common.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class ConfigHandler
{
	public static float outlineThickness = 1f;
	public static float outlineOpacity = 1f;
	public static boolean showOverlay = true;

	private static final String CATEGORY_PREFIX_BLOCKS = "blocks";
	public static final List<String> ORDER = new ArrayList<String>() {{ // Sort properties in config file
		add("entry-name");
		add("state-id");
		add("default");
		add("red");
		add("green");
		add("blue");
		add("enabled");
	}};

	public static void init(File suggestedConfig) {
		// Creates the config
		XRay.config = new Configuration( suggestedConfig, Reference.CONFIG_VERSION );
		XRay.config.load();
	}

	public static void storeCurrentDist()
	{
		XRay.config.getCategory( Configuration.CATEGORY_GENERAL ).put("searchdist", new Property("searchdist", "" + XRayController.getCurrentDist(), Property.Type.INTEGER) );
	}

	public static void syncConfig()
	{
		ConfigCategory cat = XRay.config.getCategory( CATEGORY_PREFIX_BLOCKS );
		XRay.config.removeCategory(cat);
        cat = XRay.config.getCategory( CATEGORY_PREFIX_BLOCKS );

        for (Map.Entry<String, Deque<BlockData>> store : XRayController.getBlockStore().getStore().entrySet()) {
            for (BlockData data: store.getValue()) {

                cat.put("entry-name",   new Property("entry-name", data.getEntryName(), Property.Type.STRING));
                cat.put("state-id",     new Property("state-id", "" + Block.getStateId(data.getState()), Property.Type.INTEGER));
                cat.put("default",      new Property("default", "" + data.isDefault(), Property.Type.BOOLEAN));
                cat.put("red",          new Property("red", "" + data.getOutline().getRed(), Property.Type.INTEGER));
                cat.put("green",        new Property("green", "" + data.getOutline().getGreen(), Property.Type.INTEGER));
                cat.put("blue",         new Property("blue", "" + data.getOutline().getBlue(), Property.Type.INTEGER));
                cat.put("enabled",      new Property("enabled", "" + data.isDrawing(), Property.Type.BOOLEAN));

                cat.setPropertyOrder( ConfigHandler.ORDER );
            }
        }
	}

	public static void setup()
	{
		XRayController.setCurrentDist( XRay.config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0).getInt() );

		// Overlay
		showOverlay = XRay.config.get(Configuration.CATEGORY_GENERAL, "show-overlay", true).getBoolean();

		List<BlockData> blockQueue = new ArrayList<>();

		String definedVersion = XRay.config.getDefinedConfigVersion();
		String version = XRay.config.getLoadedConfigVersion() == null ? "1.4" : XRay.config.getLoadedConfigVersion();

		if ( !XRay.config.getCategoryNames().contains(CATEGORY_PREFIX_BLOCKS) ) // No config file (not that here, version always equals definedVersion ...
		{
			blockQueue.addAll( DefaultConfig.DEFAULT_ORES );
		}

		else if ( !definedVersion.equals( version ) ) // Old config found
		{
            // Delete config. Recreate...
            // TODO: ^
            blockQueue.addAll( DefaultConfig.DEFAULT_ORES );
		}
		else // We have a valid config file
		{
			ConfigCategory blockCat = XRay.config.getCategory( CATEGORY_PREFIX_BLOCKS );
			for ( ConfigCategory cat : blockCat.getChildren() )
			{
				try {
				    IBlockState block = Block.getStateById(cat.get("state-id").getInt());
                    blockQueue.add(new BlockData(
                            block.getBlock().getRegistryName(),
                            cat.get("entry-name").getString(),
                            new OutlineColor(
                                    Utils.clampColor(cat.get("red").getInt()),
                                    Utils.clampColor(cat.get("green").getInt()),
                                    Utils.clampColor(cat.get("blue").getInt())
                            ),
                            cat.get("default").getBoolean(),
                            block,
                            new ItemStack(block.getBlock()),
                            cat.get("enabled").getBoolean()
                    ));
                }
				catch (Exception e) // Relying on exceptions is very bad but it saves 400 lines of code
				{
					XRay.logger.warn("Invalid or missing parameter in config" );
				}
			}
		}

        for (BlockData data: blockQueue)
            XRayController.getBlockStore().putBlock(data.getState().getBlock().getLocalizedName(), data);

		syncConfig();
		XRay.config.save();
	}

	// TODO: Remove by version 2.0, this will be unneeded by that point, if we detect and old config
	// TODO: we should remove it and start again, with the addition of mods this will likely be the smartest move
//	private static Collection<OreInfo> parseOldConfig( String version )
//	{
//		Collection<OreInfo> ores = new HashSet();
//
//		if ( version == null || version.isEmpty() || "null".equals( version ) ) // No version number (v1.4.0 and before)
//		{
//
//			ConfigCategory oresCat = XRay.config.getCategory(CATEGORY_PREFIX_BLOCKS);
//			for ( ConfigCategory cat : oresCat.getChildren() )
//			{
//				try
//				{
//					Map<String, Property> props = cat.getValues();
//					String disp = props.get("name").getString();
//					int r = props.get("red").getInt();
//					int g = props.get("green").getInt();
//					int b = props.get("blue").getInt();
//					boolean enabled = props.get("enabled").getBoolean();
//					int id = props.get("id").getInt();
//					int meta = props.get("meta").getInt();
//
//					Block block = Block.getBlockById( id );
//					String name = block.getRegistryName().toString();
//					ItemStack stack = new ItemStack(block, 1, meta);
//					if ( !stack.isEmpty() )
//						name = stack.getItem().getRegistryName().toString();
//
//					String nameFound = stack.isEmpty() ? name : stack.getDisplayName();
//					if ( disp.equals(nameFound) )
//						ores.add( new OreInfo(name, meta, new int[]{r, g, b}, enabled, false) );
//				} catch (Exception ignored) {}
//			}
//		}
//		return ores;
//	}
}
