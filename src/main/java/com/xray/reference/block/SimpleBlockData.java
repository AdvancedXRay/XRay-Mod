package com.xray.reference.block;

import com.xray.utils.OutlineColor;

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
