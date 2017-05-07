package askew.entity.sloth;

/*
 * SlothModel.java
 *
 * This the sloth!
 */

import askew.GameCanvas;
import askew.GlobalConfiguration;
import askew.InputControllerManager;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import lombok.Getter;
import lombok.Setter;

public class SlothModel extends ComplexObstacle  {

    /** Constants for tuning sloth behaviour */
    private static final float HAND_DENSITY = 10.0f;
    private transient float ARM_DENSITY;
    private static final float BODY_MASS = 0.5903138f;
    private transient float TORQUE;
    private static final boolean BODY_FIXED_ROTATION = true;
    private static final boolean HANDS_FIXED_ROTATION = true;
    private transient float GRAVITY_SCALE;
    private transient boolean GRABBING_HAND_HAS_TORQUE;
    private transient float OMEGA_NORMALIZER;

    @Setter @Getter
    public transient int controlMode;
    /** After flying this distance, flow starts to experience some serious
     * air resistance.
     */
    private static final float FLOW_RESISTANCE_DAMPING_LAMBDA = 23f;

    public static final int GRAB_ORIGINAL = 0;
    public static final int GRAB_REVERSE = 1;
    public static final int GRAB_TOGGLE = 2;

    public static final int CONTROLS_ORIGINAL = 0;
    public static final int CONTROLS_ONE_ARM = 1;

    public static final int CAN_DOUBLE_GRAB = 1;

    /** Indices for the body parts in the bodies array */
    private static final int PART_NONE = -1;
    private static final int PART_BODY = 0;
    private static final int PART_RIGHT_ARM = 1;
    private static final int PART_LEFT_ARM = 2;
    private static final int PART_LEFT_HAND = 3;
    private static final int PART_RIGHT_HAND = 4;
    private static final int PART_HEAD = 5;
    private static final int PART_POWER_GLOW = 8;

    private static final float PI = (float)Math.PI;

    /** The number of DISTINCT body parts */
    private static final int BODY_TEXTURE_COUNT = 9;

    private transient RevoluteJointDef leftGrabJointDef;
    private transient RevoluteJointDef rightGrabJointDef;
    private transient Joint leftGrabJoint;
    private transient Joint rightGrabJoint;
    private transient PolygonShape sensorShape;
    private transient Fixture sensorFixture1;
    private transient Fixture sensorFixture2;

    private transient Body leftTarget;
    private transient Body rightTarget;

    // help lines for tutorial mode
    public static final int DEFAULT = -1;
    public static final int SHIMMY_E = 0;
    public static final int SHIMMY_SE = 1;
    public static final int SHIMMY_S = 2;
    public static final int SHIMMY_SW = 3;
    public static final int SHIMMY_W = 4;
    public static final int SHIMMY_NW = 5;
    public static final int SHIMMY_N = 6;
    public static final int SHIMMY_NE = 7;
    public static final int PLUS_30 = 8;
    public static final int MINUS_30 = 9;

    /** Set damping constant for rotation of Flow's arms */
    private static final float ROTATION_DAMPING = 5f;

    private  transient Body grabPointR;
    private transient Body grabPointL;

    private transient Vector2 forceL = new Vector2();
    private transient Vector2 forceR = new Vector2();

    private transient CircleShape grabGlow = new CircleShape();

    private transient WheelObstacle pin;

    //JSON
    @Getter @Setter
    public float x;
    @Getter @Setter
    public float y;

    private float lTheta;
    private float rTheta;

    private transient float rightVert;      // right joystick y input
    private transient float leftHori;       // left joystick z input
    private transient float leftVert;       // left joystick y input
    private transient float rightHori;      // right joystick x input

    @Getter
    private transient boolean leftGrab;
    @Getter
    private transient boolean rightGrab;
    private transient boolean prevLeftGrab;
    private transient boolean prevRightGrab;
    private transient boolean leftStickPressed;
    private transient boolean rightStickPressed;
    private transient float flowFacingState;
    @Getter
    private transient float power;

    @Getter @Setter
    private transient int movementMode;
    private transient boolean leftGrabbing;
    private transient boolean rightGrabbing;
    @Getter
    private transient boolean grabbedEntity;
    @Getter
    private transient boolean releasedEntity;
    private transient boolean leftCanGrabOrIsGrabbing;
    private transient boolean didSafeGrab;
    private transient boolean setLastGrabX;
    private transient float lastGrabX;
    private transient boolean dismembered;
    private transient boolean pinned = false;
    private transient boolean didOneArmCheck;
    private transient boolean waitingForSafeRelease;
    private transient boolean tutorial = false;

    /**
     * Returns the texture index for the given body part
     *
     * As some body parts are symmetrical, we reuse texture.
     *
     * @returns the texture index for the given body part
     */
    private static int partToAsset(int part) {
        switch (part) {
            case PART_LEFT_HAND:
            case PART_RIGHT_HAND:
            case PART_HEAD:
                return 0;
            case PART_LEFT_ARM:
                return 3;
            case PART_RIGHT_ARM:
                return 1;
            case PART_BODY:
                return 2;
            default:
                return -1;
        }
    }

    // Layout of ragdoll
    //  |0|______0______|0|
    //
    /** Dist between arms? */

    private static final float SHOULDER_XOFFSET    = 0.00f;
    private static final float SHOULDER_YOFFSET    = 0.10f;

    private static final float HAND_YOFFSET    = 0;

    private static final float BODY_HEIGHT = 1.4f;
    private static final float BODY_WIDTH = 1.8f * (489f / 835f);

