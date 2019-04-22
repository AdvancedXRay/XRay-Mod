package com.xray.reference.block;

import com.xray.utils.OutlineColor;

public class SimpleBlockData {

    private String name;
    private String stateString;
    private int stateId;

    private OutlineColor color;
    private boolean drawing;

    public SimpleBlockData(String name, String stateString, int stateId, OutlineColor color, boolean drawing) {
        this.name = name;
        this.stateString = stateString;
        this.stateId = stateId;
        this.color = color;
        this.drawing = drawing;
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
}
