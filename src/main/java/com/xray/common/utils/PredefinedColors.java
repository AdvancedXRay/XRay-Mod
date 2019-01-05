package com.xray.common.utils;

import com.xray.common.reference.ColorName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PredefinedColors {

    private static final List<ColorName> colors = new ArrayList<ColorName>() {{
        add(new ColorName("Black",        new OutlineColor(0, 0, 0)));
        add(new ColorName("White",        new OutlineColor(255, 255, 255)));
        add(new ColorName("Red",          new OutlineColor(255, 0, 0)));
        add(new ColorName("Firebrick",    new OutlineColor(178, 34, 34)));
        add(new ColorName("Crimson",      new OutlineColor(220, 20, 60)));
        add(new ColorName("Pink",         new OutlineColor(255, 192, 203)));
        add(new ColorName("Hot Pink",     new OutlineColor(255, 105, 180)));
        add(new ColorName("Deep Pink",    new OutlineColor(255, 20, 147)));
        add(new ColorName("Coral",        new OutlineColor(255, 127, 80)));
        add(new ColorName("Orange Red",   new OutlineColor(255, 69, 0)));
        add(new ColorName("Orange",       new OutlineColor(255, 165, 0)));
        add(new ColorName("Yellow",       new OutlineColor(255, 255, 0)));
        add(new ColorName("Gold",         new OutlineColor(255, 215, 0)));
        add(new ColorName("Moccasin",     new OutlineColor(255, 228, 181)));
        add(new ColorName("Lavender",     new OutlineColor(230, 230, 250)));
        add(new ColorName("Violet",       new OutlineColor(238, 130, 238)));
        add(new ColorName("Purple",       new OutlineColor(147, 112, 219)));
        add(new ColorName("Blue Violet",  new OutlineColor(138, 43, 226)));
        add(new ColorName("Slate Blue",   new OutlineColor(106, 90, 205)));
        add(new ColorName("Lime",         new OutlineColor(0, 255, 0)));
        add(new ColorName("Pale Green",   new OutlineColor(144, 238, 144)));
        add(new ColorName("Green",        new OutlineColor(0, 128, 0)));
        add(new ColorName("Dark Cyan",    new OutlineColor(0, 139, 139)));
        add(new ColorName("Cyan",         new OutlineColor(0, 255, 255)));
        add(new ColorName("Aquamarine",   new OutlineColor(127, 255, 212)));
        add(new ColorName("Turquoise",    new OutlineColor(64, 224, 208)));
        add(new ColorName("Sky Blue",     new OutlineColor(135, 206, 235)));
        add(new ColorName("Blue",         new OutlineColor(0, 0, 255)));
        add(new ColorName("Navy",         new OutlineColor(0, 0, 128)));
        add(new ColorName("Shady Brown",  new OutlineColor(244,164, 96)));
        add(new ColorName("Chocolate",    new OutlineColor(210, 105, 30)));
        add(new ColorName("Brown",        new OutlineColor(165, 42, 42)));
        add(new ColorName("Gray",         new OutlineColor(128, 128, 128)));
        add(new ColorName("Silver",       new OutlineColor(192, 192, 192)));
        add(new ColorName("Slate Gray",   new OutlineColor(112, 128, 144)));
    }};

    public static List<ColorName> getColors() {
        return colors;
    }

}
