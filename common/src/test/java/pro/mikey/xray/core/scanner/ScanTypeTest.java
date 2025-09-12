package pro.mikey.xray.core.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScanTypeTest {
    @Test
    void colorParserFailsOnInvalidHex() {
        var color = "#invalidColor";
        assertThrows(IllegalArgumentException.class, () -> ScanType.parseColor(color));
    }

    @Test
    void colorParserFailsOnInvalidInput() {
        var color = "invalidColor";
        assertThrows(IllegalArgumentException.class, () -> ScanType.parseColor(color));
    }

    @Test
    void correctlyFailsOnInvalidRGB() {
        var color = "rgb(255, 87, 51, 0.5)"; // Invalid RGB format
        assertThrows(IllegalArgumentException.class, () -> ScanType.parseColor(color));
    }

    @Test
    void colorParserFailsOnInvalidHSL() {
        var color = "hsl(360, 100%, 50%, 0.5)"; // Invalid HSL format
        assertThrows(IllegalArgumentException.class, () -> ScanType.parseColor(color));
    }

    @Test
    void correctlyParsesValidHex() {
        var color = "#FF5733";
        var expectedColor = 0xFFFF5733; // Now includes alpha channel (FF)

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }

    @Test
    void correctlyParsesRGB() {
        var color = "rgb(255, 87, 51)";
        var expectedColor = 0xFFFF5733; // Now includes alpha channel (FF)

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }

    @Test
    void correctlyParsesHSL() {
        var color = "hsl(44, 50%, 50%)";
        var expectedColor = 0xFFBF9D40; // Now includes alpha channel (FF)

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }
    
    @Test
    void correctlyParsesRGBA() {
        var color = "rgba(255, 87, 51, 128)";
        var expectedColor = 0x80FF5733; // Alpha is 128 (0x80)

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }
    
    @Test
    void correctlyParsesHexWithAlpha() {
        var color = "#80FF5733";
        var expectedColor = 0x80FF5733;

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }
}
