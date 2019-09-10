package com.xray;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.xray.reference.Reference;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Paths;

public class Configuration
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static void load()
    {
        SPEC.setConfig(CommentedFileConfig
                .builder(Paths.get("config", Reference.MOD_ID + ".toml"))
                .build());
    }

    public static final General general = new General();
    public static class General
    {
        public final ForgeConfigSpec.IntValue radius;
        public final ForgeConfigSpec.BooleanValue showOverlay;
        public final ForgeConfigSpec.BooleanValue shouldFade;
        public final ForgeConfigSpec.DoubleValue outlineThickness;

        General()
        {
            BUILDER.push("general");
            radius = BUILDER
                    .comment("DO NOT TOUCH!",
                            "This setting is for memory only and if changed to a value not supported",
                            "the game will crash on start up. This value can be changed in game very simply.",
                            "If you must change it then these are the valid values 0 -> 7",
                            "but please leave it alone :P")
                    .defineInRange("radius", 3, 0, 9);

            showOverlay = BUILDER
                    .comment("This allows you hide or show the overlay in the top right of the screen when XRay is enabled")
                    .define("showOverlay", true);

            shouldFade = BUILDER
                    .comment("By default the blocks will begin to fade out as you get further away, some may not like this as",
                                "it can sometime causes things not to show. If you're a dirty cheater (lol) then you may want",
                                "to disable this as you won't be able to see chests out of your range.")
                    .define("shouldFade", true);

            outlineThickness = BUILDER
                    .comment("This allows you to set your own outline thickness, I find that 1.0 is perfect but others my",
                            "think differently. The max is 5.0")
                    .defineInRange("outlineThickness", 1.0, 1.0, 5.0);

            BUILDER.pop();
        }
    }

    private static final ForgeConfigSpec SPEC = BUILDER.build();
}
