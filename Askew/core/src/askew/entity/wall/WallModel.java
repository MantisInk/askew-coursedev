package askew.entity.wall;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.PolygonObstacle;
import com.badlogic.gdx.graphics.Color;
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

    public static final float WALL_FRICTION = 1.0f;
    public static final float WALL_RESTITUTION = 0.1f;
    TextureRegion circleTextureRegion;
    TextureRegion edgeTextureRegion;

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
        wallTextureRegion = manager.getProcessedTextureMap().get(MantisAssetManager.WALL_TEXTURE);
        edgeTextureRegion = manager.getProcessedTextureMap().get(MantisAssetManager.EDGE_TEXTURE);
        circleTextureRegion = new TextureRegion(manager.get("texture/wall/corner.png",Texture.class));
        setTexture(wallTextureRegion);
    }

    @Override
    public void draw(GameCanvas canvas) {
        // Base draw
        if (region != null) {
            canvas.draw(region, Color.WHITE,0,0,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1,1);
        }

        // TODO: Draw edge texture
        for (int i = 0; i < points.length; i += 2) {
            float x1 = points[i];
            float y1 = points[i+1];
            float x2 = points[(i+2)%points.length];
            float y2 = points[(i+3)%points.length];
            canvas.draw(edgeTextureRegion,)
        }

        // Draw corners
        for (int i = 0; i < points.length; i+=2) {
            //TextureRegion region, Color tint, float ox, float oy,float x, float y, float angle, float sx, float sy)
            canvas.draw(circleTextureRegion,Color.WHITE,circleTextureRegion.getRegionWidth()/2f,circleTextureRegion.getRegionHeight()/2f,(getX()+points[i])*drawScale.x,(getY()+points[i+1])*drawScale.y,0,.25f,.25f);
        }
    }

    public void setPosition(float x, float y){
        super.setPosition(x,y);
        this.x = x;
        this.y = y;
    }

    public void pinchCreate(float bdx, float bdy) {
        // find the closest point in the existing points list and insert after.
        // the closest point minimizes pinch distance
        int index = -1;
        float minDst = Float.MAX_VALUE;
        for (int i = 0; i < points.length; i+=2){
            float curX = points[i];
            float curY = points[i+1];
            float nextX = points[(i+2)%points.length];
            float nextY = points[(i+3)%points.length];
            float dx = curX - bdx;
            float dy = curY - bdy;
            float dst = (float) Math.sqrt(dx*dx + dy*dy);
            float dx2 = nextX - bdx;
            float dy2 = nextY - bdy;
            float dst2 = (float) Math.sqrt(dx2*dx2 + dy2*dy2);
            if ((dst+dst2) < minDst) {
                index = i;
                minDst = dst+dst2;
            }
        }

        float[] newPoints = new float[points.length+2];
        for (int i = 0; i <= index; i+=2) {
            newPoints[i] = points[i];
            newPoints[i+1] = points[i+1];
        }

        newPoints[index+2] = bdx;
        newPoints[index+3] = bdy;

        for (int i = index+4; i < newPoints.length; i+=2) {
            newPoints[i] = points[i-2];
            newPoints[i+1] = points[i+1-2];
        }
        this.points = newPoints;
        this.initShapes(points);
        this.initBounds();
    }

    public void pinchDelete(float bdx, float bdy) {
        // find the closest point in the existing points list and insert after.
        // the closest point minimizes pinch distance
        int index = -1;
        float minDst = Float.MAX_VALUE;
        //DONTFIXME: start at index 2 because origin must be 0,0
        for (int i = 2; i < points.length; i+=2){
            float curX = points[i];
            float curY = points[i+1];
            float dx = curX - bdx;
            float dy = curY - bdy;
            float dst = (float) Math.sqrt(dx*dx + dy*dy);
            if ((dst) < minDst) {
                index = i;
                minDst = dst;
            }
        }


        float[] newPoints = new float[points.length-2];
        for (int i = 0; i < index; i+=2) {
            newPoints[i] = points[i];
            newPoints[i+1] = points[i+1];
        }

        // goodbye old pointo

        for (int i = index; i < newPoints.length; i+=2) {
            newPoints[i] = points[i+2];
            newPoints[i+1] = points[i+1+2];
        }
        this.points = newPoints;
        this.initShapes(points);
        this.initBounds();
    }

    public void pinchMove(float bdx, float bdy) {
        // find the closest point in the existing points list and insert after.
        // the closest point minimizes pinch distance
        int index = -1;
        float minDst = Float.MAX_VALUE;
        //DONTFIXME: start at index 2 because origin must be 0,0
        for (int i = 2; i < points.length; i+=2){
            float curX = points[i];
            float curY = points[i+1];
            float dx = curX - bdx;
            float dy = curY - bdy;
            float dst = (float) Math.sqrt(dx*dx + dy*dy);
            if ((dst) < minDst) {
                index = i;
                minDst = dst;
            }
        }

        points[index] = bdx;
        points[index+1] = bdy;

        this.initShapes(points);
        this.initBounds();
    }

    public float getModelX() {
        return this.x;
    }

    public float getModelY() {
        return this.y;
    }
}

