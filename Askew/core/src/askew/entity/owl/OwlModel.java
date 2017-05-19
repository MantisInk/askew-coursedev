package askew.entity.owl;

import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.obstacle.SimpleObstacle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import lombok.Getter;
import lombok.Setter;

/**
 * The owl which must be reached at the end of every level.
 * <p>
 * This should be a good example of a basic dumb entity.
 */
@SuppressWarnings("FieldCanBeLocal")
public class OwlModel extends ComplexObstacle {

    private static final float OWL_DRAW_WIDTH = 2.2f;
    private static final float OWL_HEIGHT = 1.4f *0.6f;
    private static final float OWL_WIDTH = 1.8f * (489f / 835f) * 0.6f;
    private static final float VICTORY_SPEED = 0.15f;
    private static final float VICTORY_DISTANCE = 13f;
    private static final String OWL_TEXTURE = "texture/owl/ebb.png";

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
    private transient float elapseTime;
    @Getter
    private transient boolean doingVictory;
    private transient float victoryDistance;
    private BoxObstacle pin;
    private BoxObstacle owlBody;


    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x Initial x position of the ragdoll head
     * @param y Initial y position of the ragdoll head
     */
    public OwlModel(float x, float y) {
        super(x, y);
        this.x = x;
        this.y = y;
        build();
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
    }

    @Override
    protected boolean createJoints(World world) {
        // Pin arm to the vine
        Vector2 pinAnchorPosition = new Vector2(0,0);
        Vector2 armAnchorPosition = new Vector2(-OWL_WIDTH*0.46f,
                OWL_HEIGHT*0.45f);

        RevoluteJointDef  grabJointDef = new RevoluteJointDef();
        grabJointDef.bodyA = pin.getBody();
        grabJointDef.bodyB = owlBody.getBody();

        grabJointDef.localAnchorA.set(pinAnchorPosition);
        grabJointDef.localAnchorB.set(armAnchorPosition);
        grabJointDef.collideConnected = false;
        joints.add(world.createJoint(grabJointDef));
        return true;
    }

    private void setupOwl() {
        this.setSensor(true);
        this.setName("owl");
        Vector2 pos = new Vector2(x,y);

        // Branch pin
        pin = new BoxObstacle(pos.x, pos.y,0.1f,0.1f);
        pin.setDensity(0);
        pin.setBodyType(BodyDef.BodyType.StaticBody);
        bodies.add(pin);

        // Owl Hull
        owlBody = new BoxObstacle(pos.x, pos.y, OWL_WIDTH,OWL_HEIGHT);
        owlBody.setName("owl");
        owlBody.setDensity(1);
        Filter f = new Filter();
        f.maskBits = FilterGroup.SLOTH;
        f.categoryBits = FilterGroup.WALL | FilterGroup.WIN;
        owlBody.setFilterData(f);
        owlBody.setCustomScale(1.4f,1.4f);
        bodies.add(owlBody);
    }


    @Override
    public void setTextures(MantisAssetManager manager) {
        Texture owlTexture = manager.get(OWL_TEXTURE);
        owlTextureRegion = new TextureRegion(owlTexture);
        for (Obstacle body : bodies) {
            ((SimpleObstacle) body).setTexture(owlTextureRegion);
        }
    }

    @Override
    public void update(float delta) {
        elapseTime += delta;
    }

    @Override
    public void build() {
        setupOwl();
    }

    @Override
    public void rebuild() {
        bodies.clear();
        build();
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

