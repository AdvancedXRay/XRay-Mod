package pro.mikey.xray.core.scanner;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import pro.mikey.xray.XRay;

import java.awt.*;
import java.util.Set;

public abstract class ScanType {
    public Type type;
    public String name;
    public String color;
    public int order;
    public boolean enabled;

    // Computed at loadtime.
    public int colorInt;

    public ScanType(Type type, String name, String color, int order, boolean enabled) {
        this.type = type;
        this.name = name;
        this.color = color;
        this.order = order;
        this.enabled = enabled;
        this.colorInt = parseColor(color);
    }

    public ScanType(Type type, JsonObject obj) {
        this.type = type;
        this.name = obj.get("name").getAsString();
        this.color = obj.get("color").getAsString();
        this.order = obj.get("order").getAsInt();
        this.enabled = obj.get("enabled").getAsBoolean();

        this.colorInt = parseColor(this.color);
    }

    public Type type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String color() {
        return color;
    }

    public int order() {
        return order;
    }

    public boolean enabled() {
        return enabled;
    }

    public int colorInt() {
        return colorInt;
    }

    public abstract boolean matches(Level level, BlockPos pos, BlockState state, FluidState fluidState);

    abstract void writeData(JsonObject obj);

    public JsonObject save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", this.type.getId().toString());
        obj.addProperty("name", this.name);
        obj.addProperty("color", this.color);
        obj.addProperty("order", this.order);
        obj.addProperty("enabled", this.enabled);

        // Write additional data for the specific scan type
        writeData(obj);

        return obj;
    }

    public void updateColor(String newColor) {
        this.color = newColor;
        this.colorInt = parseColor(newColor);
    }

    public void updateColor(int newColorInt) {
        this.colorInt = newColorInt;

        // Extract ARGB components
        int a = (newColorInt >> 24) & 0xFF;
        int r = (newColorInt >> 16) & 0xFF;
        int g = (newColorInt >> 8) & 0xFF;
        int b = newColorInt & 0xFF;

        // What type are we?
        if (this.color.startsWith("rgba(")) {
            this.color = String.format("rgba(%d, %d, %d, %d)", r, g, b, a);
        } else if (this.color.startsWith("rgb(")) {
            // If alpha is not 255, convert to rgba format
            if (a != 255) {
                this.color = String.format("rgba(%d, %d, %d, %d)", r, g, b, a);
            } else {
                this.color = String.format("rgb(%d, %d, %d)", r, g, b);
            }
        } else if (this.color.startsWith("#")) {
            // Convert to hex format (with alpha if not fully opaque)
            if (a != 255) {
                this.color = String.format("#%02X%02X%02X%02X", a, r, g, b);
            } else {
                this.color = String.format("#%02X%02X%02X", r, g, b);
            }
        } else if (this.color.startsWith("hsl(")) {
            // Convert to HSL format
            var hsl = Color.RGBtoHSB(r, g, b, null);
            // We're half way there, so let's convert it to HSL
            this.color = String.format("hsl(%d, %.2f%%, %.2f%%)", (int) (hsl[0] * 360), hsl[1] * 100, hsl[2] * 100);
        } else if (this.color.startsWith("0x")) {
            this.color = String.format("#%06X", newColorInt & 0xFFFFFF);
        } else {
            // Default to rgba format for unknown formats
            this.color = String.format("rgba(%d, %d, %d, %d)", r, g, b, a);
        }
    }

    /**
     * Custom method for reading hex, rgb, rgba, hsl, and other color formats
     * @param color String representation of the color
     *              rgb(255, 0, 0), rgba(255, 0, 0, 128), hsl(120, 100%, 50%), #FF0000, #FF0000FF, 0xFF0000
     *              are the supported formats at the moment
     * @return int representation of the color (ARGB format)
     */
    public static int parseColor(String color) {
        if (color.startsWith("#")) {
            // Support both RRGGBB and AARRGGBB formats
            if (color.length() == 7) {
                // RRGGBB format - add full alpha
                return 0xFF000000 | Integer.parseInt(color.substring(1), 16);
            } else if (color.length() == 9) {
                // AARRGGBB format
                return (int)Long.parseLong(color.substring(1), 16);
            } else {
                throw new IllegalArgumentException("Invalid hex color format: " + color);
            }
        }

        if (color.startsWith("rgba(") && color.endsWith(")")) {
            // rgba(255, 0, 0, 128)
            String[] parts = color.substring(5, color.length() - 1).split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid RGBA color format: " + color);
            }
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            int a = Integer.parseInt(parts[3].trim());
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        if (color.startsWith("rgb(") && color.endsWith(")")) {
            // rgb(255, 0, 0) - default to full alpha
            String[] parts = color.substring(4, color.length() - 1).split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid RGB color format: " + color);
            }
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        if (color.startsWith("hsl(") && color.endsWith(")")) {
            var parts = color.replace("hsl(", "").replace(")", "").split(",");

            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid HSL color format: " + color);
            }

            // Parse out the HSL values, intrinsically supports values with or without percentage
            float h = Float.parseFloat(parts[0].trim());
            float s = Float.parseFloat(parts[1].replace("%", "").trim()) / 100f;
            float l = Float.parseFloat(parts[2].replace("%", "").trim()) / 100f;

            // Convert HSL to RGB
            float c = (1 - Math.abs(2 * l - 1)) * s;
            float hPrime = h / 60f;
            float x = c * (1 - Math.abs(hPrime % 2 - 1));
            float m = l - c / 2; // m is the adjustment to match the lightness

            // Switch to determine RGB values based on HSL
            float r = 0, g = 0, b = 0;
            switch ((int) hPrime) {
                case 0 -> { r = c; g = x; }
                case 1 -> { r = x; g = c; }
                case 2 -> { g = c; b = x; }
                case 3 -> { g = x; b = c; }
                case 4 -> { r = x; b = c; }
                case 5, 6 -> { r = c; b = x; } // case 6 handles h=360
            }

            // Adjust RGB values by adding m
            int ri = Math.round((r + m) * 255);
            int gi = Math.round((g + m) * 255);
            int bi = Math.round((b + m) * 255);

            return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
        }

        if (color.startsWith("0x")) {
            // 0xFF0000 format - add full alpha
            return 0xFF000000 | Integer.parseInt(color.substring(2), 16);
        }

        throw new IllegalArgumentException("Unsupported color format: " + color);
    }

    public static String randomRgbColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        return String.format("rgb(%d, %d, %d)", r, g, b);
    }

    public enum Type {
        BLOCK(XRay.id("block"));

        private static final Set<Type> values = Set.of(values());

        private final Identifier id;

        Type(Identifier id) {
            this.id = id;
        }

        public static Type fromId(Identifier id) {
            for (Type type : values) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown ScanType ID: " + id);
        }

        public Identifier getId() {
            return id;
        }
    }
}
