package askew.entity.thorn;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.BoxObstacle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import lombok.Getter;
import lombok.Setter;

/**
 * Stab stab stab.
 */
public class ThornModel extends BoxObstacle {

    private static final float HEIGHT = 0.5f;
    private final float width;
    private final float angle;
    //JSON
    @Getter
    @Setter
    private transient float x;
    @Getter
    @Setter
    private transient float y;
    private TextureRegion wrapper;
    private float realX;
    private float realY;

    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x Initial x position of the ragdoll head
     * @param y Initial y position of the ragdoll head
     */
    public ThornModel(float x, float y, float width, float angle) {
        super(x + width / 2, y + HEIGHT / 2, width, HEIGHT);
        this.x = x;
        this.y = y;
        this.realX = x;
        this.realY = y;
        this.width = width;
        this.angle = angle;
        setPosition(x, y);
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(0);
        this.setRestitution(0);
        this.setSensor(true);
        Filter f = new Filter();
        f.maskBits = FilterGroup.SLOTH | FilterGroup.HAND | FilterGroup.ARM;
        f.categoryBits = FilterGroup.WALL | FilterGroup.THORN;
        this.setFilterData(f);
        this.setName("thorns");
        this.setAngle((float) Math.toRadians(angle));
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.realX = x;
        this.realY = y;
        this.x = x + width / 2 * (float) Math.cos(Math.toRadians(angle)) + HEIGHT / 2 * (float) Math.sin(Math.toRadians(angle));
        this.y = y + width / 2 * (float) Math.sin(Math.toRadians(angle)) + HEIGHT / 2 * (float) Math.cos(Math.toRadians(angle));
        if (wrapper != null) {
            wrapper.setRegion(0, 0, (int) Math.floor(width * wrapper.getRegionWidth()), (int) Math.floor(wrapper.getRegionHeight()));
        }
    }


    @Override
    public void setTextures(MantisAssetManager manager) {
        wrapper = manager.getProcessedTextureMap().get(MantisAssetManager.THORN_TEXTURE);
        wrapper = new TextureRegion(wrapper);
        wrapper.setRegion(0, 0, (int) Math.floor(width * wrapper.getRegionWidth()), (int) Math.floor(wrapper.getRegionHeight()));
        setTexture(wrapper);
    }

    @Override
    public void draw(GameCanvas canvas, Color tint) {
        if (wrapper != null) {
            wrapper.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            canvas.draw(wrapper, tint, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(),
                    (1.0f / wrapper.getRegionWidth()) * getWidth() * getDrawScale().x * getObjectScale().x * customScale.x,
                    (1.0f / wrapper.getRegionHeight() * getHeight() * getDrawScale().y * getObjectScale().y * customScale.y));
        }
    }

}

