package com.xray.common;

import com.xray.client.gui.helper.HelperBlock;
import com.xray.client.render.ClientTick;
import com.xray.client.render.XrayRenderer;
import com.xray.common.config.ConfigHandler;
import com.xray.common.proxy.CommonProxy;
import com.xray.common.reference.OreInfo;
import com.xray.common.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;

@Mod(modid= Reference.MOD_ID, name= Reference.MOD_NAME, version=Reference.MOD_VERSION /*guiFactory = Reference.GUI_FACTORY*/)
public class XRay
{
	public static int localPlyX, localPlyY, localPlyZ, localPlyXPrev, localPlyZPrev; // For internal use in the ClientTick thread.
	private static boolean drawOres = false; // Off by default
	public static boolean drawCaves = false;
	public static ArrayList<HelperBlock> blockList = new ArrayList<>();
        private static Minecraft mc = Minecraft.getMinecraft();

	// Config settings
	public static Configuration config;
    public static int currentDist = 0; // Index for the distNumers array. Default search distance.
	public static float outlineThickness = 1f;
	public static float outlineOpacity = 1f;

    // Radius +/- around the player to search. So 8 is 8 on left and right of player plus under the player. So 17x17 area.
    public static final int[] distNumbers = new int[] {8, 16, 32, 48, 64, 80, 128, 256};

    // Keybindings
	public static final int keyIndex_toggleXray = 0;
	public static final int keyIndex_showXrayMenu = 1;
	public static final int[] keyBind_keyValues = { Keyboard.KEY_BACKSLASH, Keyboard.KEY_Z };
	public static final String[] keyBind_descriptions = { I18n.format("xray.config.toggle"), I18n.format("xray.config.open")};
	public static KeyBinding[] keyBind_keys = null;

	public static ArrayList<OreInfo> searchList = new ArrayList<>(); // List of ores/blocks to search for.

        // ClientTick thread management
        private static volatile boolean findingBlocks = false;
        private static ExecutorService EXECUTOR;

	// The instance of your mod that Forge uses.
	@Instance(Reference.MOD_ID)
	public static XRay instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="com.xray.common.proxy.ClientProxy", serverSide="com.xray.common.proxy.ServerProxy")
	private static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
    {
		config = new Configuration( event.getSuggestedConfigurationFile() );

		ConfigHandler.init(event.getSuggestedConfigurationFile(), config);

		ConfigHandler.setup( event ); // Read the config file and setup environment.
        System.out.println(I18n.format("xray.debug.init"));

		proxy.preInit( event );
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
    {
		proxy.init( event );
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
    {
		for ( Block block : ForgeRegistries.BLOCKS ) {
			NonNullList<ItemStack> subBlocks = NonNullList.create();
			block.getSubBlocks( block.getCreativeTabToDisplayOn(), subBlocks );
			for( ItemStack subBlock : subBlocks ) {
				if (subBlock.isEmpty())
					continue;

				Block tmpBlock = Block.getBlockFromItem( subBlock.getItem() );
				blockList.add( new HelperBlock( subBlock.getDisplayName(), tmpBlock, subBlock, subBlock.getItem(), subBlock.getItem().getRegistryName() ));
			}
		}

		proxy.postInit( event );
	}

	public static boolean drawOres()
	{
		return drawOres;
	}

	public static void toggleDrawOres()
	{
		drawOres = !drawOres;
		if ( drawOres )
			EXECUTOR = Executors.newSingleThreadExecutor();
		else
			EXECUTOR.shutdownNow();
	}

	/**
	 * Starts the ClientTick thread if it's not already running.
	 *
	 * @param force should we force a block scan even if the player hasn't moved?
	 */
	public static synchronized void requestBlockFinder( boolean force )
	{
		if ( !findingBlocks && drawOres && mc.world != null && mc.player != null )
		{
			if ( force )
			{
				XrayRenderer.ores.clear(); // This forces blockFinder() execution
			}
			findingBlocks = true; // Prevents other executions until this thread finishes. The thread itself resets it on completion.
			EXECUTOR.execute( new ClientTick() );
		}
	}

	/**
	 * Should only be called by ClientTick! Maybe a security token to mimic
	 * 'friend' methods would be useful here.
	 */
	public void doneFindingBlocks()
	{
		findingBlocks = false;
	}
}
