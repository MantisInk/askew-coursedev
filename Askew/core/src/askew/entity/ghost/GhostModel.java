package askew.entity.ghost;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.obstacle.BoxObstacle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

/**
 * A ghost which patrols between two points and murders Flow.
 */
public class GhostModel extends BoxObstacle  {

    public static final float GHOST_SPEED = 1f;
    public static final float GHOST_WIDTH = 1.6f;

    // determined at runtime to preserve aspect ratio from designers
    private transient float ghostHeight;

    private transient TextureRegion ghostTextureRegion;
    private transient float thirtyTwoPixelDensityScale;

    private float x;
    private float y;

    private float patroldx;
    private float patroldy;

    private transient boolean secondDestination;

    private transient Animation walkAnimation;

    private transient float elapseTime;

    private transient boolean faceRight;
    private transient boolean lastFace;

    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x  Initial x position of the ragdoll head
     * @param y  Initial y position of the ragdoll head
     */
    public GhostModel(float x, float y, float patroldx, float patroldy) {
        super(x,y, GHOST_WIDTH, GHOST_WIDTH);
        this.x = x;
        this.y = y;
        this.patroldx = patroldx;
        this.patroldy = patroldy;
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(0);
        this.setRestitution(0);
        this.setSensor(true);
        this.setName("ghost");
        elapseTime = 1;
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
        walkAnimation = new Animation(0.127f, manager.getTextureAtlas().findRegions("Ghosty"), Animation.PlayMode.LOOP);
        if (walkAnimation.getKeyFrames().length == 0)
            System.err.println("did not find ghost");
        ghostTextureRegion = walkAnimation.getKeyFrame(0);
        this.ghostHeight = getWidth() * ((float) ghostTextureRegion.getRegionHeight() / (float) ghostTextureRegion.getRegionWidth());
        thirtyTwoPixelDensityScale = 32f / ghostTextureRegion.getRegionWidth();
        this.setHeight(ghostHeight);
        setTexture(ghostTextureRegion);
    }

    @Override
    public void draw(GameCanvas canvas) {
        // TODO: Help me figure out the draw scaling someone please
        elapseTime += Gdx.graphics.getDeltaTime();

        if (elapseTime == 0) return;

        TextureRegion drawFrame = walkAnimation.getKeyFrame(elapseTime, true);
        if (faceRight) {
            if (!drawFrame.isFlipX())
                drawFrame.flip(true,false);
            canvas.draw(drawFrame, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(), thirtyTwoPixelDensityScale * GHOST_WIDTH, thirtyTwoPixelDensityScale * ghostHeight);
        } else {
            if (drawFrame.isFlipX())
                drawFrame.flip(true,false);
            canvas.draw(drawFrame, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(), thirtyTwoPixelDensityScale * GHOST_WIDTH, thirtyTwoPixelDensityScale * ghostHeight);
        }

//        if (texture != null) {
//            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(), thirtyTwoPixelDensityScale * GHOST_WIDTH, thirtyTwoPixelDensityScale * ghostHeight);
//        }
    }

    @Override
    public void update(float dtime) {
        // Calculate vector to destination
        Vector2 myPos = getPosition();
        Vector2 dPos;
        if (secondDestination) {
            dPos = new Vector2(patroldx, patroldy);
        } else {
            dPos = new Vector2(x, y);
        }

        dPos = dPos.sub(myPos); // Direction now faces the destination
        float distanceToMove = dPos.len();
        float actualMoveDistance = GHOST_SPEED * dtime;


        if (distanceToMove < actualMoveDistance) {
            actualMoveDistance -= distanceToMove;
            secondDestination = !secondDestination;
            if (secondDestination) {
                dPos = new Vector2(patroldx, patroldy);
            } else {
                dPos = new Vector2(x, y);
            }
            dPos = dPos.sub(myPos); // Direction now faces the destination
        }

        // Calculate move proportions
        double moveX = Math.cos(dPos.angleRad());
        double moveY = Math.sin(dPos.angleRad());

        faceRight = moveX > 0;

        // Move toward destination
        this.setX(this.getX() + (float) moveX * actualMoveDistance);
        this.setY(this.getY() + (float) moveY * actualMoveDistance);
    }
}
