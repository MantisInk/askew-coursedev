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
package askew.entity.vine;

import askew.GlobalConfiguration;
import askew.entity.obstacle.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

/**
 * A bridge with planks connected by revolute joints.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Vine extends ComplexObstacle {
	/** The debug name for the entire obstacle */
	private static final String VINE_NAME = "vine";
	/** The debug name for each plank */
	private static final String PLANK_NAME = "barrier";
	/** The debug name for each anchor pin */
	private static final String BRIDGE_PIN_NAME = "pin";
	/** The radius of each anchor pin */
	private static final float BRIDGE_PIN_RADIUS = 0.1f;
    /** The density of each plank in the bridge */
	private transient float BASIC_DENSITY;

    public static final String VINE_TEXTURE = "texture/vine/vine.png";

	// Invisible anchor objects
	/** The left side of the bridge */
	private transient WheelObstacle start = null;
	private transient WheelObstacle finish = null;
	private boolean pin = false;
	/** Set damping constant for joint rotation in vines */
	public static final float DAMPING_ROTATION = 5f;

	// Dimension information
	/** The size of the entire bridge */
	protected transient Vector2 dimension;
	/** The size of a single plank */
	protected transient Vector2 planksize;
	/* The length of each link */
	protected transient float linksize = 1.0f;
	/** The spacing between each link */
	protected transient float spacing = 0.0f;

	protected float numLinks;
	protected float x;
	protected float y;


	/**
	 * Creates a new rope bridge at the given position.
	 *
	 * This bridge is straight horizontal. The coordinates given are the 
	 * position of the leftmost anchor.
	 *
	 * @param x  		The x position of the left anchor
	 * @param y  		The y position of the left anchor
	 * @param length		The length of the bridge
	 * @param lwidth	The plank length
	 * @param lheight	The bridge thickness
	 */
	public Vine(float x, float y, float length, float lwidth, float lheight, Vector2 scale) {
		this(x, y, x, y-length, lwidth*scale.x/32f, lheight*scale.y/32f, false, 5f, -450f);
		numLinks = length;
		this.x = x;
		this.y = y;
	}

	public Vine(float x, float y, float length, float lwidth, float lheight, Vector2 scale, boolean pinned, float angle, float omega) {
		this(x, y, x+length, y, lwidth*scale.x/32f, lheight*scale.y/32f, pinned, angle, omega);
		numLinks = length;
		this.x = x;
		this.y = y;
	}

    /**
     * Creates a new rope bridge with the given anchors.
     *
	 * @param x0  		The x position of the left anchor
	 * @param y0  		The y position of the left anchor
	 * @param x1  		The x position of the right anchor
	 * @param y1  		The y position of the right anchor
	 * @param lwidth	The plank length
	 * @param lheight	The bridge thickness
	 */
	public Vine(float x0, float y0, float x1, float y1, float lwidth, float lheight, boolean pinned, float angle, float omega) {
		super(x0,y0);
		pin = pinned;
		setName(VINE_NAME);

		//System.out.println("x0 "+x0+" y0 "+y0);
		//System.out.println("x1 "+x1+" y1 "+y1);;

		this.BASIC_DENSITY = GlobalConfiguration.getInstance().getAsFloat("vineDensity");

		//System.out.println("lwidth "+lwidth+" lheight "+lheight);
		
		planksize = new Vector2(lwidth,lheight);
		linksize = planksize.y;

		//System.out.println("linksize before "+linksize);
		
	    // Compute the bridge length
		dimension = new Vector2(x1-x0,y1-y0);
	    float length = dimension.len();
	    Vector2 norm = new Vector2(dimension);
	    norm.nor();
	    norm.rotate(angle);

	    //System.out.println("length "+length);
	    
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

	    //System.out.println("links "+nLinks);
	    //System.out.println("linksize "+linksize);
	    	    
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
	        plank.setAngularVelocity(omega*(nLinks-ii-1)/(nLinks));
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

		// Create the leftmost anchor
		// Normally, we would do this in constructor, but we have
		// reasons to not add the anchor to the bodies list.
		Vector2 pos = bodies.get(0).getPosition();
		pos.y -= linksize / 2;
		start = new WheelObstacle(pos.x,pos.y,BRIDGE_PIN_RADIUS);
		start.setName(BRIDGE_PIN_NAME+0);
		start.setDensity(BASIC_DENSITY);
		start.setBodyType(BodyDef.BodyType.StaticBody);
		start.activatePhysics(world);

		// Definition for a revolute joint
		RevoluteJointDef jointDef = new RevoluteJointDef();

		// Initial joint
		jointDef.bodyA = start.getBody();
		jointDef.bodyB = bodies.get(0).getBody();
		jointDef.localAnchorA.set(anchor1);
		jointDef.localAnchorB.set(anchor2);
		jointDef.collideConnected = false;
		Joint joint = world.createJoint(jointDef);
		joints.add(joint);

		// Link the planks together
		anchor1.y = linksize / 2;
		for (int ii = 0; ii < bodies.size-1; ii++) {
			//#region INSERT CODE HERE
			// Look at what we did above and join the planks
			jointDef.bodyA = bodies.get(ii).getBody();
			jointDef.bodyB = bodies.get(ii+1).getBody();
			jointDef.localAnchorA.set(anchor1);
			jointDef.localAnchorB.set(anchor2);
			jointDef.collideConnected = false;
			joint = world.createJoint(jointDef);
			joints.add(joint);
			//#endregion
		}

		if (pin) {
			// Create the rightmost anchor
			Obstacle last = bodies.get(bodies.size-1);

			pos = last.getPosition();
			pos.y += linksize / 2;
			finish = new WheelObstacle(pos.x,pos.y,BRIDGE_PIN_RADIUS);
			finish.setName(BRIDGE_PIN_NAME+1);
			finish.setDensity(BASIC_DENSITY);
			finish.setBodyType(BodyDef.BodyType.StaticBody);
			finish.activatePhysics(world);

			// Final joint
			anchor2.y = 0;
			jointDef.bodyA = last.getBody();
			jointDef.bodyB = finish.getBody();
			jointDef.localAnchorA.set(anchor1);
			jointDef.localAnchorB.set(anchor2);
			joint = world.createJoint(jointDef);
			joints.add(joint);
		}

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
    public void setTextures(AssetManager manager) {
        Texture vineTexture = manager.get(VINE_TEXTURE, Texture.class);
        TextureRegion regionedTexture = new TextureRegion(vineTexture);
        for(Obstacle body : bodies) {
            ((SimpleObstacle)body).setTexture(regionedTexture);
        }
    }
}