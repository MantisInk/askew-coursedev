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

    private transient Vector2 pupilOffset;
    private transient TextureRegion texture2;
    private final String pathEyes = "texture/eye/eyes.png";
    private final String pathPupils = "texture/eye/pupils.png";
    private transient TextureRegion texture2;

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
        Vector2 sPos = sloth.getMainBody().getPosition();
        float diffx = (sPos.x - this.x)/2;
        diffx = (diffx < -0.2) ? -0.2f : diffx;
        diffx = (diffx > 0.2) ? 0.2f : diffx;
        float diffy = (sPos.y - this.y)/2;
        diffy = (diffy < -0.15) ? -0.15f : diffy;
        diffy = (diffy > 0.17) ? 0.17f : diffy;
        pupilOffset.set(diffx,diffy);
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
