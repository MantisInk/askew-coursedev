package askew.entity.sloth;

/*
 * SlothModel.java
 *
 * This the sloth!
 */

import askew.GameCanvas;
import askew.GlobalConfiguration;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.obstacle.SimpleObstacle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import lombok.Getter;

public class SlothModel extends ComplexObstacle  {

    /** Constants for tuning sloth behaviour */
    private static final float HAND_DENSITY = 10.0f;
    private transient float ARM_DENSITY;
    private static final float HEAD_DENSITY = 1.0f;
    private static final float BODY_DENSITY = 1.25f;
    private transient float TWO_FREE_FORCE_MULTIPLIER;
    private transient float TORQUE;
    private static final boolean BODY_FIXED_ROTATION = true;
    private static final boolean HANDS_FIXED_ROTATION = true;
    private transient float GRAVITY_SCALE;
    private transient boolean SPIDERMAN_MODE;
    private transient boolean GRABBING_HAND_HAS_TORQUE;
    private transient float OMEGA_NORMALIZER;
    private transient boolean TORQUE_BASED_MOVEMENT = false;

    private transient int MOVEMENT_ORIGINAL = 0;
    private transient int MOVEMENT_REVERSE = 1;
    private transient int MOVEMENT_TOGGLE = 2;



    /** Indices for the body parts in the bodies array */
    private static final int PART_NONE = -1;
    private static final int PART_BODY = 0;
    private static final int PART_RIGHT_ARM = 1;
    private static final int PART_LEFT_ARM = 2;
    private static final int PART_LEFT_HAND = 3;
    private static final int PART_RIGHT_HAND = 4;
    private static final int PART_HEAD = 5;

    private static final float PI = (float)Math.PI;

    /** The number of DISTINCT body parts */
    private static final int BODY_TEXTURE_COUNT = 8;

    private transient RevoluteJointDef leftGrabJointDef;
    private transient RevoluteJointDef rightGrabJointDef;
    private transient Joint leftGrabJoint;
    private transient Joint rightGrabJoint;
    private transient PolygonShape sensorShape;
    private transient Fixture sensorFixture1;
    private transient Fixture sensorFixture2;

    /** Set damping constant for rotation of Flow's arms */
    private static final float ROTATION_DAMPING = 5f;

    private  transient Body grabPointR;
    private transient Body grabPointL;

    private transient Vector2 forceL = new Vector2();
    private transient Vector2 forceR = new Vector2();

    private transient CircleShape grabGlow = new CircleShape();



    /** For drawing the force lines*/
    //private Affine2 camTrans = new Affine2();

    public float x;
    public float y;
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
    private transient int movementMode;
    private transient boolean leftGrabbing;
    private transient boolean rightGrabbing;

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

    private static final float BODY_HEIGHT = 1.6f;
    private static final float BODY_WIDTH = 2.0f * (489f / 835f);

    private static final float ARM_WIDTH = 1.75f;
    private static final float ARM_HEIGHT = 0.5f;

    private static final float ARM_XOFFSET    = ARM_WIDTH / 2f + .375f;
    private static final float ARM_YOFFSET    = 0f;

