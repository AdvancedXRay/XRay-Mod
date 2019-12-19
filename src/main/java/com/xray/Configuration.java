package com.xray;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configuration
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final General general = new General();
    public static final Store store = new Store();

    public static class General {
        public final ForgeConfigSpec.BooleanValue showOverlay;
        public final ForgeConfigSpec.DoubleValue outlineThickness;

        General() {
            BUILDER.push("general");

            showOverlay = BUILDER
                    .comment("This allows you hide or show the overlay in the top right of the screen when XRay is enabled")
                    .define("showOverlay", true);

            outlineThickness = BUILDER
                    .comment("This allows you to set your own outline thickness, I find that 1.0 is perfect but others my",
                            "think differently. The max is 5.0")
                    .defineInRange("outlineThickness", 1.0, 1.0, 5.0);

            BUILDER.pop();
        }
    }

    public static class Store {
        public final ForgeConfigSpec.IntValue radius;
        public final ForgeConfigSpec.BooleanValue lavaActive;

        Store() {
            BUILDER.comment("DO NOT TOUCH!").push("store");

            radius = BUILDER
                    .comment("DO NOT TOUCH!",
                            "This setting is for memory only and if changed to a value not supported",
                            "the game will crash on start up. This value can be changed in game very simply.",
                            "If you must change it then these are the valid values 0 -> 7",
                            "but please leave it alone :P")
                    .defineInRange("radius", 3, 0, 9);

            lavaActive = BUILDER
                    .comment("Memory value for if you're currently wanting Lava to be rendered into the mix")
                    .define("lavaActive", false);

            BUILDER.pop();
        }
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
