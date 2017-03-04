package edu.cornell.gdiac.physics.platform.sloth;

/*
 * SlothModel.java
 *
 * This the sloth!
 */

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.graphics.g2d.*;
import edu.cornell.gdiac.physics.obstacle.*;
import lombok.Getter;

public class SlothModel extends ComplexObstacle {

    /** Constants for tuning sloth behaviour */
    private static final float HAND_DENSITY = 1.0f;
    private static final float ARM_DENSITY = 0.1f;
    private static final float TWO_FREE_FORCE_MULTIPLIER = 5.0f;
    private static final boolean HANDS_FIXED_ROTATION = true;
    private static final float GRAVITY_SCALE = 1.7f;

    /** Indices for the body parts in the bodies array */
    private static final int PART_NONE = -1;
    private static final int PART_RIGHT_ARM = 0;
    private static final int PART_LEFT_ARM = 1;
    private static final int PART_LEFT_HAND = 2;
    private static final int PART_RIGHT_HAND = 3;

    /** The number of DISTINCT body parts */
    private static final int BODY_TEXTURE_COUNT = 6;

    private RevoluteJointDef leftGrabJoint;
    private RevoluteJointDef rightGrabJoint;

    public float x;
    public float y;
    private float rightVert;
    private float leftHori;
    private float leftVert;
    private float rightHori;
    @Getter
    private boolean leftGrab;
    @Getter
    private boolean rightGrab;

    /**
     * Returns the texture index for the given body part
     *
     * As some body parts are symmetrical, we reuse textures.
     *
     * @returns the texture index for the given body part
     */
    private static int partToAsset(int part) {
        switch (part) {
            case PART_LEFT_HAND:
            case PART_RIGHT_HAND:
                return 0;
            case PART_LEFT_ARM:
            case PART_RIGHT_ARM:
                return 2;
            default:
                return -1;
        }
    }

    // Layout of ragdoll
    //  |0|______0______|0| LOL WTF
    //
    /** Dist between arms? */
    private static final float ARM_XOFFSET    = 1.00f;
    private static final float ARM_YOFFSET    = 0;

    private static final float HAND_XOFFSET    = .70f;
    private static final float HAND_YOFFSET    = 0;

    /** Texture assets for the body parts */
    private TextureRegion[] partTextures;

    /** Cache vector for organizing body parts */
    private Vector2 partCache = new Vector2();

    /**
     * Creates a new ragdoll with its head at the given position.
     *
     * @param x  Initial x position of the ragdoll head
     * @param y  Initial y position of the ragdoll head
     */
    public SlothModel(float x, float y) {
        super(x,y);
        this.x = x;
        this.y = y;
    }

    private void init() {
        // We do not do anything yet.
        BoxObstacle part;

        // ARMS
        // Right arm
        part = makePart(PART_RIGHT_ARM, PART_NONE, x, y, ARM_DENSITY);
        part.setGravityScale(GRAVITY_SCALE);

        // Left arm
        part = makePart(PART_LEFT_ARM, PART_RIGHT_ARM, ARM_XOFFSET, ARM_YOFFSET, ARM_DENSITY);
        part.setAngle((float)Math.PI);
        part.setGravityScale(GRAVITY_SCALE);

        // HANDS
        // Left hand
        part = makePart(PART_LEFT_HAND, PART_LEFT_ARM, ARM_XOFFSET, ARM_YOFFSET, HAND_DENSITY);
        part.setFixedRotation(HANDS_FIXED_ROTATION);
        part.setGravityScale(GRAVITY_SCALE);
        // Right hand
        part = makePart(PART_RIGHT_HAND, PART_RIGHT_ARM, ARM_XOFFSET, ARM_YOFFSET, HAND_DENSITY);
        part.setFixedRotation(HANDS_FIXED_ROTATION);
        part.setGravityScale(GRAVITY_SCALE);
    }

    /**
     * Sets the drawing scale for this physics object
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x  the x-axis scale for this physics object
     * @param y  the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y) {
        super.setDrawScale(x,y);

        if (partTextures != null && bodies.size == 0) {
            init();
        }
    }

    /**
     * Sets the array of textures for the individual body parts.
     *
     * The array should be BODY_TEXTURE_COUNT in size.
     *
     * @param textures the array of textures for the individual body parts.
     */
    public void setPartTextures(TextureRegion[] textures) {
        assert textures != null && textures.length > BODY_TEXTURE_COUNT : "Texture array is not large enough";

        partTextures = new TextureRegion[BODY_TEXTURE_COUNT];
        System.arraycopy(textures, 0, partTextures, 0, BODY_TEXTURE_COUNT);
        if (bodies.size == 0) {
            init();
        } else {
            for(int ii = 0; ii <= 2; ii++) {
                ((SimpleObstacle)bodies.get(ii)).setTexture(partTextures[partToAsset(ii)]);
            }
        }
    }

