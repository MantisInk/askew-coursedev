package askew.entity.wall;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.PolygonObstacle;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;

/**
 * Wall to obstruct Flow's movement. This is a polygon, so you can get
 * creative with how the wall is shaped if you'd like.
 *
 * We're building a wall and making libgdx pay for it
 */
public class WallModel extends PolygonObstacle {

    public static final float WALL_FRICTION = 0.4f;
    public static final float WALL_RESTITUTION = 0.1f;

    // Instance variables
    private float x;
    private float y;

    /** Determines whether this is wall or thorn */
    private boolean thorn;

    /** The points that define the convex hull of the wall. Must be an even number (2n) of points representing (x1,y1) ... (xn,yn) */
    private float[] points;

    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x  Initial x position of the ragdoll head
     * @param y  Initial y position of the ragdoll head
     */
    public WallModel(float x, float y, float[] points, boolean thorn) {
        super(points, x, y);
        this.x = x;
        this.y = y;
        this.points = points;
        this.thorn = thorn;
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(WALL_FRICTION);
        this.setRestitution(WALL_RESTITUTION);
        if (thorn) {
            this.setSensor(true);
            this.setName("thorns");
            Filter f = new Filter();
            f.maskBits = FilterGroup.SLOTH;
            f.categoryBits = FilterGroup.WALL;
            this.setFilterData(f);
        } else {
            Filter f = new Filter();
            f.maskBits = FilterGroup.SLOTH | FilterGroup.VINE;
            f.categoryBits = FilterGroup.WALL;
            this.setFilterData(f);
        }
    }

    public void setDrawScale(float x, float y) {
        super.setDrawScale(x,y);
    }

    @Override
    public void setTextures(MantisAssetManager manager) {
        TextureRegion wallTextureRegion;
        if (thorn) {
            wallTextureRegion = manager.getProcessedTextureMap().get(MantisAssetManager.THORN_TEXTURE);
        } else {
            wallTextureRegion = manager.getProcessedTextureMap().get(MantisAssetManager.WALL_TEXTURE);
        }
        setTexture(wallTextureRegion);
    }

}

