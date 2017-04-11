package askew.entity.owl;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.obstacle.BoxObstacle;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;

/**
 * The owl which must be reached at the end of every level.
 *
 * This should be a good example of a basic dumb entity.
 */
public class OwlModel extends BoxObstacle  {

    public static final String OWL_TEXTURE = "texture/owl/owl.png";
    public static final float OWL_WIDTH = 2.2f;
    // determined at runtime to preserve aspect ratio from designers
    private transient float owlHeight;

    private transient TextureRegion owlTextureRegion;
    private transient float thirtyTwoPixelDensityScale;

    public float x;
    public float y;


    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x  Initial x position of the ragdoll head
     * @param y  Initial y position of the ragdoll head
     */
    public OwlModel(float x, float y) {
        super(x,y, OWL_WIDTH, OWL_WIDTH);
        this.x = x;
        this.y = y;
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(0);
        this.setRestitution(0);
        this.setSensor(true);
        this.setName("owl");
    }

    public void setDrawScale(float x, float y) {
        super.setDrawScale(x,y);
    }


    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        // TODO
    }


    @Override
    public void setTextures(MantisAssetManager manager) {
        Texture owlTexture = manager.get(OWL_TEXTURE);
        owlTextureRegion = new TextureRegion(owlTexture);
        // aspect ratio scaling
        this.owlHeight = getWidth() * ( owlTextureRegion.getRegionHeight() / owlTextureRegion.getRegionWidth());
        thirtyTwoPixelDensityScale = 32f /owlTextureRegion.getRegionWidth();
        setTexture(owlTextureRegion);
    }

    @Override
    public void draw(GameCanvas canvas) {
        // TODO: Help me figure out the draw scaling someone please

        if (texture != null) {
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(), thirtyTwoPixelDensityScale * OWL_WIDTH, thirtyTwoPixelDensityScale * owlHeight);
        }
    }
}

