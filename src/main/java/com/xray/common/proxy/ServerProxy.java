package com.xray.common.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class ServerProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event) {
	    super.preInit(event);
	}

	@Override
	public void init(FMLInitializationEvent event) {
	    super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
	    super.postInit(event);
	}

	@Override
	public void onExit(FMLServerStoppingEvent event) {
		super.onExit(event);
	}
}
