package com.xray.common.reference;

import com.xray.common.utils.OutlineColor;

public class ColorName {

    private String name;
    private OutlineColor color;

    public ColorName(String name, OutlineColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public OutlineColor getColor() {
        return color;
    }
}
