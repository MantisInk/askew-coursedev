package askew.entity.tree;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.obstacle.SimpleObstacle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by Lizzy on 4/9/2017.
 * wrapper for combining swinging branch and trunk
 */
public class Tree extends ComplexObstacle {
    /** The debug name for the entire obstacle */
    private static final String VINE_NAME = "tree";

    private Trunk treeTrunk;
    private StiffBranch treeBranch;

    public Tree (float trunk_pos_x, float trunk_pos_y, float trunklen, float branchlen, float dwidth, float dheight, Vector2 scale){
        treeTrunk = new Trunk(trunk_pos_x, trunk_pos_y,trunklen, dwidth, dheight, branchlen, scale);
        treeTrunk.setDrawScale(scale);
        bodies.add(treeTrunk);

        //treeBranch = new StiffBranch(branch_pos.x, branch_pos.y+(treeTrunk.linksize*(trunklen-branchlen)),branchlen,dwidth,dheight, scale);
        treeBranch = new StiffBranch(treeTrunk.final_norm.x, treeTrunk.final_norm.y, branchlen,dwidth,dheight, scale);
        treeBranch.setDrawScale(scale);
        bodies.add(treeBranch);
    }

    public Tree (Trunk trunk, float branchlen, float dwidth, float dheight, Vector2 scale) {
        treeTrunk = trunk;
        bodies.add(treeTrunk);

        treeBranch = new StiffBranch(treeTrunk.final_norm.x, treeTrunk.final_norm.y, branchlen,dwidth,dheight, scale);
        treeBranch.setDrawScale(scale);
        bodies.add(treeBranch);
    }

    public Tree (Trunk trunk, StiffBranch branch) {
        treeTrunk = trunk;
        treeBranch = branch;
        bodies.add(treeTrunk);
        bodies.add(treeBranch);
    }

    @Override
    protected boolean createJoints(World world) {
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
        treeTrunk.deactivatePhysics(world);
        treeBranch.deactivatePhysics(world);
    }

    public void draw(GameCanvas canvas) {
        treeTrunk.draw(canvas);
        treeBranch.draw(canvas);
    }

    @Override
    public void setTextures(MantisAssetManager manager) {
        Texture managedTexture = manager.get("./texture/branch/branch.png", Texture.class);
        TextureRegion regionTexture = new TextureRegion(managedTexture);
        for(Obstacle body : bodies) {
            ((SimpleObstacle)body).setTexture(regionTexture);
        }
    }
}