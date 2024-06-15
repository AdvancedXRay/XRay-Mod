package pro.mikey.xray;


import net.neoforged.neoforge.common.ModConfigSpec;

public class Configuration
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final General general = new General();
    public static final Store store = new Store();

    public static final ModConfigSpec.BooleanValue firstRun = BUILDER
            .comment("DO NOT TOUCH!", "This is not for you.", "This is used to check if it's the first time the mod has been run")
            .define("firstRun", true);

    public static class General {
        public final ModConfigSpec.BooleanValue showOverlay;
        public final ModConfigSpec.DoubleValue outlineThickness;

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
        public final ModConfigSpec.IntValue radius;
        public final ModConfigSpec.BooleanValue lavaActive;

        Store() {
            BUILDER.comment("DO NOT TOUCH!").push("store");

            radius = BUILDER
                    .comment("DO NOT TOUCH!", "This is not for you.")
                    .defineInRange("radius", 2, 0, 5);

            lavaActive = BUILDER
                    .comment("Memory value for if you're currently wanting Lava to be rendered into the mix")
                    .define("lavaActive", false);

            BUILDER.pop();
        }
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
