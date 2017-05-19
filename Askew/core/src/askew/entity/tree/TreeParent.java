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
import askew.entity.Entity;
import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

/**
 * A tree trunk with planks connected by weld joints.
 * <p>
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public abstract class TreeParent extends ComplexObstacle {

    /**
     * coords for starting branch off this trunk
     */
    public transient static final float DAMPING_ROTATION = 5f;
    static final String TRUNK_NAME = "trunk";
    /**
     * The debug name for the entire obstacle
     */
    static final String PLANK_NAME = "driftwood";
    /**
     * The debug name for each plank
     */
    static final float BASIC_DENSITY = 13f;


    static final float PLANK_WIDTH = .10f;
    static final float PLANK_HEIGHT = 1.0f;


//	@Getter @Setter
//	private transient float x;
//	@Getter @Setter
//	private transient float y;
//	@Getter @Setter
//	private transient float angle;
//	@Getter @Setter
//	private transient float numLinks;									// param for json constructor


    public transient Vector2 final_norm = null;
    /**
     * The spacing between each link
     */
    protected transient Vector2 dimension;
    /**
     * Set damping constant for joint rotation in vines
     */
    protected transient Vector2 planksize;
    /**
     * The size of a single plank
     */
    // TODO: Fix this from being public (refactor artifact) ?
    private transient float spacing = 0.0f;

    /**
     * The spacing between each link
     */


    TreeParent(float x, float y) {

        this.setDrawNumber(Entity.DN_STICK);
//		setName(TRUNK_NAME);
//		numLinks = length;
//		this.x = x;
//		this.y = y;
//		this.angle = angle;
//		setPosition(x,y);
//		build();
    }

    public abstract void build();

    public void rebuild() {
        bodies.clear();
        build();
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
    protected abstract boolean createJoints(World world);

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
        for (Obstacle body : bodies) {
            ((BoxObstacle) body).setTexture(regionTexture);
        }
    }
}