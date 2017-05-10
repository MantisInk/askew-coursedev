package askew.entity.owl;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.BoxObstacle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import lombok.Getter;
import lombok.Setter;

/**
 * The owl which must be reached at the end of every level.
 * <p>
 * This should be a good example of a basic dumb entity.
 */
@SuppressWarnings("FieldCanBeLocal")
public class OwlModel extends BoxObstacle {

    private static final float OWL_DRAW_WIDTH = 2.2f;
    private static final float OWL_WIDTH = 1.8f;
    private static final float VICTORY_SPEED = 0.15f;
    private static final float VICTORY_DISTANCE = 13f;
    //JSON
    @Getter
    @Setter
    public float x;
    @Getter
    @Setter
    public float y;
    // determined at runtime to preserve aspect ratio from designers
    private transient float owlHeight;
    private transient TextureRegion owlTextureRegion;
    private transient Animation idleAnimation;
    private transient Animation flyAnimation;
    private transient float elapseTime;
    @Getter
    private transient boolean doingVictory;
    private transient float victoryDistance;


    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x Initial x position of the ragdoll head
     * @param y Initial y position of the ragdoll head
     */
    public OwlModel(float x, float y) {
        //noinspection SuspiciousNameCombination
        super(x, y, OWL_WIDTH, OWL_WIDTH);
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
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
    }


    @Override
    public void setTextures(MantisAssetManager manager) {
        idleAnimation = new Animation(0.127f, manager.getTextureAtlas()
                .findRegions
                        ("idleowl"), Animation.PlayMode.LOOP);
        flyAnimation = new Animation(0.127f, manager.getTextureAtlas().findRegions
                ("owlfly"), Animation.PlayMode.LOOP);
        if (idleAnimation.getKeyFrames().length == 0)
            System.err.println("did not find anim");
        owlTextureRegion = idleAnimation.getKeyFrame(0);
        setTexture(owlTextureRegion);
        // aspect ratio scaling
        this.owlHeight = OWL_DRAW_WIDTH * (owlTextureRegion.getRegionHeight() / owlTextureRegion.getRegionWidth());
        if (flyAnimation.getKeyFrames().length == 0)
            System.err.println("did not find anim");
    }

    @Override
    public void draw(GameCanvas canvas) {
        elapseTime += Gdx.graphics.getDeltaTime();

        if (elapseTime == 0) return;

        TextureRegion drawFrame;
        if (doingVictory) {
            drawFrame = flyAnimation.getKeyFrame(elapseTime, true);
            if (!drawFrame.isFlipX())
                drawFrame.flip(true, false);
        } else {
            drawFrame = idleAnimation.getKeyFrame(elapseTime, true);
        }
        drawFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        setTexture(drawFrame);
        this.owlHeight = OWL_DRAW_WIDTH * (drawFrame.getRegionHeight() / drawFrame.getRegionWidth());

        canvas.draw(drawFrame, Color.WHITE, origin.x, origin.y, getPosition().x * drawScale.x, getPosition().y * drawScale.y, getAngle(),
                (1.0f / drawFrame.getRegionWidth()) * OWL_DRAW_WIDTH * getDrawScale().x * objectScale.x,
                (1.0f / drawFrame.getRegionHeight() * owlHeight * getDrawScale().y * objectScale.y));
    }

    public float doVictory() {
        if (!doingVictory) {
            doingVictory = true;
        }

        this.setPosition(this.getPosition().x + VICTORY_SPEED, this.getPosition().y);
        this.victoryDistance += VICTORY_SPEED;
        return this.victoryDistance / VICTORY_DISTANCE;
    }

    public boolean didVictory() {
        return victoryDistance > VICTORY_DISTANCE;
    }
}

