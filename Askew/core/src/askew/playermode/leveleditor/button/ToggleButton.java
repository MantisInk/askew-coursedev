package askew.playermode.leveleditor.button;


import askew.GameCanvas;
import com.badlogic.gdx.graphics.Color;
import lombok.Getter;
import lombok.Setter;

import static askew.playermode.leveleditor.LevelEditorController.GUI_LEFT_BAR_MARGIN;
import static askew.playermode.leveleditor.LevelEditorController.GUI_LEFT_BAR_WIDTH;

public class ToggleButton extends Button {

    private static final float DEFAULT_WIDTH = GUI_LEFT_BAR_WIDTH - (2 * GUI_LEFT_BAR_MARGIN);
    private static final float DEFAULT_HEIGHT = 16;

    @Getter
    @Setter
    public boolean on;

    private ToggleButton(float x, float y, float width, float height, int index, String name) {
        super(x, y, width, height, "LEOptions", index, name);
        this.on = false;
    }

    public ToggleButton(float x, float y, int index, String name) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, index, name);
    }

    @Override
    public void draw(GameCanvas canvas, float mousex, float mousey) {
        if (texture != null) {
            if (inBounds(mousex, mousey)) {
                canvas.draw(yellowbox, Color.WHITE, 0, 0, x - 3f, y - 3f, 0, (width + 6f) / yellowbox.getWidth(), (height + 6f) / yellowbox.getHeight());
            }
            Color tint = Color.FOREST;
            if (!on) {
                tint = Color.FIREBRICK;
            }
            canvas.draw(texture, tint, 0, 0, x, y, width, height);
            canvas.drawTextStandard(name, x + 10, y + height);
        }
    }
}
