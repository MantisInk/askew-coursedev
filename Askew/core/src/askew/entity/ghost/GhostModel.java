package askew.entity.ghost;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.BoxObstacle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import lombok.Getter;
import lombok.Setter;

/**
 * A ghost which patrols between two points and murders Flow.
 */
public class GhostModel extends BoxObstacle {

    private static final float GHOST_SPEED = 1f;
    private static final float GHOST_WIDTH = 0.8f;
    private static final float GHOST_HEIGHT = GHOST_WIDTH * (546f / 289f);

    //JSON
    @Getter
    @Setter
    private float x;
    @Getter
    @Setter
    private float y;

    /**
     * Patrol points.
     */
    @Getter
    @Setter
    private float patroldx1;
    @Getter
    @Setter
    private float patroldy1;
    @Getter
    @Setter
    private float patroldx2;
    @Getter
    @Setter
    private float patroldy2;

    private transient boolean secondDestination;

    private transient Animation walkAnimation;

    private transient float elapseTime;

    private transient boolean faceRight;

    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x Initial x position of the ragdoll head
     * @param y Initial y position of the ragdoll head
     */
    public GhostModel(float x, float y, float patroldx1, float patroldy1, float patroldx2, float patroldy2) {
        super(x, y, GHOST_WIDTH, GHOST_HEIGHT);
        this.x = x;
        this.y = y;
        this.patroldx1 = patroldx1;
        this.patroldy1 = patroldy1;
        this.patroldx2 = patroldx2;
        this.patroldy2 = patroldy2;
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(0);
        this.setRestitution(0);
        this.setSensor(true);
        Filter f = new Filter();
        f.maskBits = FilterGroup.SLOTH | FilterGroup.ARM;
        f.categoryBits = FilterGroup.WALL | FilterGroup.THORN;
        this.setFilterData(f);
        this.setName("ghost");
        elapseTime = 1;
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
    }


    @Override
    public void setTextures(MantisAssetManager manager) {
        walkAnimation = new Animation(0.127f, manager.getTextureAtlas().findRegions("Ghosty"), Animation.PlayMode.LOOP);
        if (walkAnimation.getKeyFrames().length == 0)
            System.err.println("did not find ghost");
        TextureRegion ghostTextureRegion = walkAnimation.getKeyFrame(0);
        setTexture(ghostTextureRegion);
    }

    @Override
    public void draw(GameCanvas canvas) {
        elapseTime += Gdx.graphics.getDeltaTime();

        if (elapseTime == 0) return;

        TextureRegion drawFrame = walkAnimation.getKeyFrame(elapseTime, true);
        if (faceRight) {
            if (!drawFrame.isFlipX())
                drawFrame.flip(true, false);
        } else {
            if (drawFrame.isFlipX())
                drawFrame.flip(true, false);
        }
        canvas.draw(drawFrame, Color.WHITE, origin.x, origin.y, getPosition().x * drawScale.x, getPosition().y * drawScale.y, getAngle(),
                (1.0f / texture.getRegionWidth()) * getWidth() * getDrawScale().x * objectScale.x,
                (1.0f / texture.getRegionHeight() * getHeight() * getDrawScale().y * objectScale.y));

    }

    @Override
    public void update(float dtime) {
        // Calculate vector to destination
        Vector2 myPos = getPosition();
        Vector2 dPos;
        if (secondDestination) {
            dPos = new Vector2(patroldx1, patroldy1);
        } else {
            dPos = new Vector2(patroldx2, patroldy2);
        }

        dPos = dPos.sub(myPos); // Direction now faces the destination
        float distanceToMove = dPos.len();
        float actualMoveDistance = GHOST_SPEED * dtime;


        if (distanceToMove < actualMoveDistance) {
            actualMoveDistance -= distanceToMove;
            secondDestination = !secondDestination;
            if (secondDestination) {
                dPos = new Vector2(patroldx1, patroldy1);
            } else {
                dPos = new Vector2(patroldx2, patroldy2);
            }
            dPos = dPos.sub(myPos); // Direction now faces the destination
        }

        // Calculate move proportions
        double moveX = Math.cos(dPos.angleRad());
        double moveY = Math.sin(dPos.angleRad());

        faceRight = moveX > 0;

        // Move toward destination
        this.setPosition(this.getPosition().x + (float) moveX * actualMoveDistance, this.getPosition().y + (float) moveY * actualMoveDistance);
    }

}

