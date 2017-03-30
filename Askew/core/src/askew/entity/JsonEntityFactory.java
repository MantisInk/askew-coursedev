package askew.entity;

import askew.entity.stiffbranch.StiffBranch;
import askew.entity.trunk.Trunk;
import askew.entity.sloth.SlothModel;
import askew.entity.vine.Vine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonObject;

/**
 * Since we cannot create a static method on Entity to create an instance of a particular entity from a JSON instance,
 * we use the factory pattern to create such entities
 */
public class JsonEntityFactory {

    public static Vine createVine(AssetManager manager, JsonObject instance, Vector2 scale) {
        Vine vine;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float numlinks = instance.get("numLinks").getAsFloat();
        Texture managedTexture = manager.get(Vine.VINE_TEXTURE, Texture.class);
        TextureRegion vineTexture = new TextureRegion(managedTexture);
		vine = new Vine(x, y, numlinks, vineTexture.getRegionHeight() / scale.x, vineTexture.getRegionHeight() / scale.y, scale);
		vine.setDrawScale(scale.x, scale.y);
		vine.setTextures(manager);
        return vine;
    }

    public static Trunk createTrunk(AssetManager manager, JsonObject instance, Vector2 scale) {
        Trunk trunk;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float numlinks = instance.get("numLinks").getAsFloat();
        float stiff = instance.get("stiffLen").getAsFloat();
        trunk = new Trunk(x, y, numlinks, 0.25f, 1.0f, stiff,scale);
        trunk.setDrawScale(scale.x, scale.y);
        trunk.setTextures(manager);
        return trunk;
    }

    public static SlothModel createSloth(AssetManager manager, JsonObject instance, Vector2 scale) {
        SlothModel ret;
        ret = new SlothModel(instance.get("x").getAsFloat(), instance.get("y").getAsFloat());
        ret.setDrawScale(scale.x, scale.y);
        ret.setTextures(manager);
        return ret;
    }

    public static StiffBranch createStiffBranch(AssetManager manager, JsonObject instance, Vector2 scale) {
        StiffBranch branch;
        float x = instance.get("x").getAsFloat();
        float y = instance.get("y").getAsFloat();
        float stiff = instance.get("stiffLen").getAsFloat();
        branch = new StiffBranch(x, y, stiff, 0.25f, 0.1f,scale);
        branch.setDrawScale(scale.x, scale.y);
        branch.setTextures(manager);
        return branch;
    }
}
