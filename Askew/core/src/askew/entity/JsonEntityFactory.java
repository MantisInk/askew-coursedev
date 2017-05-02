package askew.entity;

import askew.MantisAssetManager;
import askew.entity.ghost.GhostModel;
import askew.entity.owl.OwlModel;
import askew.entity.sloth.SlothModel;
import askew.entity.thorn.ThornModel;
import askew.entity.tree.PoleVault;
import askew.entity.tree.StiffBranch;
import askew.entity.tree.Trunk;
import askew.entity.vine.Vine;
import askew.entity.wall.WallModel;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Since we cannot create a static method on Entity to create an instance of a particular entity from a JSON instance,
 * we use the factory pattern to create such entities
 */
public class JsonEntityFactory {

    public static Vine createVine(MantisAssetManager manager, JsonObject instance) {
        Vine vine;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float numlinks = instance.get("numLinks").getAsFloat();
        float angle = instance.get("angle").getAsFloat();
        float omega = instance.get("omega").getAsFloat();
        Texture managedTexture = manager.get(Vine.VINE_TEXTURE, Texture.class);
        //TextureRegion vineTexture = new TextureRegion(managedTexture);
		vine = new Vine(x, y, numlinks, angle, omega);
		vine.setTextures(manager);
        return vine;
    }

    public static Trunk createTrunk(MantisAssetManager manager, JsonObject instance) {
        Trunk trunk;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float angle = instance.get("angle").getAsFloat();
        float numlinks = instance.get("numLinks").getAsFloat();
        trunk = new Trunk(x, y, numlinks, angle);
        trunk.setTextures(manager);
        return trunk;
    }

    public static PoleVault createPoleVault(MantisAssetManager manager, JsonObject instance) {
        PoleVault poleVault;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float angle = instance.get("angle").getAsFloat();
        float numlinks = instance.get("numLinks").getAsFloat();
        float linksize = instance.get("linksize").getAsFloat();
        poleVault = new PoleVault(x, y, numlinks, 0.25f, linksize, new Vector2(1,1), angle);
        poleVault.setTextures(manager);
        return poleVault;
    }

    public static SlothModel createSloth(MantisAssetManager manager, JsonObject instance) {
        SlothModel ret;
        ret = new SlothModel(instance.get("x").getAsFloat(), instance.get("y").getAsFloat());
        ret.setTextures(manager);
        return ret;
    }

    public static StiffBranch createStiffBranch(MantisAssetManager manager, JsonObject instance) {
        StiffBranch branch;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float stiff = instance.get("stiffLen").getAsFloat();
        float angle = instance.get("angle").getAsFloat();
        branch = new StiffBranch(x, y, stiff, 0.25f, 1f,new Vector2(1,1), angle);
        branch.setTextures(manager);
        return branch;
    }

    public static OwlModel createOwl(MantisAssetManager manager, JsonObject instance) {
        OwlModel owl;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        owl = new OwlModel(x, y);
        owl.setTextures(manager);
        return owl;
    }

    public static WallModel createWall(MantisAssetManager manager, JsonObject instance) {
        WallModel wall;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        List<Float> points = new ArrayList<>();
        System.out.println(instance.get("points"));
        instance.get("points").getAsJsonArray().forEach(pt->points.add(pt.getAsFloat()));
        Float[] arrayPoints = points.toArray(new Float[points.size()]);
        float[] copy = new float[arrayPoints.length];
        for (int i = 0; i < arrayPoints.length; i++) {
            copy[i] = arrayPoints[i];
        }
        wall = new WallModel(x, y, copy);
        wall.setTextures(manager);
        return wall;
    }


    public static ThornModel createThorn(MantisAssetManager manager, JsonObject instance) {
        ThornModel thorn;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float width = instance.get("width").getAsFloat();
        float height = instance.get("height").getAsFloat();
        float angle = instance.get("angle").getAsFloat();
        thorn = new ThornModel(x, y, width, height, angle);
        thorn.setTextures(manager);
        return thorn;
    }

    public static GhostModel createGhost(MantisAssetManager manager, JsonObject instance) {
        GhostModel ghost;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float patroldx = instance.get("patroldx").getAsFloat();
        float patroldy = instance.get("patroldy").getAsFloat();
        ghost = new GhostModel(x, y, patroldx, patroldy);
        ghost.setTextures(manager);
        return ghost;
    }

    public static BackgroundEntity createBGEntity(MantisAssetManager manager, JsonObject instance) {
        BackgroundEntity bge;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float width = instance.get("width").getAsFloat();
        float height = instance.get("height").getAsFloat();
        float depth = instance.get("depth").getAsFloat();
        float angle = instance.get("angle").getAsFloat();
        float scalex = instance.get("scalex").getAsFloat();
        float scaley = instance.get("scaley").getAsFloat();
        String colorString;
        try {colorString = instance.get("color").getAsString();}
        catch(NullPointerException e){
            colorString = "0xffffffff";
        }
        int intColor;
        try {
            long color = instance.get("color").getAsLong();
            intColor = (int) color;
        } catch (Exception e) {
            try {
                long color = Long.decode(colorString);
                intColor = (int) color;
            } catch (Exception ee) {
                long color = Long.valueOf(colorString, 16);
                intColor = (int) color;
            }
        }
        String tex = instance.get("texturePath").getAsString();

        bge = new BackgroundEntity(x,y,width,height,depth,angle,scalex,scaley,tex,intColor);
        bge.setTextures(manager);
        return bge;
    }
}
