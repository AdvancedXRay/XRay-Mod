package com.xray.reference.block;

import com.xray.utils.OutlineColor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class SimpleBlockData {

    private String name;
    private String stateString;
    private int stateId;
    private int order;

    private OutlineColor color;
    private boolean drawing;

    public SimpleBlockData(String name, String stateString, int stateId, OutlineColor color, boolean drawing, int order) {
        this.name = name;
        this.stateString = stateString;
        this.stateId = stateId;
        this.color = color;
        this.drawing = drawing;
        this.order = order;
    }

    /**
     * Helper for creating default config from ore dictionary names.
     * Given an ore name, tries to find an actual instance of such ore.
     * @param name OreDictionary name (eg. oreIron)
     * @param color a color for this ore
     * @param draw shall we draw it by default?
     * @return An ore registered with this dictionary name, null if none found
     */
    public static SimpleBlockData firstOreInDictionary( String name, String entryName, int[] color, boolean draw )
    {
        NonNullList<ItemStack> ores = OreDictionary.getOres( name );
        if ( ores.isEmpty() || ores.get(0).isEmpty() )
            return null;

        ItemStack stack = ores.get( 0 );

        return new SimpleBlockData(
                entryName,
                Block.getBlockFromItem(stack.getItem()).getDefaultState().toString(),
                Block.getStateId(Block.getBlockFromItem(stack.getItem()).getDefaultState()),
                new OutlineColor(color[0], color[1], color[2]),
                draw,
                0
        );
    }

    public String getName() {
        return name;
    }

    public String getStateString() {
        return stateString;
    }

    public OutlineColor getColor() {
        return color;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public int getStateId() {
        return stateId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
