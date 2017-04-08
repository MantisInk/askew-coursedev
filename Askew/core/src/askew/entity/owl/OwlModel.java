package askew.entity.owl;

import askew.GameCanvas;
import askew.entity.obstacle.BoxObstacle;
import com.badlogic.gdx.assets.AssetManager;
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
    public static final float OWL_WIDTH = 1.0f;
    public static final float OWL_HEIGHT = 1.0f;

    private transient TextureRegion owlTextureRegion;

    public float x;
    public float y;


    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x  Initial x position of the ragdoll head
     * @param y  Initial y position of the ragdoll head
     */
    public OwlModel(float x, float y) {
        super(x,y, OWL_WIDTH, OWL_HEIGHT);
        this.x = x;
        this.y = y;
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(0);
        this.setRestitution(0);
        this.setSensor(true);
        this.setName("owl");
    }

    private void init() {

    }

    public void setDrawScale(float x, float y) {
        super.setDrawScale(x,y);

        if (owlTextureRegion != null && body == null) {
            init();
        }
    }


    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        // TODO
    }


    @Override
    public void setTextures(AssetManager manager) {
        Texture owlTexture = manager.get(OWL_TEXTURE);
        owlTextureRegion = new TextureRegion(owlTexture);


        if (body == null) {
            init();
        } else {
            setTexture(owlTextureRegion);
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        // TODO
    }
}

