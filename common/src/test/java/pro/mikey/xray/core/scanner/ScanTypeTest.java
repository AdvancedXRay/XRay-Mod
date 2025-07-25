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
        var expectedColor = 0xFF5733;

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }

    @Test
    void correctlyParsesRGB() {
        var color = "rgb(255, 87, 51)";
        var expectedColor = 0xFF5733;

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }

    @Test
    void correctlyParsesHSL() {
        var color = "hsl(44, 50%, 50%)";
        var expectedColor = 0xBF9D40;

        int parsedColor = ScanType.parseColor(color);
        assertEquals(expectedColor, parsedColor);
    }
}
