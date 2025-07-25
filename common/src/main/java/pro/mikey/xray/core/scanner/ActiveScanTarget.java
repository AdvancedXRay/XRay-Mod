package pro.mikey.xray.core.scanner;

/**
 * @deprecated I have no clue why I added this. ScanType contains the colour int...
 */
@Deprecated
public record ActiveScanTarget(
    ScanType type,
    int color
) {}
