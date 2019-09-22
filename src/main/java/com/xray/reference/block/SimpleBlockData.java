package com.xray.reference.block;

import com.xray.utils.OutlineColor;

public class SimpleBlockData {

    private String name;
    private String blockName;
    private int order;

    private OutlineColor color;
    private boolean drawing;

    public SimpleBlockData(String name, String blockName, OutlineColor color, boolean drawing, int order) {
        this.name = name;
        this.blockName = blockName;
        this.color = color;
        this.drawing = drawing;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public String getBlockName() {
        return blockName;
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
