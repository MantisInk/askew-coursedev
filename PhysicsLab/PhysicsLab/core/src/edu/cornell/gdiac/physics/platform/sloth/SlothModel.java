package edu.cornell.gdiac.physics.platform.sloth;

/*
 * SlothModel.java
 *
 * This the sloth!
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.physics.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class SlothModel extends ComplexObstacle {

    /** Constants for tuning sloth behaviour */
    private static final float HAND_DENSITY = 4.0f;
    private static final float ARM_DENSITY = 0.1f;
    private static final float HEAD_DENSITY = 1.0f;
    private static final float BODY_DENSITY = 0.1f;
    private static final float TWO_FREE_FORCE_MULTIPLIER = 5.0f;
    private static final float TORQUE = 6.0f;
    private static final boolean BODY_FIXED_ROTATION = true;
    private static final boolean HANDS_FIXED_ROTATION = true;
    private static final float GRAVITY_SCALE = 0.7f;
    private static final float ARM_MASS = 5.0f;
    public boolean SPIDERMAN_MODE = false;

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
    private static final int BODY_TEXTURE_COUNT = 6;

    private RevoluteJointDef leftGrabJointDef;
    private RevoluteJointDef rightGrabJointDef;
    private Joint leftGrabJoint;
    private Joint rightGrabJoint;
    private PolygonShape sensorShape;
    private Fixture sensorFixture1;
    private Fixture sensorFixture2;

    private Body grabPointR;
    private Body grabPointL;

    private Vector2 forceL = new Vector2();
    private Vector2 forceR = new Vector2();



    /** For drawing the force lines*/
    private ShapeRenderer shaper = new ShapeRenderer();

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
            case PART_HEAD:
                return 0;
            case PART_LEFT_ARM:
            case PART_RIGHT_ARM:
                return 2;
            case PART_BODY:
                return 3;
            default:
                return -1;
        }
    }

    // Layout of ragdoll
    //  |0|______0______|0| LOL WTF
    //
    /** Dist between arms? */

    private static final float SHOULDER_XOFFSET    = 0.00f;
    private static final float SHOULDER_YOFFSET    = 0.00f;


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

        // Body
        part = makePart(PART_BODY, PART_NONE, x, y, BODY_DENSITY,true);
        part.setFixedRotation(BODY_FIXED_ROTATION);
        part.setGravityScale(GRAVITY_SCALE);

        // ARMS
        // Right arm
        part = makePart(PART_RIGHT_ARM, PART_BODY, SHOULDER_XOFFSET + ARM_XOFFSET, SHOULDER_YOFFSET + ARM_YOFFSET, ARM_DENSITY,false);
        part.setGravityScale(GRAVITY_SCALE);
        //part.setMass(ARM_MASS);

        // Left arm
        part = makePart(PART_LEFT_ARM, PART_BODY, -ARM_XOFFSET, -ARM_YOFFSET, ARM_DENSITY,false);
        part.setAngle((float)Math.PI);
        part.setGravityScale(GRAVITY_SCALE);
        //part.setMass(ARM_MASS);

        // HANDS
        // Left hand
        part = makePart(PART_LEFT_HAND, PART_LEFT_ARM, ARM_XOFFSET, ARM_YOFFSET, HAND_DENSITY,false);
        part.setFixedRotation(HANDS_FIXED_ROTATION);
        part.setGravityScale(GRAVITY_SCALE);
        // Right hand
        part = makePart(PART_RIGHT_HAND, PART_RIGHT_ARM, ARM_XOFFSET, ARM_YOFFSET, HAND_DENSITY,false);
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
    private BoxObstacle makePart(int part, int connect, float x, float y, float density, boolean collides) {
        TextureRegion texture = partTextures[partToAsset(part)];

        partCache.set(x,y);
        if (connect != PART_NONE) {
            partCache.add(bodies.get(connect).getPosition());
        }

        float dwidth  = texture.getRegionWidth()/drawScale.x;
        float dheight = texture.getRegionHeight()/drawScale.y;

        BoxObstacle body;
        if(!collides){
            body = new TrevorObstacle(partCache.x, partCache.y, dwidth, dheight);
        }
        else{
            body = new BoxObstacle(partCache.x, partCache.y, dwidth, dheight);
        }
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
//        createJoint(world, PART_LEFT_ARM, PART_RIGHT_ARM, ARM_XOFFSET/2, 0, -ARM_XOFFSET/2, 0);

        // BODY TO ARM WOW
        createJoint(world, PART_BODY, PART_RIGHT_ARM, SHOULDER_XOFFSET/2, 0, -ARM_XOFFSET/2, 0);
        createJoint(world, PART_BODY, PART_LEFT_ARM, -SHOULDER_XOFFSET/2, 0, ARM_XOFFSET/2, 0);

        // HANDS
        createJoint(world, PART_LEFT_ARM, PART_LEFT_HAND, -HAND_XOFFSET, 0, 0, 0);
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
    public float calculateTorque(float deltaTheta){
        return (float) Math.max(-1.0f,Math.min(1.0f, 1.2 * Math.sin(deltaTheta)));
    }

    /**
     * DOES EVERYTHING!!
     */


    public void doThePhysics() {
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

        float forceLeft = calculateTorque(-dLTheta);
        float lx = (float) (TORQUE * -Math.sin(lTheta) * forceLeft * lLength);
        float ly = (float) (TORQUE * -Math.cos(lTheta) * forceLeft * lLength);
        forceL.set(lx,ly);

        float forceRight = calculateTorque(-dRTheta);
        float rx = (float) (TORQUE * -Math.sin(rTheta) * forceRight * rLength);
        float ry = (float) (TORQUE * -Math.cos(rTheta) * forceRight * rLength);
        forceR.set(rx,ry);

        if (isRightGrab() && !isLeftGrab())
        leftHand
                .getBody()
                .applyForce(lx, ly, leftHand.getX(), leftHand.getY(), true);
        if (!isRightGrab() && isLeftGrab())
        rightHand
                .getBody()
                .applyForce(rx, ry, rightHand.getX(), rightHand.getY(), true);

        //Draw the lines for the forces

        float left_x = leftHori*TWO_FREE_FORCE_MULTIPLIER;
        float left_y = -leftVert*TWO_FREE_FORCE_MULTIPLIER;

        float right_x = rightHori*TWO_FREE_FORCE_MULTIPLIER;
        float right_y = -rightVert*TWO_FREE_FORCE_MULTIPLIER;

        OrthographicCamera camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.setToOrtho(false);

        //ShapeRenderer shaper = new ShapeRenderer();
        Gdx.gl.glLineWidth(3);
        shaper.setProjectionMatrix(camera.combined);

        shaper.begin(ShapeRenderer.ShapeType.Line);
        shaper.setColor(Color.BLACK);
        //System.out.println(shaper.isDrawing());
        //System.out.println(left_x+", "+left_y);
        //System.out.println(right_x+", "+right_y);
        shaper.line(leftHand.getX(),leftHand.getY(), leftHand.getX()+(left_x*200),leftHand.getY()+(left_y*200));
        shaper.line(rightHand.getX(),rightHand.getY(), rightHand.getX()+(right_x*200),rightHand.getY()+(right_y*200));
        shaper.end();
        Gdx.gl.glLineWidth(3);
    }

    public void drawForces(){
        Obstacle right = bodies.get(PART_RIGHT_HAND);
        Obstacle left = bodies.get(PART_LEFT_HAND);

        //Draw the lines for the forces

        OrthographicCamera camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.setToOrtho(false);

        //ShapeRenderer shaper = new ShapeRenderer();
        Gdx.gl.glLineWidth(3);
        shaper.setProjectionMatrix(camera.combined);

        shaper.begin(ShapeRenderer.ShapeType.Line);
        shaper.setColor(Color.BLUE);
        //System.out.println(shaper.isDrawing());
        //System.out.println(left_x+", "+left_y);
        //System.out.println(right_x+", "+right_y);
        //System.out.println(left.getX()+", "+left.getY());
        //System.out.println(right.getX()+", "+right.getY());
        //System.out.println(this.x+", "+this.y);
        //shaper.line(this.x+left.getX(),this.y+left.getY(), left.getX()+(left_x*20),left.getY()+(left_y*20));
        shaper.line(left.getX()*drawScale.x,left.getY() * drawScale.y, left.getX()*drawScale.x+(forceL.x*20),left.getY() * drawScale.y+(forceL.y*20));
        shaper.setColor(Color.RED);
        //shaper.line(this.x+right.getX(),this.y+right.getY(), right.getX()+(right_x*20),right.getY()+(right_y*20));
        shaper.line(right.getX()*drawScale.x,right.getY() * drawScale.y, right.getX()*drawScale.x+(forceR.x*20),right.getY() * drawScale.y+(forceR.y*20));
        shaper.end();
        Gdx.gl.glLineWidth(3);

    }

    public void setLeftGrab(boolean leftGrab) {
        this.leftGrab = leftGrab;
    }

    public void setRightGrab(boolean rightGrab) {
        this.rightGrab = rightGrab;
    }

    public void grabLeft(World world, Body target) {
        if (leftGrabJoint != null) return;
        Vector2 anchorHand = new com.badlogic.gdx.math.Vector2(0, 0);
        // TODO: Improve this vector
        Vector2 anchorTarget = new com.badlogic.gdx.math.Vector2(0, 0);
        if (SPIDERMAN_MODE)
            grabPointL.setTransform(bodies.get(PART_LEFT_HAND).getPosition(), 0);
        //RevoluteJointDef jointDef = new RevoluteJointDef();
        leftGrabJointDef = new RevoluteJointDef();
        leftGrabJointDef.bodyA = bodies.get(PART_LEFT_HAND).getBody(); // barrier

        if (target == null) {
            if (SPIDERMAN_MODE) {
                leftGrabJointDef.bodyB = grabPointL; // pin
            } else{
                return;
            }
        }
        else
            leftGrabJointDef.bodyB = target;
        leftGrabJointDef.localAnchorA.set(anchorHand);
        leftGrabJointDef.localAnchorB.set(anchorTarget);
        leftGrabJointDef.collideConnected = false;
        //jointDef.lowerAngle = (float) (- Math.PI/4);
        //jointDef.upperAngle = (float) (Math.PI/4);
        //jointDef.enableLimit = true;
        leftGrabJoint = world.createJoint(leftGrabJointDef);
        joints.add(leftGrabJoint);
    }

    public void releaseLeft(World world) {
        if (leftGrabJoint != null) {
            System.err.println("Release");
            world.destroyJoint(leftGrabJoint);
            //joints.removeValue(leftGrabJoint,true);
        }
        leftGrabJoint = null;
    }

    public void grabRight(World world, Body target) {
        if (rightGrabJoint != null) return;
        System.err.println("Grab");
        Vector2 anchorHand = new com.badlogic.gdx.math.Vector2(0, 0);
        // TODO: Improve this vector
        Vector2 anchorTarget = new com.badlogic.gdx.math.Vector2(0, 0);
        if (SPIDERMAN_MODE)
                grabPointR.setTransform(bodies.get(PART_RIGHT_HAND).getPosition(), 0);
        //RevoluteJointDef jointDef = new RevoluteJointDef();
        rightGrabJointDef = new RevoluteJointDef();
        rightGrabJointDef.bodyA = bodies.get(PART_RIGHT_HAND).getBody(); // barrier
        if (target == null) {
            if (SPIDERMAN_MODE) {
                rightGrabJointDef.bodyB = grabPointR; // pin
            } else{
                return;
            }
        }
        else
            rightGrabJointDef.bodyB = target;
        rightGrabJointDef.localAnchorA.set(anchorHand);
        rightGrabJointDef.localAnchorB.set(anchorTarget);
        rightGrabJointDef.collideConnected = false;
        //jointDef.lowerAngle = (float) (- Math.PI/4);
        //jointDef.upperAngle = (float) (Math.PI/4);
        //jointDef.enableLimit = true;
        rightGrabJoint = world.createJoint(rightGrabJointDef);
        joints.add(rightGrabJoint);
    }

    public void releaseRight(World world) {
        if (rightGrabJoint != null) {
            System.err.println("Release");
            world.destroyJoint(rightGrabJoint);
            //joints.removeValue(rightGrabJoint,true);
        }
        rightGrabJoint = null;
    }

    public void activateSlothPhysics(World world) {
        float MN_HEIGHT = 5.0f;
        float MN_SENSOR_HEIGHT = .4f;
        float MN_WIDTH = .4f;
        //float MN_SHRINK = 0.6f;
        Vector2 sensorCenter = new Vector2(0, 0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0.0f;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(MN_WIDTH, MN_SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture1 = bodies.get(PART_LEFT_HAND).getBody().createFixture(sensorDef);
        sensorFixture1.setUserData("sloth left hand");
        sensorFixture2 = bodies.get(PART_RIGHT_HAND).getBody().createFixture(sensorDef);
        sensorFixture2.setUserData("sloth right hand");
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        bd.position.set(0.0f, -10.0f);
        bd.angularDamping = 5f;
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
        ObjectSet<Obstacle> wtfSet = new ObjectSet<Obstacle>();
        for (Obstacle b : bodies) wtfSet.add(b);
        return wtfSet;
    }

}

