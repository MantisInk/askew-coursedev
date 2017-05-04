package askew.entity.thorn;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.PolygonObstacle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import lombok.Getter;
import lombok.Setter;

/**
 * Stab stab stab.
 */
public class ThornModel extends PolygonObstacle {

    //JSON
    @Getter @Setter
    private float x;
    @Getter @Setter
    private float y;
    private float width;
    private float angle;

    public static final float EPSILON = 0.2f;

    private static float[] xywhaToPoints(float width, float angle) {
        float[] points = new float[8];
        angle = (float) Math.toRadians(angle);
        // origin is x,y, rotate everything else. Height is always 1.
        points[0] = 0;
        points[1] = 0;
        points[2] = 0;
        points[3] = 1;
        points[4] = width;
        points[5] = 1;
        points[6] = width;
        points[7] = 0;
//        float lx = (float) -(Math.sin(angle));
//        float ly = (float) (Math.cos(angle));
//        points[2] = lx;
//        points[3] = ly;
//        points[4] = lx + width * (float)(Math.cos(angle));
//        points[5] = ly + width * (float)(Math.sin(angle));
//        points[6] = width * (float)(Math.cos(angle));
//        points[7] = width * (float)(Math.sin(angle));
//        if (Math.abs(points[6] / width) < EPSILON) {
//            points[4] = lx;
//            points[6] = 0;
//        }

        return points;
    }

    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x  Initial x position of the ragdoll head
     * @param y  Initial y position of the ragdoll head
     */
    public ThornModel(float x, float y, float width, float angle) {
        super(xywhaToPoints(width,angle),x,y);
        this.x = x;
        this.y = y;
        this.width = width;
        this.angle = angle;
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(0);
        this.setRestitution(0);
        this.setSensor(true);
        Filter f = new Filter();
        f.maskBits = FilterGroup.SLOTH;
        f.categoryBits = FilterGroup.WALL;
        this.setFilterData(f);
        this.setName("thorns");
        this.setAngle((float)Math.toRadians(angle));
    }

    public void setPosition(float x, float y){
        super.setPosition(x,y);
        this.x = x;
        this.y = y;
    }


    @Override
    public void setTextures(MantisAssetManager manager) {
        TextureRegion wallTextureRegion;
        wallTextureRegion = manager.getProcessedTextureMap().get(MantisAssetManager.THORN_TEXTURE);
        setTexture(wallTextureRegion);
    }

}

