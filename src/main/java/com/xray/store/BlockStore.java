package com.xray.store;

import com.xray.reference.block.BlockData;
import com.xray.reference.block.SimpleBlockData;
import com.xray.utils.OutlineColor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;

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

    public void setStore(HashMap<String, BlockData> store) {
        this.store = store;
    }

    public void toggleDrawing(String key ) {
        if( !this.store.containsKey(key) )
            return;

        BlockData data = this.store.get(key);
        data.setDrawing(!data.isDrawing());
    }

    public static HashMap<String, BlockData> getFromSimpleBlockList(List<SimpleBlockData> simpleList)
    {
        HashMap<String, BlockData> blockData = new HashMap<>();

        for (SimpleBlockData e : simpleList) {
            IBlockState state = Block.getStateById(e.getStateId());

            blockData.put(
                    e.getStateString(),
                    new BlockData(
                            e.getName(),
                            e.getStateId(),
                            e.getColor(),
                            new ItemStack( state.getBlock(), 1, state.getBlock().getMetaFromState(state)),
                            e.isDrawing()
                    )
            );
        }

        return blockData;
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
                Block.getStateId(Block.getBlockFromItem(stack.getItem()).getDefaultState()),
                new OutlineColor(color[0], color[1], color[2]),
                stack,
                draw
        );
    }
}
