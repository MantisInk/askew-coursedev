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

import askew.MantisAssetManager;
import askew.entity.obstacle.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

/**
 * A rotating branch with planks connected by weld joints.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class StiffBranch extends ComplexObstacle {

	private static final String BRANCH_NAME = "branch";				/** The debug name for the entire obstacle */
	private static final String PLANK_NAME = "barrier";				/** The debug name for each plank */
	private static final String BRANCH_PIN_NAME = "pin";			/** The debug name for each anchor pin */
	private static final float BRANCH_PIN_RADIUS = 0.1f;			/** The radius of each anchor pin */
	private static final float BASIC_DENSITY = 0.8f;				/** The density of each plank in the bridge */
	private static final float BASIC_MASS = 0.03f;
	private static final float LOWER_LIMIT = 0-(float)Math.PI/3;	/** lower limit of rotation in radians */
	private static final float UPPER_LIMIT = (float)Math.PI/3;		/** upper limit of rotation in radians */

	// Invisible anchor objects
	private transient WheelObstacle start = null;					/** The bottom pin of the branch (rotation pt) */
	public static final float DAMPING_ROTATION = 5f;				/** Set damping constant for joint rotation in vines */

	// Dimension information
	protected transient Vector2 dimension;							/** The size of the entire bridge */
	protected transient Vector2 planksize;							/** The size of a single plank */
	protected transient float linksize = 1.0f;						/* The length of each link */
	protected transient float spacing = 0.0f;						/** The spacing between each link */

	private float stiffLen;											/** The number of planks in the branch */
	private float x, y;												/** The starting coords (bottom) of the branch */

	/**
	 * Creates a new tree branch at the given position.
	 *
	 * This bridge is starts vertical. If it hits anything, it will begin falling.
	 *
	 * @param x  		The x position of the left anchor
	 * @param y  		The y position of the left anchor
	 * @param width		The length of the bridge
	 * @param lwidth	The plank thickness
	 * @param lheight	The plank length
	 */
	public StiffBranch(float x, float y, float width, float lwidth, float lheight, Vector2 scale) {
		this(x, y, x, y+width, lwidth*scale.y/32f, lheight*scale.y/32f, 0f);
		this.stiffLen = width;
		this.x = x;
		this.y = y;
	}

	public StiffBranch(float x, float y, float width, float lwidth, float lheight, Vector2 scale, float angle) {
		this(x, y, x, y+width, lwidth*scale.y/32f, lheight*scale.y/32f, angle);
		this.stiffLen = width;
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new tree branch with the given anchors.
	 *
	 * @param x0  		The x position of the bottom anchor
	 * @param y0  		The y position of the bottom anchor
	 * @param x1  		The x position of the branch end (top)
	 * @param y1  		The y position of the branch end (top)
	 * @param lwidth	The plank thickness
	 * @param lheight	The plank length
	 */
	public StiffBranch(float x0, float y0, float x1, float y1, float lwidth, float lheight, float angle) {
		super(x0,y0);
		setName(BRANCH_NAME);

		planksize = new Vector2(lwidth,lheight);
		linksize = planksize.y;

		// Compute the bridge length
		dimension = new Vector2(x1-x0,y1-y0);
		float length = dimension.len();
		Vector2 norm = new Vector2(dimension);
		norm.nor();
		norm.rotate(angle);

		// If too small, only make one plank.
		int nLinks = (int)(length / linksize);
		if (nLinks <= 1) {
			nLinks = 1;
			linksize = length;
			spacing = 0;
		} else {
			spacing = length - nLinks * linksize;
			spacing /= (nLinks-1);
		}

		// Create the planks
		planksize.y = linksize;
		Vector2 pos = new Vector2();
		for (int ii = 0; ii < nLinks; ii++) {
			float t = ii*(linksize+spacing) + linksize/2.0f;
			pos.set(norm);
			pos.scl(t);
			pos.add(x0,y0);
			BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
			plank.setName(PLANK_NAME+ii);
			plank.setDensity(BASIC_DENSITY);
			plank.setMass(BASIC_MASS);
			bodies.add(plank);
		}
	}

	/**
	 * Creates the joints for this object.
	 *
	 * This method is executed as part of activePhysics. This is the primary method to
	 * override for custom physics objects.
	 *
	 * @param world Box2D world to store joints
	 *
	 * @return true if object allocation succeeded
	 */
	protected boolean createJoints(World world) {
		assert bodies.size > 0;

		Vector2 anchor1 = new Vector2();
		Vector2 anchor2 = new Vector2(0, -linksize / 2);

		// Create the bottom anchor
		// Normally, we would do this in constructor, but we have
		// reasons to not add the anchor to the bodies list.
		Vector2 pos = bodies.get(0).getPosition();
		pos.y -= linksize / 2;
		start = new WheelObstacle(pos.x,pos.y,BRANCH_PIN_RADIUS);
		start.setName(BRANCH_PIN_NAME+0);
		start.setDensity(BASIC_DENSITY);
		start.setBodyType(BodyDef.BodyType.StaticBody);
		start.activatePhysics(world);

		// Definition for a revolute joint
		WeldJointDef jointDef = new WeldJointDef();

		// Initial joint
		// uncomment section to stand up
		// comment section to fall over
//		jointDef.bodyA = start.getBody();
//		jointDef.bodyB = bodies.get(0).getBody();
//		jointDef.localAnchorA.set(anchor1);
//		jointDef.localAnchorB.set(anchor2);
//		jointDef.collideConnected = false;
//		Joint joint = world.createJoint(jointDef );
//		joints.add(joint);

		// uncomment to fall over
		// comment to stand up
		RevoluteJointDef flexJointDef = new RevoluteJointDef();
		flexJointDef.bodyA = start.getBody();
		flexJointDef.bodyB = bodies.get(0).getBody();
		flexJointDef.localAnchorA.set(anchor1);
		flexJointDef.localAnchorB.set(anchor2);
		flexJointDef.collideConnected = false;
		flexJointDef.enableLimit = true;
		flexJointDef.lowerAngle = LOWER_LIMIT;
		flexJointDef.upperAngle = UPPER_LIMIT;
		Joint joint = world.createJoint(flexJointDef);
		joints.add(joint);

		// Link the planks together
		anchor1.y = linksize / 2;
		for (int ii = 0; ii < bodies.size-1; ii++) {
			// join the planks
			jointDef = new WeldJointDef();
			jointDef.bodyA = bodies.get(ii).getBody();
			jointDef.bodyB = bodies.get(ii+1).getBody();
			jointDef.localAnchorA.set(anchor1);
			jointDef.localAnchorB.set(anchor2);
			jointDef.collideConnected = false;
			joint = world.createJoint(jointDef);
			joints.add(joint);
		}

//		RevoluteJointDef flexJointDef = new RevoluteJointDef();
//		flexJointDef.bodyA = bodies.get(bodies.size-2).getBody();
//		flexJointDef.bodyB = bodies.get(bodies.size-1).getBody();
//		flexJointDef.localAnchorA.set(anchor1);
//		flexJointDef.localAnchorB.set(anchor2);
//		flexJointDef.collideConnected = false;
//		joint = world.createJoint(flexJointDef);
//		joints.add(joint);
		return true;
	}

	/**
	 * Destroys the physics Body(s) of this object if applicable,
	 * removing them from the world.
	 *
	 * @param world Box2D world that stores body
	 */
	public void deactivatePhysics(World world) {
		super.deactivatePhysics(world);
		if (start != null) {
			start.deactivatePhysics(world);
		}
	}

	/**
	 * Returns the texture for the individual planks
	 *
	 * @return the texture for the individual planks
	 */
	public TextureRegion getTexture() {
		if (bodies.size == 0) {
			return null;
		}
		return ((SimpleObstacle)bodies.get(0)).getTexture();
	}

	@Override
	public void setTextures(MantisAssetManager manager) {
		Texture managedTexture = manager.get("texture/branch/branch.png", Texture.class);
		TextureRegion regionTexture = new TextureRegion(managedTexture);
		for(Obstacle body : bodies) {
			((SimpleObstacle)body).setTexture(regionTexture);
		}
	}
}