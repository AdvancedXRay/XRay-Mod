package com.xray.config;

import com.xray.reference.block.BlockData;
import com.xray.reference.block.BlockStore;

import java.util.ArrayList;
import java.util.List;

class DefaultConfig {

	// Put default blocks and settings into the config file.
	static final List<BlockData> DEFAULT_ORES = new ArrayList<BlockData>() {
		{
			add( BlockStore.firstOreInDictionary( "oreIron",         new int[]{170,117, 37}, false ));
			add( BlockStore.firstOreInDictionary( "oreCoal",         new int[]{  0,  0,  0}, false ));
			add( BlockStore.firstOreInDictionary( "oreGold",         new int[]{255,255,  0}, false ));
			add( BlockStore.firstOreInDictionary( "oreRedstone",     new int[]{255,  0,  0}, false ));
			add( BlockStore.firstOreInDictionary( "oreDiamond",      new int[]{136,136,255}, true  ));
			add( BlockStore.firstOreInDictionary( "oreEmerald",      new int[]{  0,136, 10}, true  ));
			add( BlockStore.firstOreInDictionary( "oreQuartz",       new int[]{ 30, 74,  0}, false ));
			add( BlockStore.firstOreInDictionary( "oreLapis",        new int[]{  0,  0,255}, false ));
			add( BlockStore.firstOreInDictionary( "oreCopper",       new int[]{204,102,  0}, false ));
			add( BlockStore.firstOreInDictionary( "oreTin",          new int[]{161,161,161}, false ));
			add( BlockStore.firstOreInDictionary( "oreCobalt",       new int[]{  0,  0,255}, false ));
			add( BlockStore.firstOreInDictionary( "oreArdite",       new int[]{255,153,  0}, false ));
			add( BlockStore.firstOreInDictionary( "oreCertusQuartz", new int[]{255,255,255}, false ));
			add( BlockStore.firstOreInDictionary( "oreUranium",      new int[]{  0,255,  0}, false ));
			add( BlockStore.firstOreInDictionary( "oreSilver",       new int[]{143,143,143}, false ));
		}
	};
}