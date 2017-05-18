/*
 * RopeBridge.java
 *
 * The class is a classic example of how to subclass ComplexPhysicsObject.
 * You have to implement the createJoints() method to stick in all of the
 * joints between objects.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package askew.entity.tree;

import askew.entity.FilterGroup;
import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.WheelObstacle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

/**
 * A tree trunk with planks connected by weld joints.
 * <p>
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
@SuppressWarnings("FieldCanBeLocal")
public class PoleVault extends TreeParent {

    /**
     * Set damping constant for joint rotation in vines
     */
    public static final float DAMPING_ROTATION = 5f;
    /**
     * The debug name for each anchor pin
     */
    private static final String TRUNK_PIN_NAME = "pin";
    /**
     * The radius of each anchor pin
     */
    private static final float TRUNK_PIN_RADIUS = 0.1f;
    /**
     * The spacing between each link
     */
    private final float numLinks;                                    // param for json constructor
    private final float angle;
    /**
     * pin the bottom of the pole vault
     */
    private transient WheelObstacle start = null;
    /**
     * starting coords of bottom anchor and length for branch
     */
    private transient Vector2 dimension;
    /**
     * The size of the entire bridge
     */
    private float linksize;
    /**
     * The length of each link
     */
    private transient Vector2 final_norm = null;
    /**
     * coords for starting branch off this trunk
     */
    private transient Vector2 planksize;
    /**
     * The size of a single plank
     */
    // TODO: Fix this from being public (refactor artifact) ?
    private transient float spacing = 0.0f;
    private float x;
    private float y;

    /**
     * Creates a new tree trunk at the given position.
     * <p>
     * This trunk is default vertical, but you can set an angle in degrees.
     * The top planks designated by stiffLen will not be created as part of the trunk.
     *
     * @param x      The x position of the left anchor
     * @param y      The y position of the left anchor
     * @param length The length of the trunk
     */
    public PoleVault(float x, float y, float length, Vector2 scale, float angle) {
        super(x, y);
        setName(TRUNK_NAME);
        numLinks = length;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.linksize = PLANK_HEIGHT;
        this.setObjectScale(scale);
        setPosition(x, y);
        build();
    }

    public void build() {
        planksize = new Vector2(PLANK_WIDTH, linksize);
        linksize = planksize.y;

        // Compute the bridge length
        dimension = new Vector2(0, numLinks);
        float length = dimension.len();
        Vector2 norm = new Vector2(dimension);
        norm.nor();
        norm.rotate(angle);

        // If too small, only make one plank.
        int nLinks = (int) (length / linksize);
        if (nLinks <= 1) {
            nLinks = 1;
//			linksize = length;
            spacing = 0;
        } else {
            spacing = length - nLinks * linksize;
            spacing /= (nLinks - 1);
        }

        // Create the planks
        planksize.y = linksize;
        Vector2 pos = new Vector2();
        for (int ii = 0; ii < nLinks; ii++) {
            float t = ii * (linksize + spacing) + linksize / 2.0f;
            pos.set(norm);
            pos.scl(t);
            pos.add(x, y);
            BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
            plank.setName(PLANK_NAME + ii);
            plank.setDensity(BASIC_DENSITY);
            plank.setAngle((float) Math.toRadians(angle));
            if (ii == 0)
                plank.setBodyType(BodyDef.BodyType.StaticBody);
            plank.setCustomScale(2.5f,1.0f);
            Filter f = new Filter();
            f.maskBits = FilterGroup.WALL | FilterGroup.SLOTH | FilterGroup.HAND;
            f.categoryBits = FilterGroup.VINE;
            plank.setFilterData(f);
            bodies.add(plank);
        }
        final_norm = new Vector2(pos);
        final_norm.add(0, linksize / 2);
    }

    /**
     * Creates the joints for this object.
     * <p>
     * This method is executed as part of activePhysics. This is the primary method to
     * override for custom physics entities.
     *
     * @param world Box2D world to store joints
     * @return true if object allocation succeeded
     */
    protected boolean createJoints(World world) {
        assert bodies.size > 0;

        Vector2 pos = bodies.get(0).getPosition();

        // Definition for a revolute joint
        WeldJointDef jointDef = new WeldJointDef();
        Joint joint;

        Vector2 anchor1 = new Vector2();
        Vector2 anchor2 = new Vector2(0, -linksize / 2);


        pos.y += linksize / 2;
        start = new WheelObstacle(pos.x, pos.y, TRUNK_PIN_RADIUS);
        start.setName(TRUNK_PIN_NAME + 0);
        start.setDensity(BASIC_DENSITY);
        start.setBodyType(BodyDef.BodyType.StaticBody);
        start.activatePhysics(world);

        // Initial joint
        jointDef.bodyA = start.getBody();
        jointDef.bodyB = bodies.get(0).getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        jointDef.collideConnected = false;
        joint = world.createJoint(jointDef);
        joints.add(joint);
        anchor1 = new Vector2(0, linksize / 2);

        // Link the planks together
        for (int ii = 0; ii < bodies.size - 1; ii++) {
            // join the planks
            jointDef = new WeldJointDef();
            jointDef.bodyA = bodies.get(ii).getBody();
            jointDef.bodyB = bodies.get(ii + 1).getBody();
            jointDef.localAnchorA.set(anchor1);
            jointDef.localAnchorB.set(anchor2);
            jointDef.collideConnected = false;
            joint = world.createJoint(jointDef);
            joints.add(joint);
        }

        return true;
    }

    public float getLinksize() {
        return linksize;
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
//		rebuild();
    }
}