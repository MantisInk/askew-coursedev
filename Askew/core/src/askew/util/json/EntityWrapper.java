package askew.util.json;

import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.JsonEntityFactory;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.*;
import lombok.Setter;

import java.lang.reflect.Type;

/**
 * Wraps entities so we can serialize them over level loaders
 */
public class EntityWrapper implements JsonSerializer<Entity>, JsonDeserializer<Entity> {

    private static final String CLASSNAME = "CLASSNAME";
    private static final String INSTANCE  = "INSTANCE";

    @Setter
    private Vector2 scale;

    @Setter
    private MantisAssetManager manager;

    public EntityWrapper() {
    }

    @Override
    public JsonElement serialize(Entity src, Type typeOfSrc,
                                 JsonSerializationContext context) {
        JsonObject retValue = new JsonObject();
        String className = src.getClass().getName();
        retValue.addProperty(CLASSNAME, className);
        JsonElement elem = context.serialize(src);
        retValue.add(INSTANCE, elem);
        return retValue;
    }

    @Override
    public Entity deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException  {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = prim.getAsString();

        // Interpret the class, construct as appropriate
        String obstacleClass = className.substring(className.lastIndexOf("."));
        JsonObject instance = jsonObject.get("INSTANCE").getAsJsonObject();
        switch(obstacleClass) {
            case ".SlothModel":
                return JsonEntityFactory.createSloth(manager, instance, scale);
            case ".Vine":
                return JsonEntityFactory.createVine(manager, instance, scale);
            case ".Trunk":
                return JsonEntityFactory.createTrunk(manager, instance, scale);
            case ".PoleVault":
                return JsonEntityFactory.createPoleVault(manager, instance, scale);
            case ".StiffBranch":
                return JsonEntityFactory.createStiffBranch(manager, instance, scale);
            case ".Tree":
                return JsonEntityFactory.createTree(manager, instance, scale);
            case ".OwlModel":
                return JsonEntityFactory.createOwl(manager, instance, scale);
            case ".WallModel":
                return JsonEntityFactory.createWall(manager, instance, scale);
            case ".GhostModel":
                return JsonEntityFactory.createGhost(manager, instance, scale);
            default:
                System.err.println("Unrecognized in wrapper: " + obstacleClass);
                Class<?> klass = null;
                try {
                    klass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new JsonParseException(e.getMessage());
                }
                return context.deserialize(jsonObject.get(INSTANCE), klass);
        }
    }
}