package com.xray.common;

import com.xray.common.reference.Reference;
import jdk.nashorn.internal.ir.annotations.Ignore;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Property;

@Config(modid = Reference.MOD_ID, name = "advanced_xray", type = Config.Type.INSTANCE)
public class Configuration
{
//    public static String version = "";

    @Config.Comment({
            "DO NOT TOUCH!",
            "This setting is for memory only and if changed to a value not supported",
            "the game will crash on start up. This value can be changed in game very simply.",
            "If you must change it then these are the valid values {8, 16, 32, 48, 64, 80, 128, 256}",
            "but please leave it alone :P"
    })
    @Config.RangeInt(min = 16, max = 256)
    public static int radius = 32;

    @Config.Name("Show XRay overlay")
    @Config.Comment("This allows you hide or show the overlay in the top right of the screen when XRay is enabled")
    public static boolean showOverlay = true;

    @Config.Name("Fading Affect")
    @Config.Comment({
            "By default the blocks will begin to fade out as you get further away, some may not like this as",
            "it can sometime causes things not to show. If you're a dirty cheater (lol) then you may want",
            "to disable this as you won't be able to see chests out of your range."
    })
    public static boolean shouldFade = true;

    @Config.Name("Outline thickness")
    @Config.Comment({
            "This allows you to set your own outline thickness, I find that 1.0 is perfect but others my",
            "think differently. The max is 5.0"
    })
    @Config.RangeDouble(min = 0.5, max = 5.0)
    public static double outlineThickness = 1f;

//	public static float outlineThickness = 1f;
//	public static float outlineOpacity = 1f;
//	public static boolean showOverlay = true;
//	public static boolean shouldFade = true;
//
//	private static final boolean isDisabled = true;
//
//	private static final String CATEGORY_PREFIX_BLOCKS = "blocks";
//	public static final List<String> ORDER = new ArrayList<String>() {{ // Sort properties in config file
//		add("entry-name");
//		add("state-id");
//		add("default");
//		add("red");
//		add("green");
//		add("blue");
//		add("enabled");
//	}};
//
//	public static void init(File suggestedConfig) {
//		// Creates the config
//		XRay.config = new Configuration( suggestedConfig, Reference.CONFIG_VERSION );
//		XRay.config.load();
//	}
//
//
//	public static void syncConfig()
//	{
//		ConfigCategory cat = XRay.config.getCategory( CATEGORY_PREFIX_BLOCKS );
//		XRay.config.removeCategory(cat);
//        cat = XRay.config.getCategory( CATEGORY_PREFIX_BLOCKS );
//
//        for (Map.Entry<String, BlockData> store : XRayController.getBlockStore().getStore().entrySet()) {
//			ConfigCategory newCat = new ConfigCategory( store.getValue().getState().toString(), cat);
//
//			newCat.put("entry-name",   new Property("entry-name", store.getValue().getEntryName(), Property.Type.STRING));
//			newCat.put("state-id",     new Property("state-id", "" + Block.getStateId(store.getValue().getState()), Property.Type.INTEGER));
//			newCat.put("default",      new Property("default", "" + store.getValue().isDefault(), Property.Type.BOOLEAN));
//			newCat.put("red",          new Property("red", "" + store.getValue().getOutline().getRed(), Property.Type.INTEGER));
//			newCat.put("green",        new Property("green", "" + store.getValue().getOutline().getGreen(), Property.Type.INTEGER));
//			newCat.put("blue",         new Property("blue", "" + store.getValue().getOutline().getBlue(), Property.Type.INTEGER));
//			newCat.put("enabled",      new Property("enabled", "" + store.getValue().isDrawing(), Property.Type.BOOLEAN));
//
//			newCat.setPropertyOrder( Configuration.ORDER );
//        }
//	}
//
//	public static void setup()
//	{
//		XRayController.setCurrentDist( XRay.config.get(Configuration.CATEGORY_GENERAL, "searchdist", 0).getInt() );
//
//		// Overlay
//		showOverlay = XRay.config.get(Configuration.CATEGORY_GENERAL, "show-overlay", true).getBoolean();
//
//		if( isDisabled )
//			return;
//
//		List<BlockData> blockQueue = new ArrayList<>();
//
//		String definedVersion = XRay.config.getDefinedConfigVersion();
//		String version = XRay.config.getLoadedConfigVersion() == null ? "1.4" : XRay.config.getLoadedConfigVersion();
//
//		if ( !XRay.config.getCategoryNames().contains(CATEGORY_PREFIX_BLOCKS) ) // No config file (not that here, version always equals definedVersion ...
//		{
//			blockQueue.addAll( DefaultConfig.DEFAULT_ORES );
//		}
//
//		else if ( !definedVersion.equals( version ) ) // Old config found
//		{
//            // Delete config. Recreate...
//            // TODO: ^
//            blockQueue.addAll( DefaultConfig.DEFAULT_ORES );
//		}
//		else // We have a valid config file
//		{
//			ConfigCategory blockCat = XRay.config.getCategory( CATEGORY_PREFIX_BLOCKS );
//			for ( ConfigCategory cat : blockCat.getChildren() )
//			{
//				try {
//				    IBlockState block = Block.getStateById(cat.get("state-id").getInt());
//                    blockQueue.add(new BlockData(
//                            block.getBlock().getRegistryName(),
//                            cat.get("entry-name").getString(),
//                            new OutlineColor(
//                                    Utils.clampColor(cat.get("red").getInt()),
//                                    Utils.clampColor(cat.get("green").getInt()),
//                                    Utils.clampColor(cat.get("blue").getInt())
//                            ),
//                            cat.get("default").getBoolean(),
//                            block,
//                            new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block)),
//                            cat.get("enabled").getBoolean()
//                    ));
//                }
//				catch (Exception e) // Relying on exceptions is very bad but it saves 400 lines of code
//				{
//					XRay.logger.warn("Invalid or missing parameter in config" );
//				}
//			}
//		}
//
//        for (BlockData data: blockQueue) {
//            if( data == null )
//                continue;
//
//            XRayController.getBlockStore().put(
//					data.getState().toString(),
//                    data
//			);
//        }
//
//		syncConfig();
//		XRay.config.save();
//	}
}
