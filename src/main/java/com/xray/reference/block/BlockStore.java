package com.xray.reference.block;

import com.xray.utils.OutlineColor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class BlockStore {

    private HashMap<String, BlockData> store = new HashMap<>();

    public void put(String key, BlockData data) {
        if (this.store.containsKey(key))
            return;

        this.store.put(key, data);
    }

    public HashMap<String, BlockData> getStore() {
        return this.store;
    }

    public void toggleDrawing( String key ) {
        if( !this.store.containsKey(key) )
            return;

        BlockData data = this.store.get(key);
        data.setDrawing(!data.isDrawing());
    }

    /**
     * Helper for creating default config from ore dictionary names.
     * Given an ore name, tries to find an actual instance of such ore.
     * @param name OreDictionary name (eg. oreIron)
     * @param color a color for this ore
     * @param draw shall we draw it by default?
     * @return An ore registered with this dictionary name, null if none found
     */
    public static BlockData firstOreInDictionary( String name, int[] color, boolean draw )
    {
        NonNullList<ItemStack> ores = OreDictionary.getOres( name );
        if ( ores.isEmpty() || ores.get(0).isEmpty() )
            return null;

        ItemStack stack = ores.get( 0 );

        return new BlockData(
                stack.getDisplayName(),
                new OutlineColor(color[0], color[1], color[2]),
                stack,
                draw
        );
    }
}
