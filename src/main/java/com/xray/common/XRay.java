package com.xray.common;

import com.xray.client.gui.helper.HelperBlock;
import com.xray.common.config.ConfigHandler;
import com.xray.common.config.DefaultConfig;
import com.xray.common.proxy.ServerProxy;
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

@Mod(modid= Reference.MOD_ID, name= Reference.MOD_NAME, version=Reference.MOD_VERSION /*guiFactory = Reference.GUI_FACTORY*/)
public class XRay
{
	public static int localPlyX, localPlyY, localPlyZ, localPlyXPrev, localPlyZPrev; // For internal use in the ClientTick thread.
	public static boolean drawOres = false; // Off by default
	public static ArrayList<HelperBlock> blockList = new ArrayList<>();

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

	// The instance of your mod that Forge uses.
	@Instance(Reference.MOD_ID)
	public static XRay instance;
	
	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="com.xray.client.proxy.ClientProxy", serverSide="com.xray.common.proxy.ServerProxy")
	private static ServerProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
    {
		config = new Configuration( event.getSuggestedConfigurationFile() );

		ConfigHandler.init(event.getSuggestedConfigurationFile(), config);

		ConfigHandler.setup( event ); // Read the config file and setup environment.
        System.out.println(I18n.format("xray.debug.init"));
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
    {
		proxy.proxyInit();
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
	}
}
