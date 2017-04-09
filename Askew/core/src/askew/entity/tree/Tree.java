package askew.entity.tree;

import askew.entity.obstacle.ComplexObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.obstacle.SimpleObstacle;
import com.badlogic.gdx.assets.AssetManager;
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
    /** The density of each plank in the bridge */
    private transient float BASIC_DENSITY;

    Trunk treeTrunk;
    StiffBranch treeBranch;

    protected float numLinks;
    protected float x;
    protected float y;

    public Tree (TextureRegion branchTexture, Vector2 trunk_pos, float trunklen, float branchlen, Vector2 branch_pos, Vector2 scale){
        float dwidth  = branchTexture.getRegionWidth()/scale.x;
        float dheight = branchTexture.getRegionHeight()/scale.y;
        treeTrunk = new Trunk(trunk_pos.x, trunk_pos.y,trunklen, dwidth, dheight, branchlen, scale);
        treeTrunk.setDrawScale(scale);
        bodies.add(treeTrunk);

        treeBranch = new StiffBranch(branch_pos.x, branch_pos.y+(treeTrunk.linksize*(trunklen-branchlen)),branchlen,dwidth,dheight, scale);
        treeBranch.setDrawScale(scale);
        bodies.add(treeBranch);
    }

    @Override
    protected boolean createJoints(World world) {
        return true;
    }

    public void setTextures(AssetManager manager) {
        Texture managedTexture = manager.get("./texture/branch/branch.png", Texture.class);
        TextureRegion regionTexture = new TextureRegion(managedTexture);
        for(Obstacle body : bodies) {
            ((SimpleObstacle)body).setTexture(regionTexture);
        }
    }
}