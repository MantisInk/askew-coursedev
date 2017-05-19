package askew.entity.owl;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.FilterGroup;
import askew.entity.obstacle.BoxObstacle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import lombok.Getter;
import lombok.Setter;

/**
 * The owl which must be reached at the end of every level.
 *
 * This should be a good example of a basic dumb entity.
 */
public class OwlModel extends BoxObstacle  {

    public static final float OWL_HEIGHT = 1.0f *0.7f;
    public static final float OWL_WIDTH = OWL_HEIGHT * (385f / 283f);


    // determined at runtime to preserve aspect ratio from designers
    private transient float owlHeight;

    private transient TextureRegion owlTextureRegion;
    private transient Animation idleAnimation;
    private transient float elapseTime;
    @Getter
    private transient boolean doingVictory;
    private transient float victoryDistance;

    //JSON
    @Getter @Setter
    public float x;
    @Getter @Setter
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
        Filter f = new Filter();
        f.maskBits = FilterGroup.SLOTH | FilterGroup.HAND
        ;
        f.categoryBits = FilterGroup.WALL;
        this.setFilterData(f);
        this.setName("owl");
        this.drawNumber = Entity.DN_EBB;
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
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
        idleAnimation = new Animation(0.25f, manager.getTextureAtlas()
                .findRegions
                        ("ebbhang"), Animation.PlayMode.LOOP);
        owlTextureRegion = idleAnimation.getKeyFrame(0);
        setTexture(owlTextureRegion);
        // aspect ratio scaling
        this.owlHeight = getWidth() * ( owlTextureRegion.getRegionHeight() / owlTextureRegion.getRegionWidth());
    }

    @Override
    public void draw(GameCanvas canvas) {
        elapseTime += Gdx.graphics.getDeltaTime();

        if (elapseTime == 0) return;

        TextureRegion drawFrame;
        drawFrame = idleAnimation.getKeyFrame(elapseTime, true);


        setTexture(drawFrame);
        this.owlHeight = getWidth() * ( drawFrame.getRegionHeight() / drawFrame.getRegionWidth());

        canvas.draw(drawFrame,Color.WHITE,origin.x,origin.y,getPosition().x*drawScale.x,getPosition().y*drawScale.y,getAngle(),
                (1.0f/drawFrame.getRegionWidth()) *   getWidth() * getDrawScale().x * objectScale.x,
                (1.0f/drawFrame.getRegionHeight()  * getHeight()* getDrawScale().y * objectScale.y));
    }

    public float doVictory() {
        if (!doingVictory) {
            doingVictory = true;
        }
        this.victoryDistance ++;
        return (victoryDistance/120f);
    }

    public boolean didVictory() {
        return victoryDistance > 120;
    }
}