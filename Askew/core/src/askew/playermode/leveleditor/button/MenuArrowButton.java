package askew.playermode.leveleditor.button;

import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

@SuppressWarnings("SameParameterValue")
public class MenuArrowButton extends Button {

    private final boolean isLeft;
    private TextureRegion textureRegion;

    public MenuArrowButton(float x, float y, float width, float height, String group, int index, String name, boolean left) {
        super(x, y, width, height, group, index, name);
        isLeft = left;
    }

    public void setTextures(MantisAssetManager manager) {
        if (manager != null) {
            texture = manager.get("texture/leveleditor/emleft.png");
            textureRegion = new TextureRegion(texture);
            yellowbox = manager.get("texture/leveleditor/yellowbox.png");
            if (!isLeft && !textureRegion.isFlipX())
                textureRegion.flip(true, false);

        }
    }

    public void draw(GameCanvas canvas, float mousex, float mousey) {
        if (texture != null) {
            if (inBounds(mousex, mousey)) {
                canvas.draw(yellowbox, Color.WHITE, 0, 0, x - 3f, y - 3f, 0, (width + 6f) / yellowbox.getWidth(), (height + 6f) / yellowbox.getHeight());
            }

            canvas.draw(textureRegion, Color.WHITE, 0, 0, x, y, width, height);
        }
    }

}