    private static final float HAND_WIDTH = 0.1125f;
    private static final float HAND_HEIGHT = 0.1125f;
    //private static final float HAND_XOFFSET  = (ARM_WIDTH / 2f) - HAND_WIDTH/2;
    private static final float HAND_XOFFSET  = (ARM_WIDTH / 2f) - HAND_WIDTH * 2 - .07f;


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
        this.SPIDERMAN_MODE = GlobalConfiguration.getInstance().getAsBoolean("flowGrabAnything");
        this.TWO_FREE_FORCE_MULTIPLIER = GlobalConfiguration.getInstance().getAsFloat("flowTwoFreeForceMultiplier");
        this.TORQUE = GlobalConfiguration.getInstance().getAsFloat("flowTorque");
        this.GRAVITY_SCALE = GlobalConfiguration.getInstance().getAsFloat("flowGravityScale");
        this.GRABBING_HAND_HAS_TORQUE = GlobalConfiguration.getInstance().getAsBoolean("flowCanMoveGrabbingHand");
        this.OMEGA_NORMALIZER = GlobalConfiguration.getInstance().getAsFloat("flowOmegaNormalizer");
        this.ARM_DENSITY = GlobalConfiguration.getInstance().getAsFloat("flowArmDensity");
        this.TORQUE_BASED_MOVEMENT = GlobalConfiguration.getInstance()
                .getAsBoolean("torqueBasedMovement");
        this.movementMode = GlobalConfiguration.getInstance().getAsInt("flowMovementMode");
        this.rightGrabbing = false;
        this.leftGrabbing =  true;
        //this.shaper = new ShapeRenderer();

    }

    private void init() {
        // We do not do anything yet.
        BoxObstacle part;

        // Body
        part = makePart(PART_BODY, PART_NONE, x, y,BODY_WIDTH,BODY_HEIGHT, BODY_DENSITY,true);
        part.setFixedRotation(BODY_FIXED_ROTATION);
        part.setGravityScale(GRAVITY_SCALE);

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

        if (partTextures != null && bodies.size == 0) {
            init();
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
    private BoxObstacle makePart(int part, int connect, float x, float y, float width, float height, float density, boolean collides) {
        TextureRegion texture = partTextures[partToAsset(part)];

        partCache.set(x,y);
        if (connect != PART_NONE) {
            partCache.add(bodies.get(connect).getPosition());
        }

        //width and height are in box2d units
        float dwidth  = width*objectScale.x;
        float dheight = height*objectScale.x;


        BoxObstacle body;
        if(collides){
            body = new BoxObstacle(partCache.x, partCache.y, dwidth, dheight);
            Filter f = new Filter();
            f.maskBits = FilterGroup.WALL;
            f.categoryBits = FilterGroup.SLOTH;
            body.setFilterData(f);
        }
        else{
            body = new BoxObstacle(partCache.x, partCache.y, dwidth, dheight);
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
//        createJoint(world, PART_LEFT_ARM, PART_RIGHT_ARM, ARM_XOFFSET/2, 0, -ARM_XOFFSET/2, 0);

        // BODY TO ARM WOW
        createJoint(world, PART_BODY, PART_RIGHT_ARM, SHOULDER_XOFFSET/2, SHOULDER_YOFFSET, -ARM_XOFFSET/2, 0);
        createJoint(world, PART_BODY, PART_LEFT_ARM, SHOULDER_XOFFSET/2, SHOULDER_YOFFSET, -ARM_XOFFSET/2, 0);

        // HANDS
        createJoint(world, PART_LEFT_ARM, PART_LEFT_HAND, HAND_XOFFSET, 0, 0, 0);
        createJoint(world, PART_RIGHT_ARM, PART_RIGHT_HAND, HAND_XOFFSET, 0, 0, 0);

        // This is bad but i do sensors here
//        activateSlothPhysics();

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

//    public static Obstacle getLeftArm(){
//        return bodies.get(3);
//    }
//
//    public static Obstacle getRightArm(){
//        return bodies.get(2);
//    }

    //theta is in radians between 0 and pi
    public float calculateTorque(float deltaTheta, float omega){
        //return (float) Math.max(-1.0f,Math.min(1.0f, 1.2 * Math.sin(deltaTheta)));
        return (float)((10.0 / (1 + Math.exp(omega + (deltaTheta *4)))) - 5);//#MAGIC 4, DELTA THETA NORMALIZER
    }

    public float calculateTorqueOld(float deltaTheta){
        return (float) Math.max(-1.0f,Math.min(1.0f, 1.2 * Math.sin(deltaTheta)));
    }

    /**
     * DOES EVERYTHING!!
     */

    public float angleDiff(float goal, float current){
        float diff = (float)(goal - current);
        if(diff > PI){ diff -= (PI + PI);}
        if(diff < -PI){ diff += (PI + PI);}
        return diff;
    }


    public void doThePhysics() {
        if (TORQUE_BASED_MOVEMENT || (leftGrabJoint == null && rightGrabJoint == null)) {
            Obstacle rightHand = bodies.get(PART_RIGHT_HAND);
            Obstacle leftHand = bodies.get(PART_LEFT_HAND);

            Obstacle rightArm = bodies.get(PART_RIGHT_ARM);
            Obstacle leftArm = bodies.get(PART_LEFT_ARM);
            //TODO REDUCE MAGIC NUMBERS ( HENRY )
            // Apply forces
            float dLTheta = 0f;
            float lcTheta = (float)Math.atan2(leftVert,leftHori); // correct
            float lTheta = (-leftArm.getAngle()) + PI;
            lTheta = ((lTheta%(2*PI)) + (2*PI)) % (2*PI) - PI; //ltheta is correct
            float lav = leftArm.getAngularVelocity() * 2;
            float lLength = (float)Math.sqrt((leftVert * leftVert) + (leftHori * leftHori));
            dLTheta = angleDiff(lcTheta,lTheta);

            //antiwobble
            float nextLTheta = -leftArm.getAngle()+PI - leftArm.getAngularVelocity()/20f;
            nextLTheta = ((nextLTheta%(2*PI)) + (2*PI)) % (2*PI) - PI; //ltheta is correct
            float totalRotL = angleDiff(lcTheta,nextLTheta);

            float impulseL = 0;
            if(totalRotL * dLTheta < 0 && lLength > .4f){
                impulseL = ((leftHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + leftArm.getInertia()) * leftArm.getAngularVelocity() * -1 * 60/2;

            }

            if(isActualLeftGrab()){
                impulseL = impulseL * 6;
            }



            float dRTheta = 0f;
            float rcTheta = (float)Math.atan2(rightVert,rightHori);
            float rTheta = -rightArm.getAngle() + PI;
            rTheta = ((rTheta%(2*PI)) + (2*PI)) % (2*PI) - PI;
            float rav = rightArm.getAngularVelocity() * 2;
            float rLength = (float)Math.sqrt((rightVert * rightVert) + (rightHori * rightHori));
            dRTheta = angleDiff(rcTheta,rTheta);

            //antiwobble
            float nextRTheta = -rightArm.getAngle()+PI - rightArm.getAngularVelocity()/20f;
            nextRTheta = ((nextRTheta%(2*PI)) + (2*PI)) % (2*PI) - PI; //ltheta is correct
            float totalRotR = angleDiff(rcTheta, nextRTheta);

            float impulseR = 0;
            if(totalRotR * dRTheta < 0 && rLength > .4f){
                impulseR = ((rightHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + rightArm.getInertia()) * rightArm.getAngularVelocity() * -1 * 60/2;

            }

            if(isActualRightGrab()){
                impulseR = impulseR * 6;
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
            if (isActualLeftGrab()  &&  rLength > .4f) {
                counterfL = counterfactor * calculateTorque(dRcLTheta, lav / OMEGA_NORMALIZER);
                if(dRcLTheta * cwtrL < 0 ){
                    cimpulseL = ((leftHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + leftArm.getInertia()) * leftArm.getAngularVelocity() * -1 * 60 * 3 * (1-lLength) ;
                }
            }
            if (isActualRightGrab()  && lLength > .4f) {
                counterfR = counterfactor * calculateTorque(dLcRTheta, rav / OMEGA_NORMALIZER);
                if(dLcRTheta * cwtrR < 0){
                    cimpulseR = ((rightHand.getMass() * ARM_XOFFSET * ARM_XOFFSET) + rightArm.getInertia()) * rightArm.getAngularVelocity() * -1 * 60 * 3 * (1-rLength) ;
                }
            }









            float forceLeft =  calculateTorque(dLTheta,lav/OMEGA_NORMALIZER); //#MAGIC 20f default, omega normalizer
            
            if(impulseL > 0)
                forceLeft = 0;

            float forceRight = calculateTorque(dRTheta,rav/OMEGA_NORMALIZER);
            if(impulseR > 0)
                forceRight = 0;

            float ltorque = TORQUE * ((forceLeft  * lLength) + TORQUE * (counterfL * rLength  )) + impulseL + cimpulseL;
            float rtorque = TORQUE * ((forceRight * rLength) + TORQUE * ( counterfR * lLength )) + impulseR + cimpulseR;
            forceL.set((float) (ltorque * Math.sin(lTheta)),(float) (ltorque * Math.cos(lTheta)));
            forceR.set((float) (rtorque * Math.sin(rTheta)),(float) (rtorque * Math.cos(rTheta)));

            if ((GRABBING_HAND_HAS_TORQUE || !isActualLeftGrab()) )
                leftArm
                        .getBody()
                        .applyTorque(ltorque,true);
            if ((GRABBING_HAND_HAS_TORQUE || !isActualRightGrab()) )
                rightArm
                        .getBody()
                        .applyTorque(rtorque, true);

            //Draw the lines for the forces

            float left_x = leftHori*TWO_FREE_FORCE_MULTIPLIER;
            float left_y = -leftVert*TWO_FREE_FORCE_MULTIPLIER;

            float right_x = rightHori*TWO_FREE_FORCE_MULTIPLIER;
            float right_y = -rightVert*TWO_FREE_FORCE_MULTIPLIER;
        } else {
            Obstacle rightHand = bodies.get(PART_RIGHT_HAND);
            Obstacle leftHand = bodies.get(PART_LEFT_HAND);

            Obstacle rightArm = bodies.get(PART_RIGHT_ARM);
            Obstacle leftArm = bodies.get(PART_LEFT_ARM);
            //TODO CALCULATE TORQUE
            // Apply forces
            float dLTheta = 0f;
            float dRTheta = 0f;


            float lcTheta = (float)Math.atan2(leftVert,leftHori);
            float lTheta = leftArm.getAngle();
            lTheta = -(lTheta+PI)%(2*PI)-PI;
            float lLength = (float)Math.sqrt((leftVert * leftVert) + (leftHori * leftHori));
            dLTheta = (float)(lTheta - lcTheta);
            dLTheta = (dLTheta+PI)%(2*PI)-PI;

            float rcTheta = (float)Math.atan2(rightVert,rightHori);
            float rTheta = rightArm.getAngle();
            rTheta = -(rTheta+PI)%(2*PI)-PI;
            float rLength = (float)Math.sqrt((rightVert * rightVert) + (rightHori * rightHori));
            dRTheta = (float)(rTheta - rcTheta);
            dRTheta = (dRTheta+PI)%(2*PI)-PI;

            float forceLeft = calculateTorqueOld(-dLTheta);
            float lx = (float) (TORQUE * -Math.sin(lTheta) * forceLeft * lLength);
            float ly = (float) (TORQUE * -Math.cos(lTheta) * forceLeft * lLength);
            forceL.set(lx,ly);

            float forceRight = calculateTorqueOld(-dRTheta);
            float rx = (float) (TORQUE * -Math.sin(rTheta) * forceRight * rLength);
            float ry = (float) (TORQUE * -Math.cos(rTheta) * forceRight * rLength);
            forceR.set(rx,ry);

            if (isActualRightGrab() && !isActualLeftGrab())
                leftHand
                        .getBody()
                        .applyForce(lx, ly, leftHand.getX(), leftHand.getY(), true);
            if (!isActualRightGrab() && isActualLeftGrab())
                rightHand
                        .getBody()
                        .applyForce(rx, ry, rightHand.getX(), rightHand.getY(), true);
        }
//        if (bodies.get(PART_BODY).getBody().getLinearVelocity().x > 0) {
//            flowFacingState++;
//        } else {
//            flowFacingState--;
//        }
//
//        // MAGIC NUMBERS (TREVOR)
//        if (flowFacingState > 25) flowFacingState = 25;
//        if (flowFacingState < -25) flowFacingState = -25;
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
        if (movementMode == MOVEMENT_ORIGINAL)
            this.leftGrab = leftGrab;
        else if (movementMode == MOVEMENT_REVERSE)
            this.leftGrab = !leftGrab;
        else if (movementMode == MOVEMENT_TOGGLE && leftGrab) {
            if (!leftGrabbing) {
                leftGrabbing = true;
            }
            else {
                leftGrabbing = false;
            }
            this.leftGrab = leftGrabbing;
        }
    }

    public void setRightGrab(boolean rightGrab) {
        if (movementMode == MOVEMENT_ORIGINAL)
            this.rightGrab = rightGrab;
        else if (movementMode == MOVEMENT_REVERSE)
            this.rightGrab = !rightGrab;
        else if (movementMode == MOVEMENT_TOGGLE && rightGrab) {
            if (!rightGrabbing) {
                rightGrabbing = true;
            }
            else {
                rightGrabbing = false;
            }
            this.rightGrab = rightGrabbing;
        }
    }

    public void setLeftStickPressed(boolean leftStickPressed) {
        this.leftStickPressed = leftStickPressed;
    }

    public void setRightStickPressed(boolean rightStickPressed) {
        this.rightStickPressed = rightStickPressed;
    }

    public void grab(World world, Body target, boolean leftHand) {
        Joint grabJoint;
        RevoluteJointDef grabJointDef;
        Obstacle hand;

        if (leftHand) {
            grabJoint = leftGrabJoint;
            hand =  bodies.get(PART_LEFT_HAND);

        } else {
            grabJoint = rightGrabJoint;
            hand =  bodies.get(PART_RIGHT_HAND);
        }

        if (grabJoint != null || target == null) return;
        Vector2 anchorHand = new com.badlogic.gdx.math.Vector2(0, 0);
        Vector2 anchorTarget = target.getLocalPoint(hand.getPosition());

        //RevoluteJointDef jointDef = new RevoluteJointDef();
        grabJointDef = new RevoluteJointDef();
        grabJointDef.bodyA = hand.getBody(); // barrier

        if (target == null) {
            return;
        } else {
            grabJointDef.bodyB = target;
        }
        grabJointDef.localAnchorA.set(anchorHand);
        grabJointDef.localAnchorB.set(anchorTarget);
        grabJointDef.collideConnected = false;
        //jointDef.lowerAngle = (float) (- Math.PI/4);
        //jointDef.upperAngle = (float) (Math.PI/4);
        //jointDef.enableLimit = true;
        grabJoint = world.createJoint(grabJointDef);
        if (leftHand) {
            leftGrabJoint = grabJoint;
        } else {
            rightGrabJoint = grabJoint;
        }

        joints.add(grabJoint);
    }

    public void releaseLeft(World world) {
        if (leftGrabJoint != null) {
            if (movementMode != MOVEMENT_TOGGLE || (movementMode == MOVEMENT_TOGGLE && !leftGrabbing))
                world.destroyJoint(leftGrabJoint);
        }
        leftGrabJoint = null;
    }


    public void releaseRight(World world) {
        if (rightGrabJoint != null) {
            if (movementMode != MOVEMENT_TOGGLE || (movementMode == MOVEMENT_TOGGLE && !rightGrabbing))
                world.destroyJoint(rightGrabJoint);
        }
        rightGrabJoint = null;
    }

    public void activateSlothPhysics(World world) {
        float MN_SENSOR_HEIGHT = HAND_HEIGHT/2f;
        float MN_SENSOR_WIDTH = HAND_WIDTH/2f;
        //float MN_SHRINK = 0.6f;
        Vector2 sensorCenter = new Vector2(0, 0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0.0f;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(MN_SENSOR_WIDTH, MN_SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        Filter f = new Filter();
        f.maskBits = FilterGroup.VINE;
        f.categoryBits = FilterGroup.HAND;
        sensorFixture1 = bodies.get(PART_LEFT_HAND).getBody().createFixture(sensorDef);
        sensorFixture1.setUserData("sloth left hand");
        sensorFixture2 = bodies.get(PART_RIGHT_HAND).getBody().createFixture(sensorDef);
        sensorFixture2.setUserData("sloth right hand");
        sensorFixture1.setFilterData(f);
        sensorFixture2.setFilterData(f);
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        bd.position.set(0.0f, -10.0f);
        bd.angularDamping = ROTATION_DAMPING;
        grabPointL = world.createBody(bd);
        grabPointR = world.createBody(bd);
        grabPointL.setTransform(-5f, -5f, 0f);
        grabPointR.setTransform(-5f, -5f, 0f);

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
        partTextures[0] = new TextureRegion(managedHand);
        partTextures[1] = new TextureRegion(managedFrontArm);
        partTextures[3] = new TextureRegion(managedBackArm);
        partTextures[4] = new TextureRegion(managedFlowFront);
        partTextures[2] = new TextureRegion(managedFlowLeft);
        partTextures[5] = new TextureRegion(managedFlowFarLeft);
        partTextures[6] = new TextureRegion(managedFrontArmMoving);
        partTextures[7] = new TextureRegion(managedBackArmMoving);

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
                if (body_ind == 1) {
                    if (rightHori >= 0.15 || rightHori <= -0.15 || rightVert >= 0.15 || rightVert <= -0.15)
                        part.setTexture(partTextures[6]);
                    else
                        part.setTexture(partTextures[1]);
                }
                if (body_ind == 2) {
                    if (leftHori >= 0.15 || leftHori <= -0.15 || leftVert >= 0.15 || leftVert <= -0.15)
                        part.setTexture(partTextures[7]);
                    else
                        part.setTexture(partTextures[3]);
                }

                //If the body parts are from the right limb
                if (body_ind == 3 || body_ind == 4) continue;
                if (body_ind == 1 || body_ind == 4) {
                    part.draw(canvas, Color.WHITE);
                    part.draw(canvas, Color.WHITE);     //remove this line when you draw the head
                }
                //If the body parts are from the left limb
                else if (body_ind == 2 || body_ind == 3) {
                    part.draw(canvas, Color.WHITE);
                }
                //If the body parts are not limbs
                else {
                    part.draw(canvas, Color.WHITE);
                }
            }
        }



        //Commented out because the vine images disappear when this is used here?
        //drawForces();

    }
}

