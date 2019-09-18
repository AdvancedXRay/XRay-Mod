package com.xray.reference.block;

import com.xray.utils.OutlineColor;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NBTUtil;

public class SimpleBlockData {

    private String name;
    private String stateString;
    private int order;

    private OutlineColor color;
    private boolean drawing;

    public SimpleBlockData(String name, BlockState stateString, OutlineColor color, boolean drawing, int order) {
        this.name = name;
        this.stateString = NBTUtil.writeBlockState(stateString).toString();
        this.color = color;
        this.drawing = drawing;
        this.order = order;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
