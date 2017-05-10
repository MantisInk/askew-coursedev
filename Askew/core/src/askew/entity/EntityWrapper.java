package askew.entity;

import askew.MantisAssetManager;
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
                return JsonEntityFactory.createSloth(manager, instance);
            case ".Vine":
                return JsonEntityFactory.createVine(manager, instance);
            case ".Trunk":
                return JsonEntityFactory.createTrunk(manager, instance);
            case ".PoleVault":
                return JsonEntityFactory.createPoleVault(manager, instance);
            case ".StiffBranch":
                return JsonEntityFactory.createStiffBranch(manager, instance);
            case ".OwlModel":
                return JsonEntityFactory.createOwl(manager, instance);
            case ".WallModel":
                return JsonEntityFactory.createWall(manager, instance);
            case ".ThornModel":
                return JsonEntityFactory.createThorn(manager, instance);
            case ".GhostModel":
                return JsonEntityFactory.createGhost(manager, instance);
            case ".BackgroundEntity":
                return JsonEntityFactory.createBGEntity(manager,instance);
            case ".EyeEntity":
                return JsonEntityFactory.createEyeEntity(manager,instance);
            default:
                System.err.println("Unrecognized in wrapper: " + obstacleClass);
                Class<?> klass;
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