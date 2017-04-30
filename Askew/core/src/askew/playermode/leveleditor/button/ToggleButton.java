package askew.playermode.leveleditor.button;


public class ToggleButton extends Button{

    public static final float DEFAULT_WIDTH = 64;
    public static final float DEFAULT_HEIGHT = 16;

    public ToggleButton(float x, float y, String group, int index, String name) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, group, index, name);
    }
}
