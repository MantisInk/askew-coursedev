package askew.entity.wall;

import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.PolygonObstacle;
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

    public static final float WALL_FRICTION = 1.0f;
    public static final float WALL_RESTITUTION = 0.1f;

    // Instance variables
    private float x;
    private float y;

    /** The points that define the convex hull of the wall. Must be an even number (2n) of points representing (x1,y1) ... (xn,yn) */
    private float[] points;

    public WallModel(float x, float y, float[] points) {
        super(points, x, y);
        this.x = x;
        this.y = y;
        this.points = points;
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(WALL_FRICTION);
        this.setRestitution(WALL_RESTITUTION);
        Filter f = new Filter();
        f.maskBits = FilterGroup.SLOTH | FilterGroup.VINE;
        f.categoryBits = FilterGroup.WALL;
        this.setFilterData(f);
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

    public void setPosition(float x, float y){
        super.setPosition(x,y);
        this.x = x;
        this.y = y;
    }

}

