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
import askew.entity.FilterGroup;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;

import askew.entity.obstacle.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A tree trunk with planks connected by weld joints.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Trunk extends ComplexObstacle {

	private transient int nLinks;
	private transient float width,height;
	private static final String TRUNK_NAME = "trunk";			/** The debug name for the entire obstacle */
	private static final String PLANK_NAME = "driftwood";		/** The debug name for each plank */
	private static final String TRUNK_PIN_NAME = "pin";			/** The debug name for each anchor pin */
	private static final float TRUNK_PIN_RADIUS = 0.1f;			/** The radius of each anchor pin */
	private static final float BASIC_DENSITY = 13f;				/** The density of each plank in the bridge */

	private static final float lwidth = .25f;
	private static final float lheight = 1.0f;

	/** The spacing between each link */
	protected transient Vector2 dimension;						/** The size of the entire bridge */

	@Getter @Setter
	private float x;
	@Getter @Setter
	private float y;
	@Getter @Setter
	private float angle;
	@Getter @Setter
	private float numLinks;									// param for json constructor


	public transient Vector2 final_norm = null;					/** coords for starting branch off this trunk */
	public transient static final float DAMPING_ROTATION = 5f;	/** Set damping constant for joint rotation in vines */
	protected transient Vector2 planksize;						/** The size of a single plank */
	// TODO: Fix this from being public (refactor artifact) ?
	private transient float spacing = 0.0f;						/** The spacing between each link */




	public Trunk(float x, float y, float length, float angle) {
		setName(TRUNK_NAME);
		numLinks = length;
		this.x = x;
		this.y = y;
		this.angle = angle;
		setPosition(x,y);
		build();
	}

	public void build(){
		planksize = new Vector2(lwidth,lheight);

		// Compute the bridge length
		dimension = new Vector2(0,numLinks);
		float length = dimension.len();
		Vector2 norm = new Vector2(dimension);
		norm.nor();
		norm.rotate(angle);

		// If too small, only make one plank.
		int nLinks = (int)(length / lheight);
		if (nLinks <= 1) {
			nLinks = 1;
//			lheight = length;
			spacing = 0;
		} else {
			spacing = length - nLinks * lheight;
			spacing /= (nLinks-1);
		}

		// Create the planks
		planksize.y = lheight;
		Vector2 pos = new Vector2();
		for (int ii = 0; ii < nLinks; ii++) {
			float t = ii*(lheight+spacing) + lheight/2.0f;
			pos.set(norm);
			pos.scl(t);
			pos.add(x,y);
			BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
			plank.setName(PLANK_NAME+ii);
			plank.setDensity(BASIC_DENSITY);
			plank.setAngle((float)Math.toRadians(angle));
			plank.setBodyType(BodyDef.BodyType.StaticBody);
			plank.setDrawScale(drawScale);
			Filter f = new Filter();
			f.maskBits = FilterGroup.WALL | FilterGroup.SLOTH | FilterGroup.HAND;
			f.categoryBits = FilterGroup.VINE;
			plank.setFilterData(f);
			bodies.add(plank);
		}
		final_norm = new Vector2(pos);
		final_norm.add(0,lheight/2);


	}
	public void rebuild(){
		bodies.clear();
		build();
	}

	public void setPosition(float x, float y){
		super.setPosition(x,y);
		this.x = x;
		this.y = y;
		rebuild();
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

		Vector2 pos = bodies.get(0).getPosition();

		// Definition for a revolute joint
		WeldJointDef jointDef = new WeldJointDef();

		Joint joint;

		Vector2 anchor1 = new Vector2(0, lheight/2);
		Vector2 anchor2 = new Vector2(0, -lheight / 2);

		// Link the planks together
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
		return ((BoxObstacle) bodies.get(0)).getTexture();
	}

	@Override
	public void setTextures(MantisAssetManager manager) {
		Texture managedTexture = manager.get("texture/branch/branch.png", Texture.class);
		TextureRegion regionTexture = new TextureRegion(managedTexture);
		for(Obstacle body : bodies) {
			((BoxObstacle)body).setTexture(regionTexture);
		}
	}
}