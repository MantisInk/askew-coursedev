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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import lombok.Getter;
import lombok.Setter;

/**
 * A tree trunk with planks connected by weld joints.
 * <p>
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Trunk extends TreeParent {

    /**
     * The spacing between each link
     */
    private transient Vector2 dimension;
    /**
     * The size of the entire bridge
     */

    @Getter
    @Setter
    private float x;
    @Getter
    @Setter
    private float y;
    @Getter
    @Setter
    private float angle;
    @Getter
    @Setter
    private float numLinks;                                    // param for json constructor


    private transient Vector2 final_norm = null;
    /**
     * coords for starting branch off this trunk
     */
    public transient static final float DAMPING_ROTATION = 5f;
    /**
     * Set damping constant for joint rotation in vines
     */
    private transient Vector2 planksize;
    /**
     * The size of a single plank
     */
    // TODO: Fix this from being public (refactor artifact) ?
    private transient float spacing = 0.0f;

    /**
     * The spacing between each link
     */


    public Trunk(float x, float y, float length, float angle) {
        super(x, y);
        setName(TRUNK_NAME);
        numLinks = length;
        this.x = x;
        this.y = y;
        this.angle = angle;
        setPosition(x, y);
        build();
    }

    public void build() {
        planksize = new Vector2(lwidth, lheight);

        // Compute the bridge length
        dimension = new Vector2(0, numLinks);
        float length = dimension.len();
        Vector2 norm = new Vector2(dimension);
        norm.nor();
        norm.rotate(angle);

        // If too small, only make one plank.
        int nLinks = (int) (length / lheight);
        if (nLinks <= 1) {
            nLinks = 1;
//			lheight = length;
            spacing = 0;
        } else {
            spacing = length - nLinks * lheight;
            spacing /= (nLinks - 1);
        }

        // Create the planks
        planksize.y = lheight;
        Vector2 pos = new Vector2();
        for (int ii = 0; ii < nLinks; ii++) {
            float t = ii * (lheight + spacing) + lheight / 2.0f;
            pos.set(norm);
            pos.scl(t);
            pos.add(x, y);
            BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
            plank.setName(PLANK_NAME + ii);
            plank.setDensity(BASIC_DENSITY);
            plank.setAngle((float) Math.toRadians(angle));
            plank.setBodyType(BodyDef.BodyType.StaticBody);
            plank.setDrawScale(drawScale);
            Filter f = new Filter();
            f.maskBits = FilterGroup.WALL | FilterGroup.SLOTH | FilterGroup.HAND;
            f.categoryBits = FilterGroup.VINE;
            plank.setFilterData(f);
            bodies.add(plank);
        }
        final_norm = new Vector2(pos);
        final_norm.add(0, lheight / 2);
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

        // Definition for a revolute joint
        WeldJointDef jointDef;

        Joint joint;

        Vector2 anchor1 = new Vector2(0, lheight / 2);
        Vector2 anchor2 = new Vector2(0, -lheight / 2);

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

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
    }
}