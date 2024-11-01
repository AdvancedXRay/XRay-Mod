package pro.mikey.xray.utils;

/**
 * Most basic BlockData information able to be serialized easily.
 */
public class SerializableBlockData {

    private String name;
    private String blockName;
    private int order;

    private int color;
    private boolean drawing;

    public SerializableBlockData(String name, String blockName, int color, boolean drawing, int order) {
        this.name = name;
        this.blockName = blockName;
        this.color = color;
        this.drawing = drawing;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public String getBlockName() {
        return blockName;
    }

    public int getColor() {
        return color;
    }

    public boolean getDrawing() {
        return drawing;
    }

    public void setDrawing(boolean value) {this.drawing = value;}

    public int getOrder() {
        return order;
    }
}