    private static final float ARM_WIDTH = 1.75f;
    private static final float ARM_HEIGHT = 0.5f;

    private static final float ARM_XOFFSET    = ARM_WIDTH / 2f + .375f;
    private static final float ARM_YOFFSET    = 0f;

    private static final float HAND_WIDTH = 0.1125f;
    private static final float HAND_HEIGHT = 0.1125f;
    //private static final float HAND_XOFFSET  = (ARM_WIDTH / 2f) - HAND_WIDTH/2;
    private static final float HAND_XOFFSET  = (ARM_WIDTH / 2f) - HAND_WIDTH * 2 - .07f;

    public static final float ARMSPAN = ARM_XOFFSET*2 - 0.05f;


    /** Texture assets for the body parts */
    private transient TextureRegion[] partTextures;

    /** Cache vector for organizing body parts */
    private transient Vector2 partCache = new Vector2();

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

        this.setObjectScale(1.0f/1.5f,1.0f/1.5f);
        this.TORQUE = GlobalConfiguration.getInstance().getAsFloat("flowTorque");
        this.GRAVITY_SCALE = GlobalConfiguration.getInstance().getAsFloat("flowGravityScale");
        this.GRABBING_HAND_HAS_TORQUE = GlobalConfiguration.getInstance().getAsBoolean("flowCanMoveGrabbingHand");
        this.OMEGA_NORMALIZER = GlobalConfiguration.getInstance().getAsFloat("flowOmegaNormalizer");
        this.ARM_DENSITY = GlobalConfiguration.getInstance().getAsFloat("flowArmDensity");
        this.movementMode = GlobalConfiguration.getInstance().getAsInt("flowMovementMode");
        this.controlMode = GlobalConfiguration.getInstance().getAsInt
                ("flowControlMode");
        if (!InputControllerManager.getInstance().getController(0).getXbox().isConnected()) controlMode = CONTROLS_ONE_ARM;
        this.rightGrabbing = false;
        this.leftGrabbing =  true;
    }

    public void build(){
        init();
    }
 	public void rebuild(){
        bodies.clear();
        build();
    }
 	public void setPosition(float x , float y){
 	    super.setPosition(x,y);
 	    this.x = x;
 	    this.y = y;
 	    rebuild();
    }

    private void init() {
        // We do not do anything yet.
        BoxObstacle part;

        // Body
        part = makePart(PART_BODY, PART_NONE, x, y,BODY_WIDTH,BODY_HEIGHT, 0,true);
        part.setFixedRotation(BODY_FIXED_ROTATION);
        part.setGravityScale(GRAVITY_SCALE);
        part.setLinearDamping(0.08f); // small amount to balance linear gimp

        // ARMS
        // Right arm
        part = makePart(PART_RIGHT_ARM, PART_BODY, SHOULDER_XOFFSET + ARM_XOFFSET/2f, SHOULDER_YOFFSET + ARM_YOFFSET,ARM_WIDTH,ARM_HEIGHT, ARM_DENSITY,false);
//        part.setAngle((float)Math.PI);
        part.setGravityScale(GRAVITY_SCALE);
        //part.setMass(ARM_MASS);

        // Left arm
        part = makePart(PART_LEFT_ARM, PART_BODY, -ARM_XOFFSET/2f, -ARM_YOFFSET,ARM_WIDTH,ARM_HEIGHT, ARM_DENSITY,false);
        part.setAngle((float)Math.PI);
        part.setGravityScale(GRAVITY_SCALE);
        //part.setMass(ARM_MASS);

        // HANDS
        // Left hand
        part = makePart(PART_LEFT_HAND, PART_LEFT_ARM, ARM_XOFFSET, ARM_YOFFSET, HAND_WIDTH, HAND_HEIGHT, HAND_DENSITY,false);
        part.setFixedRotation(HANDS_FIXED_ROTATION);
        part.setGravityScale(GRAVITY_SCALE);

        // Right hand
        part = makePart(PART_RIGHT_HAND, PART_RIGHT_ARM, ARM_XOFFSET, ARM_YOFFSET, HAND_WIDTH, HAND_HEIGHT, HAND_DENSITY,false);
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
    private BoxObstacle makePart(int part, int connect, float x, float y, float width, float height, float density, boolean collides) {
        TextureRegion texture = partTextures[partToAsset(part)];

        partCache.set(x,y);
        if (connect != PART_NONE) {
            partCache.add(bodies.get(connect).getPosition());
        }

        //width and height are in box2d units
        float dWidth  = width*objectScale.x;
        float dHeight = height*objectScale.x;


        BoxObstacle body;
        if(collides){
            body = new BoxObstacle(partCache.x, partCache.y, dWidth, dHeight);
            Filter f = new Filter();
            f.maskBits = FilterGroup.WALL;
            f.categoryBits = FilterGroup.SLOTH;
            body.setFilterData(f);
        }
        else{
            body = new BoxObstacle(partCache.x, partCache.y, dWidth, dHeight);
            body.setFriction(.4f);
            Filter f = new Filter();
            f.maskBits = FilterGroup.NOCOLLIDE;
            f.categoryBits = FilterGroup.ARM;
            body.setFilterData(f);
        }

        body.setDrawScale(drawScale);
        body.setTexture(texture);
        body.setDensity(density);
        body.setName("slothpart");

        bodies.add(body);

        // Prevent any missed collisions
        body.setBullet(true);

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

        // BODY TO ARM WOW
        createJoint(world, PART_BODY, PART_RIGHT_ARM, SHOULDER_XOFFSET/2, SHOULDER_YOFFSET, -ARM_XOFFSET/2, 0);
        createJoint(world, PART_BODY, PART_LEFT_ARM, SHOULDER_XOFFSET/2, SHOULDER_YOFFSET, -ARM_XOFFSET/2, 0);

        // HANDS
        createJoint(world, PART_LEFT_ARM, PART_LEFT_HAND, HAND_XOFFSET, 0, 0, 0);
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

    public float getLeftHori() {return this.leftHori;}

    public float getLeftVert() {return this.leftVert;}

    public float getRightHori() {return this.rightHori;}

    public float getRightVert() {return this.rightVert;}

    public Body getMainBody() {return bodies.get(0).getBody();}

    //theta is in radians between 0 and pi
    public float calculateTorque(float deltaTheta, float omega){
        //return (float) Math.max(-1.0f,Math.min(1.0f, 1.2 * Math.sin(deltaTheta)));
        return (float)((10.0 / (1 + Math.exp(omega + (deltaTheta *4)))) - 5);//#MAGIC 4, DELTA THETA NORMALIZER
    }

    /**
     * DOES EVERYTHING!!
     */

    public float angleDiff(float goal, float current){
        float diff = goal - current;
        if(diff > PI){ diff -= (PI + PI);}
        if(diff < -PI){ diff += (PI + PI);}
        return diff;
    }


    private transient float previousAngleLeft;
    private transient float previousAngleRight;
    private transient float cummulativeAngleLeft = 0;
    private transient float cummulativeAngleRight = 0;


    public void doThePhysics() {
        Obstacle rightHand = bodies.get(PART_RIGHT_HAND);
        Obstacle leftHand = bodies.get(PART_LEFT_HAND);

        Obstacle rightArm = bodies.get(PART_RIGHT_ARM);
        Obstacle leftArm = bodies.get(PART_LEFT_ARM);

        if (controlMode == CONTROLS_ONE_ARM) {
            if (leftCanGrabOrIsGrabbing && isActualLeftGrab() ||
                    (!leftCanGrabOrIsGrabbing && !isActualRightGrab())) {
                rightHori = leftHori;
                rightVert = leftVert;
                leftHori = 0;
                leftVert = 0;
            } else {
                rightHori = 0;
                rightVert = 0;
            }
        }

        //TODO REDUCE MAGIC NUMBERS ( HENRY )
        // Apply forces
        float lcTheta = (float)Math.atan2(leftVert,leftHori);
        lTheta = (-leftArm.getAngle()) + PI;
        lTheta = ((lTheta%(2*PI)) + (2*PI)) % (2*PI) - PI;
        float leftAngularVelocity = leftArm.getAngularVelocity() * 2;
        float lLength = (float)Math.sqrt((leftVert * leftVert) + (leftHori * leftHori));
        float dLTheta = angleDiff(lcTheta,lTheta);

        float rcTheta = (float)Math.atan2(rightVert,rightHori);
        rTheta = -rightArm.getAngle() + PI;
        rTheta = ((rTheta%(2*PI)) + (2*PI)) % (2*PI) - PI;
        float rightAngularVelocity = rightArm.getAngularVelocity() * 2;
        float rLength = (float)Math.sqrt((rightVert * rightVert) + (rightHori * rightHori));
        float dRTheta = angleDiff(rcTheta,rTheta);

        //antiwobble

        float currentAngleLeft = lcTheta;
        float currentAngleRight = rcTheta;

        float angleChangeLeft = angleDiff(currentAngleLeft, previousAngleLeft);
        float angleChangeRight = angleDiff(currentAngleRight,previousAngleRight);
        if(Math.abs(angleChangeRight) > .03f){
            cummulativeAngleRight += angleChangeRight;
        }
        else{

            if(Math.abs(cummulativeAngleRight) > 15) {
                //System.out.print("Right angle reset");
            }
            cummulativeAngleRight = 0;
        }
        if(Math.abs(angleChangeLeft) > .03f){
            cummulativeAngleLeft += angleChangeLeft;
        }
        else{

            if(Math.abs(cummulativeAngleLeft) > 15){
                //System.out.print("Left angle reset");
            }
            cummulativeAngleLeft = 0;
        }

        //System.out.println("Left delta: " + angleChangeLeft +"    : Right delta: "+ angleChangeRight);
        //if(Math.abs(cummulativeAngleLeft) > 15 || Math.abs(cummulativeAngleRight) > 15) {
            //System.out.println("Left delta: " + cummulativeAngleLeft + "    : Right delta: " + cummulativeAngleRight);
        //}
        previousAngleRight = currentAngleRight;
        previousAngleLeft = currentAngleLeft;

        float impulseL = 0;
        float nextLTheta = -leftArm.getAngle() + PI - leftArm.getAngularVelocity() / 20f;
        nextLTheta = ((nextLTheta % (2 * PI)) + (2 * PI)) % (2 * PI) - PI;
        float totalRotL = angleDiff(lcTheta, nextLTheta);
        if(Math.abs(angleChangeLeft) < .05f) {
            if (totalRotL * dLTheta < 0 && lLength > .4f) {
                impulseL = ((leftHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + leftArm.getInertia()) * leftArm.getAngularVelocity() * -1 * 60 / 2;
            }

            if (isActualLeftGrab()) {
                impulseL = impulseL * 6;
            }
        }


        float impulseR = 0;
        float nextRTheta = -rightArm.getAngle() + PI - rightArm.getAngularVelocity() / 20f;
        nextRTheta = ((nextRTheta % (2 * PI)) + (2 * PI)) % (2 * PI) - PI; //ltheta is correct
        float totalRotR = angleDiff(rcTheta, nextRTheta);
        if(Math.abs(angleChangeRight) < .05f) {
            if (totalRotR * dRTheta < 0 && rLength > .4f) {
                impulseR = ((rightHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + rightArm.getInertia()) * rightArm.getAngularVelocity() * -1 * 60 / 2;
            }

            if (isActualRightGrab()) {
                impulseR = impulseR * 6;
            }
        }

        //countertorque left stick on right arm
        float dLcRTheta = 0f; // How much left controller affects right arm
        float invlcTheta = (float)Math.atan2(-leftVert,-leftHori);
        dLcRTheta = angleDiff(invlcTheta, rTheta);

        //countertorque right stick on left arm
        float dRcLTheta = 0f; // How much right controller affects left arm
        float invrcTheta = (float)Math.atan2(-rightVert,-rightHori);
        dRcLTheta = angleDiff(invrcTheta, lTheta);

        //anticounterwobble right arm
        float cwtrL = angleDiff(invrcTheta, nextLTheta);
        float cwtrR = angleDiff(invlcTheta, nextRTheta);

        float counterfactor = .3f;
        float counterfR =0;
        float counterfL = 0;
        float cimpulseR = 0;
        float cimpulseL = 0;
        if (isActualLeftGrab()  &&  rLength > .4f ) {
            counterfL = counterfactor * calculateTorque(dRcLTheta, leftAngularVelocity / OMEGA_NORMALIZER);
            if(dRcLTheta * cwtrL < 0  && Math.abs(angleChangeRight) < .05f){
                cimpulseL = ((leftHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + leftArm.getInertia()) * leftArm.getAngularVelocity() * -1 * 60 * 3 * (1-lLength) ;
            }
        }
        if (isActualRightGrab()  && lLength > .4f ) {
            counterfR = counterfactor * calculateTorque(dLcRTheta, rightAngularVelocity / OMEGA_NORMALIZER);
            if(dLcRTheta * cwtrR < 0 && Math.abs(angleChangeLeft) < .05f){
                cimpulseR = ((rightHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + rightArm.getInertia()) * rightArm.getAngularVelocity() * -1 * 60 * 3 * (1-rLength) ;
            }
        }

        float forceLeft =  calculateTorque(dLTheta,leftAngularVelocity/OMEGA_NORMALIZER); //#MAGIC 20f default, omega normalizer
        float forceRight = calculateTorque(dRTheta,rightAngularVelocity/OMEGA_NORMALIZER);

        if(impulseL > 0 && !pinned)
            forceLeft *= .3f;

        if(impulseR > 0 && !pinned)
            forceRight *= .3f;

        // if in pinned, turn off auto-assist
        if(pinned) {
            impulseL = 0;
            impulseR = 0;
            cimpulseL = 0;
            cimpulseR = 0;
        }

        float lTorque = TORQUE * ((forceLeft  * lLength) + TORQUE * (counterfL * rLength  )) + impulseL + cimpulseL;
        float rTorque = TORQUE * ((forceRight * rLength) + TORQUE * ( counterfR * lLength )) + impulseR + cimpulseR;
        forceL.set((float) (lTorque * Math.sin(lTheta)),(float) (lTorque * Math.cos(lTheta)));
        forceR.set((float) (rTorque * Math.sin(rTheta)),(float) (rTorque * Math.cos(rTheta)));

        // ANTI GIMP - Trevor. Filled with magic ###s
        /*
        float maxVelocity = Math.max(Math.abs(rightAngularVelocity),Math.abs(leftAngularVelocity));
        float gimpScale = 1.0f;
        float CUTOFF = 17;
        if (maxVelocity > CUTOFF) {
            System.out.println("MaxVel:" + maxVelocity);
            gimpScale = (float) Math.exp(-( (maxVelocity-CUTOFF) / 4.5f));
        }

        lTorque *= gimpScale;
        rTorque *= gimpScale;
        */


        if ((GRABBING_HAND_HAS_TORQUE || !isActualLeftGrab()) )
            leftArm
                    .getBody()
                    .applyTorque(lTorque,true);
        if ((GRABBING_HAND_HAS_TORQUE || !isActualRightGrab()) )
            rightArm
                    .getBody()
                    .applyTorque(rTorque, true);

        float torquePower = (float) Math.sqrt(lTorque * lTorque + rTorque * rTorque);
        this.power += (torquePower / 22f - power) * 0.10f;
        if (this.power > 1) this.power = 1;

        flowFacingState = (int)bodies.get(PART_BODY).getBody().getLinearVelocity().x;
    }


    public boolean isActualRightGrab() {
        return this.rightGrabJoint != null;
    }

    public boolean isActualLeftGrab() {
        return this.leftGrabJoint != null;
    }

    public void drawForces(GameCanvas canvas, Affine2 camTrans){
        Obstacle right = bodies.get(PART_RIGHT_HAND);
        Obstacle left = bodies.get(PART_LEFT_HAND);

        Gdx.gl.glLineWidth(3);
        float left_x = left.getX();
        float left_y = left.getY();
        float right_x = right.getX();
        float right_y = right.getY();


        canvas.beginDebug(camTrans);
        canvas.drawLine(left.getX()*drawScale.x,left.getY() * drawScale.y, left.getX()*drawScale.x+(forceL.x*2),left.getY() * drawScale.y+(forceL.y*2),Color.BLUE, Color.BLUE);
        canvas.drawLine(right.getX()*drawScale.x,right.getY() * drawScale.y, right.getX()*drawScale.x+(forceR.x*2),right.getY() * drawScale.y+(forceR.y*2),Color.RED, Color.RED);
        canvas.endDebug();
    }

    public void drawGrab(GameCanvas canvas, Affine2 camTrans){
        Obstacle right = bodies.get(PART_RIGHT_HAND);
        Obstacle left = bodies.get(PART_LEFT_HAND);
        grabGlow.setRadius(.12f);
        Gdx.gl.glLineWidth(3);
        canvas.beginDebug(camTrans);
        if(isLeftGrab())  {
            if (isActualLeftGrab()) {
                canvas.drawPhysics(grabGlow, new Color(0xABCDEF),left.getX() , left.getY() , drawScale.x,drawScale.y );
            } else {
                canvas.drawPhysics(grabGlow, new Color(0xcfcf000f),left.getX() , left.getY() , drawScale.x,drawScale.y );
            }
        }
        if(isRightGrab()) {
            if (isActualRightGrab()) {
                canvas.drawPhysics(grabGlow, new Color(0xABCDEF),right.getX() , right.getY() , drawScale.x,drawScale.y );
            } else {
                canvas.drawPhysics(grabGlow, new Color(0xcfcf000f),right.getX() , right.getY() ,drawScale.x,drawScale.y );
            }
        }

        canvas.endDebug();
    }

    public void setLeftGrab(boolean leftGrab) {
        if (controlMode == CONTROLS_ONE_ARM && !didOneArmCheck) return;
        if (controlMode != CONTROLS_ONE_ARM) releasedEntity = false;
        if (movementMode == GRAB_ORIGINAL)
            this.leftGrab = leftGrab;
        else if (movementMode == GRAB_REVERSE)
            this.leftGrab = !leftGrab;
        else if (movementMode == GRAB_TOGGLE && leftGrab) {
            leftGrabbing = !leftGrabbing && (controlMode == CONTROLS_ORIGINAL);
            this.leftGrab = leftGrabbing;
        }
        didOneArmCheck = false;
    }

    public void setRightGrab(boolean rightGrab) {
        if (controlMode != CONTROLS_ONE_ARM || didOneArmCheck) {
            if (movementMode == GRAB_ORIGINAL)
                this.rightGrab = rightGrab;
            else if (movementMode == GRAB_REVERSE)
                this.rightGrab = !rightGrab;
            else if (movementMode == GRAB_TOGGLE && rightGrab) {
                if (!rightGrabbing) {
                    rightGrabbing = true;
                }
                else {
                    rightGrabbing = false;
                }
                this.rightGrab = rightGrabbing;
            }
        } else {
            setOneGrab(rightGrab);
        }
        didOneArmCheck = false;
    }

    public void setLeftStickPressed(boolean leftStickPressed) {
        this.leftStickPressed = leftStickPressed;
    }

    public void setRightStickPressed(boolean rightStickPressed) {
        this.rightStickPressed = rightStickPressed;
    }

    public float getLTheta() { return lTheta;}

    public float getRTheta() {return rTheta;}

    public Body getLeftHand() {return bodies.get(PART_LEFT_HAND).getBody();}

    public Body getRightHand() {return bodies.get(PART_RIGHT_HAND).getBody();}

    public Body getLeftTarget() {return leftTarget;}

    public Body getRightTarget() {return rightTarget;}

    public Body getLeftmostTarget() {
        if (leftTarget != null && rightTarget != null) {
            if(leftTarget.getPosition().x < rightTarget.getPosition().x) {
                return leftTarget;
            } else {
                return rightTarget;
            }
        }
        if (rightTarget != null) {
            return rightTarget;
        } else {
            return leftTarget;
        }
    }

    public Body getRightmostTarget() {
        if (leftTarget != null && rightTarget != null) {
            if(leftTarget.getPosition().x >= rightTarget.getPosition().x) {
                return leftTarget;
            } else {
                return rightTarget;
            }
        }
        if (leftTarget != null) {
            return leftTarget;
        } else {
            return rightTarget;
        }
    }

    public Body getTopTarget() {
        if (leftTarget != null && rightTarget != null) {
            if(leftTarget.getPosition().y > rightTarget.getPosition().y) {
                return leftTarget;
            } else {
                return rightTarget;
            }
        }
        if (rightTarget != null) {
            return rightTarget;
        } else {
            return leftTarget;
        }
    }

    public Body getBottomTarget() {
        if (leftTarget != null && rightTarget != null) {
            if(leftTarget.getPosition().y <= rightTarget.getPosition().y) {
                return leftTarget;
            } else {
                return rightTarget;
            }
        }
        if (rightTarget != null) {
            return rightTarget;
        } else {
            return leftTarget;
        }
    }

    public Obstacle getLeftArm() {
        return bodies.get(PART_LEFT_ARM);
    }

    public Obstacle getRightArm(){
        return bodies.get(PART_RIGHT_ARM);
    }

    public void grab(World world, Body target, boolean leftHand) {
        if (didSafeGrab) return;
        Joint grabJoint;
        Joint otherGrabJoint;
        RevoluteJointDef grabJointDef;
        Obstacle hand;
        Obstacle otherHand;
        grabbedEntity = false;

        if (leftHand) {
            grabJoint = leftGrabJoint;
            otherGrabJoint = rightGrabJoint;
            hand =  bodies.get(PART_LEFT_HAND);
            otherHand = bodies.get(PART_RIGHT_HAND);
        } else {
            grabJoint = rightGrabJoint;
            otherGrabJoint = leftGrabJoint;
            hand =  bodies.get(PART_RIGHT_HAND);
            otherHand = bodies.get(PART_LEFT_HAND);
        }

        if (grabJoint != null || target == null) return;

        Vector2 pos = hand.getPosition();
        if(CAN_DOUBLE_GRAB == 1){
            if(otherGrabJoint != null){
                if((otherHand.getPosition().dst(hand.getPosition()) ) < .3f){
                    pos = otherHand.getPosition();
                }
            }
        }
        Vector2 anchorHand = new com.badlogic.gdx.math.Vector2(0, 0);
        Vector2 anchorTarget = target.getLocalPoint(pos);

        //RevoluteJointDef jointDef = new RevoluteJointDef();
        grabJointDef = new RevoluteJointDef();
        grabJointDef.bodyA = hand.getBody(); // barrier

        grabJointDef.bodyB = target;
        grabJointDef.localAnchorA.set(anchorHand);
        grabJointDef.localAnchorB.set(anchorTarget);
        grabJointDef.collideConnected = false;
        //jointDef.lowerAngle = (float) (- Math.PI/4);
        //jointDef.upperAngle = (float) (Math.PI/4);
        //jointDef.enableLimit = true;
        grabJoint = world.createJoint(grabJointDef);
        if (leftHand) {
            leftGrabJoint = grabJoint;
            leftTarget = target;
        } else {
            rightGrabJoint = grabJoint;
            rightTarget = target;
        }
        // set data as grabbed for pinned to shade grabbed stuff
        target.setUserData("grabbed");

        joints.add(grabJoint);
        grabbedEntity = true;
    }

    public void releaseLeft(World world) {
        if (didSafeGrab) return;
        if (leftGrabJoint != null) {
            joints.removeValue(leftGrabJoint,true);
            world.destroyJoint(leftGrabJoint);
            leftCanGrabOrIsGrabbing = false;
            releasedEntity = true;
            leftGrabJoint = null;
            leftTarget = null;
        }
    }

    public void releaseRight(World world) {
        if (didSafeGrab) return;
        if (rightGrabJoint != null) {
            joints.removeValue(rightGrabJoint,true);
            world.destroyJoint(rightGrabJoint);
            leftCanGrabOrIsGrabbing = true;
            releasedEntity = true;
            rightGrabJoint = null;
            rightTarget = null;
        }
    }

    @Override
    public void deactivatePhysics(World world) {
        if(grabPointL != null)
            world.destroyBody(grabPointL);
        if(grabPointR != null)
            world.destroyBody(grabPointR);
        if (leftGrabJoint != null) {
            joints.removeValue(leftGrabJoint,true);
            // not sure why but dont destroy these joints. it crashes the game. -trevor
//            world.destroyJoint(leftGrabJoint);
            leftCanGrabOrIsGrabbing = false;
            releasedEntity = true;
            leftGrabJoint = null;
            leftTarget = null;
        }
        if (rightGrabJoint != null) {
            joints.removeValue(rightGrabJoint,true);
//            world.destroyJoint(rightGrabJoint);
            leftCanGrabOrIsGrabbing = true;
            releasedEntity = true;
            rightGrabJoint = null;
            rightTarget = null;
        }
        super.deactivatePhysics(world);
    }

    public void activateSlothPhysics(World world) {
        float MN_SENSOR_HEIGHT = HAND_HEIGHT/2f;
        float MN_SENSOR_WIDTH = HAND_WIDTH/2f;
        Vector2 sensorCenter = new Vector2(0, 0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0.0f;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(MN_SENSOR_WIDTH, MN_SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        Filter f = new Filter();
        f.maskBits = FilterGroup.VINE | FilterGroup.WALL;
        f.categoryBits = FilterGroup.HAND;
        sensorFixture1 = bodies.get(PART_LEFT_HAND).getBody().createFixture(sensorDef);
        sensorFixture1.setUserData("slothpart sloth left hand");
        sensorFixture2 = bodies.get(PART_RIGHT_HAND).getBody().createFixture(sensorDef);
        sensorFixture2.setUserData("slothpart sloth right hand");
        sensorFixture1.setFilterData(f);
        sensorFixture2.setFilterData(f);
        sensorFixture1.getBody().setBullet(true);
        sensorFixture2.getBody().setBullet(true);
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        bd.position.set(0.0f, -10.0f);
        bd.angularDamping = ROTATION_DAMPING;
        grabPointL = world.createBody(bd);
        grabPointR = world.createBody(bd);
        grabPointL.setTransform(-5f, -5f, 0f);
        grabPointR.setTransform(-5f, -5f, 0f);
        bodies.get(0).setMass(BODY_MASS);
    }

    public float getTorqueForce(float torque, float r, float theta){
        return torque/(r*(float)Math.sin(theta));
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape, Color.RED,bodies.get(PART_LEFT_HAND).getX(),bodies.get(PART_LEFT_HAND).getY(),getAngle(),drawScale.x,drawScale.y);
        canvas.drawPhysics(sensorShape, Color.RED,bodies.get(PART_RIGHT_HAND).getX(),bodies.get(PART_RIGHT_HAND).getY(),getAngle(),drawScale.x,drawScale.y);
    }

    public ObjectSet<Obstacle> badBodies() {
        ObjectSet<Obstacle> badSet = new ObjectSet<Obstacle>();
        for (Obstacle b : bodies) badSet.add(b);
        return badSet;
    }

    @Override
    public void setTextures(MantisAssetManager manager) {
        partTextures = new TextureRegion[BODY_TEXTURE_COUNT];
        Texture managedHand = manager.get("texture/sloth/hand.png");
        Texture managedFrontArm = manager.get("texture/sloth/frontarm.png");
        Texture managedFlowFront = manager.get("texture/sloth/frontflow.png");
        Texture managedBackArm = manager.get("texture/sloth/backarm.png");
        Texture managedFlowLeft = manager.get("texture/sloth/leftflow.png");
        Texture managedFlowFarLeft = manager.get("texture/sloth/farleftflow.png");
        Texture managedFrontArmMoving = manager.get("texture/sloth/frontarm_moving.png");
        Texture managedBackArmMoving = manager.get("texture/sloth/backarm_moving.png");
        Texture managedPowerGlow = manager.get("texture/sloth/power_glow.png");
        partTextures[0] = new TextureRegion(managedHand);
        partTextures[1] = new TextureRegion(managedFrontArm);
        partTextures[3] = new TextureRegion(managedBackArm);
        partTextures[4] = new TextureRegion(managedFlowFront);
        partTextures[2] = new TextureRegion(managedFlowLeft);
        partTextures[5] = new TextureRegion(managedFlowFarLeft);
        partTextures[6] = new TextureRegion(managedFrontArmMoving);
        partTextures[7] = new TextureRegion(managedBackArmMoving);
        partTextures[8] = new TextureRegion(managedPowerGlow);

//        for(int i = 0; i < partTextures.length; i++){
//            partTextures[i].getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        }

        if (bodies.size == 0) {
            init();
        } else {
            for(int ii = 0; ii <= 2; ii++) {
                ((SimpleObstacle)bodies.get(ii)).setTexture(partTextures[partToAsset(ii)]);
            }
        }
    }

    @Override
    public void draw(GameCanvas canvas){
        for(int body_ind = bodies.size-1; body_ind >= 0; body_ind--){
            BoxObstacle part = (BoxObstacle) bodies.get(body_ind);
            TextureRegion texture = part.getTexture();
            if (texture != null) {
                // different textures for flow's body
                if (body_ind == 0) {
                    if (flowFacingState > 3 && flowFacingState < 6){
                        part.setTexture(partTextures[2]);
                        if (!texture.isFlipX()) {
                            texture.flip(true, false);
                        }
                    } else if (flowFacingState > 6) {
                        part.setTexture(partTextures[5]);
                        if (!texture.isFlipX()) {
                            texture.flip(true, false);
                        }
                    } else if (flowFacingState < -3 && flowFacingState > -6) {
                        part.setTexture(partTextures[2]);
                        if (texture.isFlipX()) {
                            texture.flip(true, false);
                        }
                    } else if (flowFacingState < -6) {
                        part.setTexture(partTextures[5]);
                        if (texture.isFlipX()) {
                            texture.flip(true, false);
                        }
                    } else {
                        part.setTexture(partTextures[4]);
                    }
                }

                // different textures for flow's arms if controlling
                if (body_ind == PART_RIGHT_ARM) {
                    if (rightHori >= 0.15 || rightHori <= -0.15 || rightVert >= 0.15 || rightVert <= -0.15)
                        part.setTexture(partTextures[6]);
                    else
                        part.setTexture(partTextures[1]);
                }
                if (body_ind == PART_LEFT_ARM) {
                    if (leftHori >= 0.15 || leftHori <= -0.15 || leftVert >= 0.15 || leftVert <= -0.15)
                        part.setTexture(partTextures[7]);
                    else
                        part.setTexture(partTextures[3]);
                }

                //If the body parts are from the right limb
                if (body_ind == PART_LEFT_HAND || body_ind == PART_RIGHT_HAND) continue;
                if (body_ind == PART_RIGHT_ARM) {
                    drawArm(canvas, part, (leftCanGrabOrIsGrabbing && isActualLeftGrab()) || (!leftCanGrabOrIsGrabbing && !isActualRightGrab()));
                } else if (body_ind == PART_LEFT_ARM) {
                    // left limb
                    drawArm(canvas, part, (leftCanGrabOrIsGrabbing && !isActualLeftGrab()) || (!leftCanGrabOrIsGrabbing && isActualRightGrab()));
                }
                //If the body parts are not limbs
                else {
                    part.draw(canvas, Color.WHITE);
                }
            }
        }
    }

    private void drawArm(GameCanvas canvas, BoxObstacle part, boolean active) {
        if (controlMode == CONTROLS_ONE_ARM) {
            if (active) {
                part.draw(canvas, Color.WHITE);
                // draw power halo
                power = this.power;

                Color tint = new Color(0,0,power,power / 2.5f);
                Vector2 origin = part.getOrigin();
                TextureRegion texture = partTextures[PART_POWER_GLOW];
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                if (texture != null) {
                    float stretch = power * 1.5f;
                    canvas.draw(texture,tint,origin.x,origin.y,part.getX()*drawScale.x,part.getY()*drawScale.y,part.getAngle(),
                            (1.0f/texture.getRegionWidth()) * part.getWidth() * part.getDrawScale().x * part.getObjectScale().x * customScale.x*stretch,
                            (1.0f/texture.getRegionHeight()  * part.getHeight()* part.getDrawScale().y * part.getObjectScale().y * customScale.y*stretch));
                }
                Gdx.gl.glDisable(GL20.GL_BLEND);

            } else {
                part.draw(canvas, Color.BLACK);
            }
        } else {
            part.draw(canvas, Color.WHITE);
        }
    }

    /**
     * Sets the grab for one handed mode
     * @param b
     */
    public void setOneGrab(boolean b) {
        if (controlMode != CONTROLS_ONE_ARM) return;
        if (!didSafeGrab) {
            didOneArmCheck = true;
            grabbedEntity = false;
            if (leftCanGrabOrIsGrabbing) {
                rightGrab = false;
                setLeftGrab(b);
            } else {
                leftGrab = false;
                setRightGrab(b);
            }
        }
    }

    /**
     * For one handed mode: release current hand and grab with other, only if
     * the operation is guaranteed to succeed
     * @param leftButtonPressed
     * @param leftCollisionBody
     * @param rightCollisionBody
     * @param world
     */
    public void setSafeGrab(boolean leftButtonPressed, Body leftCollisionBody, Body rightCollisionBody, World world) {
        if (controlMode != CONTROLS_ONE_ARM) return;
        grabbedEntity = false;
        didSafeGrab = false;
        releasedEntity = false;
        if (waitingForSafeRelease && leftButtonPressed) {
            return;
        }
        waitingForSafeRelease = false;
        if (leftButtonPressed) {
            if (isActualLeftGrab()) {
                if (rightCollisionBody != null) {
                    releaseLeft(world);
                    grab(world, rightCollisionBody, false);
                    didSafeGrab = true;
                    releasedEntity = true;
                    waitingForSafeRelease = true;
                }
            } else {
                if (isActualRightGrab()) {
                    if (leftCollisionBody != null) {
                        releaseRight(world);
                        grab(world, leftCollisionBody, true);
                        didSafeGrab = true;
                        releasedEntity = true;
                        waitingForSafeRelease = true;
                    }
                }
            }
        } else {
            didSafeGrab = false;
        }
    }

    public boolean dismember(World world) {
        if (!dismembered) {
            Joint jointA = joints.get(0);
            Joint jointB = joints.get(1);
            joints.removeValue(jointA,true);
            joints.removeValue(jointB,true);
            world.destroyJoint(jointA);
            world.destroyJoint(jointB);
            jointA = null;
            jointB = null;
            bodies.get(0).setFixedRotation(false);
            bodies.get(0).getBody().applyAngularImpulse(0.5f,true);
            for (Obstacle b : bodies) {
                b.getFilterData().categoryBits = 0;
                b.getBody().applyForceToCenter((float)Math.random()*110 - 55,(float)Math.random()*110 - 55,true);
            }
            dismembered = true;
            return true;
        }
        return false;
    }

    public void pin(World world){
        pin = new WheelObstacle(this.x, this.y, 1);
        pin.setBodyType(BodyDef.BodyType.StaticBody);
        pin.activatePhysics(world);

        RevoluteJointDef jointdef = new RevoluteJointDef();
        Vector2 anchor = new Vector2();
        jointdef.bodyA = pin.getBody();
        jointdef.bodyB = this.getMainBody();
        jointdef.localAnchorA.set(anchor);
        jointdef.localAnchorB.set(anchor);
        Joint joint = world.createJoint(jointdef);
        joints.add(joint);
    }

    public void setPinned() {pinned = true;}

    public void setTutorial() {tutorial = true;}

    public void drawHelpLines(GameCanvas canvas, Affine2 camTrans, int mode, float angleDiff) {
        if (tutorial) {
            Obstacle left = bodies.get(PART_LEFT_HAND);
            Obstacle right = bodies.get(PART_RIGHT_HAND);
            Obstacle body = bodies.get(PART_BODY);

            Vector2 lPos = left.getPosition();
            Vector2 rPos = right.getPosition();
            Vector2 bPos = body.getPosition();
            float mag;

            if(isActualLeftGrab() || isActualRightGrab()) {
                if (isActualLeftGrab()) {
                    if (!isActualRightGrab() || left.getX() < right.getX()) {
                        switch(mode) {
                            case SHIMMY_S:
                                rPos = new Vector2(lPos.x,lPos.y-ARMSPAN);
                                break;
                            case PLUS_30:
                                rPos = (rPos.cpy().sub(bPos)).rotate(30).add(bPos);
                                break;
                            case MINUS_30:
                                rPos = (rPos.cpy().sub(bPos)).rotate(-30).add(bPos);
                                break;
                            default:
                                rPos.sub(bPos).rotate(-angleDiff).add(bPos);
                        }
                    }
                } else {
                    if (!isActualLeftGrab() || right.getX() < left.getX()) {
                        switch(mode) {
                            case SHIMMY_S:
                                lPos = new Vector2(rPos.x,rPos.y-ARMSPAN);
                                break;
                            case PLUS_30:
                                lPos.sub(bPos).rotate(30).add(bPos);
                                break;
                            case MINUS_30:
                                lPos.sub(bPos).rotate(-30).add(bPos);
                                break;
                            default:
                                lPos.sub(bPos).rotate(-angleDiff).add(bPos);
                        }
                    }
                }

//                System.out.println("     left: "+lPos.angle()+" right: "+rPos.angle());
//                System.out.println("lPos ("+lPos.x+","+lPos.y+")  rPos ("+rPos.x+","+rPos.y+")");
                mag = Math.min(lPos.cpy().sub(bPos).len(),rPos.cpy().sub(bPos).len());
                lPos.sub(bPos).setLength(mag).add(bPos);
                rPos.sub(bPos).setLength(mag).add(bPos);
                canvas.beginDebug(camTrans);
                canvas.drawLine(bPos.x * drawScale.x, bPos.y * drawScale.y, lPos.x * drawScale.x, lPos.y * drawScale.y, Color.BLUE, Color.BLUE);
                canvas.drawLine(bPos.x * drawScale.x, bPos.y * drawScale.y, rPos.x * drawScale.x, rPos.y * drawScale.y, Color.RED, Color.RED);
                canvas.endDebug();
            }
        }
    }
}

