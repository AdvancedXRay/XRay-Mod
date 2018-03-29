package com.xray.common.config;

import com.xray.common.reference.OreInfo;
import java.util.ArrayList;
import java.util.List;

public class DefaultConfig {

	// Put default blocks and settings into the config file.
	public static final List<OreInfo> DEFAULT_ORES = new ArrayList<OreInfo>() {
	{
		add( OreInfo.firstOreInDictionary( "oreIron",         new int[]{170,117, 37}, false ));
		add( OreInfo.firstOreInDictionary( "oreCoal",         new int[]{  0,  0,  0}, false ));
		add( OreInfo.firstOreInDictionary( "oreGold",         new int[]{255,255,  0}, false ));
		add( OreInfo.firstOreInDictionary( "oreRedstone",     new int[]{255,  0,  0}, false ));
		add( OreInfo.firstOreInDictionary( "oreDiamond",      new int[]{136,136,255}, true  ));
		add( OreInfo.firstOreInDictionary( "oreEmerald",      new int[]{  0,136, 10}, true  ));
		add( OreInfo.firstOreInDictionary( "oreQuartz",       new int[]{ 30, 74,  0}, false ));
		add( OreInfo.firstOreInDictionary( "oreLapis",        new int[]{  0,  0,255}, false ));
		add( OreInfo.firstOreInDictionary( "oreCopper",       new int[]{204,102,  0}, false ));
		add( OreInfo.firstOreInDictionary( "oreTin",          new int[]{161,161,161}, false ));
		add( OreInfo.firstOreInDictionary( "oreCobalt",       new int[]{  0,  0,255}, false ));
		add( OreInfo.firstOreInDictionary( "oreArdite",       new int[]{255,153,  0}, false ));
		add( OreInfo.firstOreInDictionary( "oreCertusQuartz", new int[]{255,255,255}, false ));
		add( OreInfo.firstOreInDictionary( "oreUranium",      new int[]{  0,255,  0}, false ));
		add( OreInfo.firstOreInDictionary( "oreSilver",       new int[]{143,143,143}, false ));
	}
	};
}