package com.xray.utils;

public class OutlineColor {
    private int r;
    private int g;
    private int b;

    public OutlineColor(int[] color)
    {
        this( color[0], color[1], color[2] );
    }

    public OutlineColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed() { return this.r; }
    public int getGreen() { return this.g; }
    public int getBlue() { return this.b; }

    public int[] getColor() {
        return new int[] {this.r, this.g, this.b};
    }
}
