package askew.entity;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.sloth.SlothModel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class EyeEntity extends BackgroundEntity {

    private final transient Vector2 pupilOffset;
    private transient TextureRegion texture2;
    private final float PUPIL_OFFSET = 0.08f;
    private final String pathEyes = "texture/eye/eyes.png";
    private final String pathPupils = "texture/eye/pupils.png";

    public EyeEntity(float x, float y) {
        this(x, y, 1.5f, 0, 1, 1, 0xFFFFFFFF);
    }

    public EyeEntity(float x, float y, float depth,
                     float angle, float scalex, float scaley, int color) {
        super(x, y, 1, 1, 1.5f, 0, scalex, scaley, "texture/eye/eyes.png", color);
        pupilOffset = new Vector2();
    }

    public void setTextures(MantisAssetManager manager) {
        super.setTextures(manager);
        Texture tex = manager.get(pathPupils);
        texture2 = new TextureRegion(tex);
        origin.set(texture2.getRegionWidth() / 2.0f, texture2.getRegionHeight() / 2.0f);
//        aspectRatio =(float)tex.getWidth()/(float)tex.getHeight();
    }

    public void update(float delta, SlothModel sloth) {
        //TODO draw moving eyes
//        SlothModel sloth = slothlist.get(0);
        Vector2 sPos = sloth.getMainBody().getPosition();
        if (sPos.x < this.x) {
            pupilOffset.x = -PUPIL_OFFSET;
        } else if (sPos.x == this.x) {
            pupilOffset.x = PUPIL_OFFSET;
        } else {
            pupilOffset.x = 0;
        }

        if (sPos.y < this.y) {
            pupilOffset.y = -PUPIL_OFFSET;
        } else if (sPos.y > this.y) {
            pupilOffset.y = PUPIL_OFFSET;
        } else {
            pupilOffset.y = 0;
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        draw(canvas, tint);
    }

    public void draw(GameCanvas canvas, Color tint) {
        if (texture != null && texture2 != null) {
            canvas.drawBackgroundEntity(texture, tint, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getDepth(), getAngle(),
                    (1.0f / texture.getRegionWidth()) * getWidth() * getDrawScale().x * objectScale.x * aspectRatio,
                    (1.0f / texture.getRegionHeight() * getHeight() * getDrawScale().y * objectScale.y), 1);
            canvas.drawBackgroundEntity(texture2, tint, origin.x, origin.y, (getX() + pupilOffset.x) * drawScale.x, (getY() + pupilOffset.y) * drawScale.y, getDepth(), getAngle(),
                    (1.0f / texture2.getRegionWidth()) * getWidth() * getDrawScale().x * objectScale.x * aspectRatio,
                    (1.0f / texture2.getRegionHeight() * getHeight() * getDrawScale().y * objectScale.y), 1);
        }
    }

}