    /**
     * Helper method to make a single body part
     *
     * While it looks like this method "connects" the pieces, it does not really.  It
     * puts them in position to be connected by joints, but they will fall apart unless
     * you make the joints.
     *
     * @param part		The part to make
     * @param connect	The part to connect to
     * @param x 		The x-offset RELATIVE to the connecting part
     * @param y			The y-offset RELATIVE to the connecting part
     *
     * @return the newly created part
     */
    private BoxObstacle makePart(int part, int connect, float x, float y, float density) {
        TextureRegion texture = partTextures[partToAsset(part)];

        partCache.set(x,y);
        if (connect != PART_NONE) {
            partCache.add(bodies.get(connect).getPosition());
        }

        float dwidth  = texture.getRegionWidth()/drawScale.x;
        float dheight = texture.getRegionHeight()/drawScale.y;

        BoxObstacle body = new TrevorObstacle(partCache.x, partCache.y, dwidth, dheight);
        body.setDrawScale(drawScale);
        body.setTexture(texture);
        body.setDensity(density);
        bodies.add(body);
        return body;
    }

    /**
     * Creates the joints for this object.
     *
     * We implement our custom logic here.
     *
     * @param world Box2D world to store joints
     *
     * @return true if object allocation succeeded
     */
    protected boolean createJoints(World world) {
        assert bodies.size > 0;

        // ARM TO ARM WOW
        createJoint(world, PART_LEFT_ARM, PART_RIGHT_ARM, ARM_XOFFSET/2, 0, -ARM_XOFFSET/2, 0);

        // HANDS
        createJoint(world, PART_LEFT_ARM, PART_LEFT_HAND, -HAND_XOFFSET, 0, 0, 0);
        createJoint(world, PART_RIGHT_ARM, PART_RIGHT_HAND, HAND_XOFFSET, 0, 0, 0);

        return true;
    }

    private void createJoint(World world, int partA, int partB, float ox1, float oy1, float ox2, float oy2) {
        Vector2 anchorA = new com.badlogic.gdx.math.Vector2(ox1, oy1);
        Vector2 anchorB = new com.badlogic.gdx.math.Vector2(ox2, oy2);

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = bodies.get(partA).getBody(); // barrier
        jointDef.bodyB = bodies.get(partB).getBody(); // pin
        jointDef.localAnchorA.set(anchorA);
        jointDef.localAnchorB.set(anchorB);
        jointDef.collideConnected = false;
        //jointDef.lowerAngle = (float) (- Math.PI/4);
        //jointDef.upperAngle = (float) (Math.PI/4);
        //jointDef.enableLimit = true;
        Joint joint = world.createJoint(jointDef);
        joints.add(joint);
    }

    public void setLeftHori(float leftHori) {
        this.leftHori = leftHori;
    }

    public void setLeftVert(float leftVert) {
        this.leftVert = leftVert;
    }

    public void setRightHori(float rightHori) {
        this.rightHori = rightHori;
    }

    public void setRightVert(float rightVert) {
        this.rightVert = rightVert;
    }

    /**
     * DOES EVERYTHING!!
     */
    public void doThePhysics() {
        Obstacle right = bodies.get(2);
        Obstacle left = bodies.get(3);

        // Apply forces
        left
                .getBody()
                .applyForce(leftHori*TWO_FREE_FORCE_MULTIPLIER, -leftVert*TWO_FREE_FORCE_MULTIPLIER, left.getX(), left.getY(), true);
        right
                .getBody()
                .applyForce(rightHori*TWO_FREE_FORCE_MULTIPLIER, -rightVert*TWO_FREE_FORCE_MULTIPLIER, right.getX(), right.getY(), true);
    }

    public void setLeftGrab(float leftGrab) {
        this.leftGrab = leftGrab > 0;
    }

    public void setRightGrab(float rightGrab) {
        this.rightGrab = rightGrab > 0;
    }

}

