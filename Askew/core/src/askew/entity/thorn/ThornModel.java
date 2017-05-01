package askew.entity.thorn;

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
 * Stab stab stab.
 */
public class ThornModel extends BoxObstacle  {

    //JSON
    @Getter @Setter
    private float x;
    @Getter @Setter
    private float y;
    private float width;
    private float height;
    private float angle;

    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x  Initial x position of the ragdoll head
     * @param y  Initial y position of the ragdoll head
     */
    public ThornModel(float x, float y, float width, float height, float angle) {
        super(x,y - height, width, height);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        this.setName("thorn");
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

