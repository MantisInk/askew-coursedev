package askew.util.json;

import askew.entity.JsonEntityFactory;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.*;
import askew.entity.obstacle.Obstacle;
import askew.entity.StiffBranch;
import askew.entity.Trunk;
import askew.entity.vine.Vine;
import askew.entity.sloth.SlothModel;
import lombok.Setter;

import java.lang.reflect.Type;

/**
 * Wraps entities so we can serialize them over level loaders
 */
public class EntityWrapper implements JsonSerializer<Obstacle>, JsonDeserializer<Obstacle> {

    private static final String CLASSNAME = "CLASSNAME";
    private static final String INSTANCE  = "INSTANCE";
    @Setter
    private Vector2 scale;
    @Setter
    private AssetManager manager;

    public EntityWrapper() {
    }

    @Override
    public JsonElement serialize(Obstacle src, Type typeOfSrc,
                                 JsonSerializationContext context) {

        JsonObject retValue = new JsonObject();
        String className = src.getClass().getName();
        retValue.addProperty(CLASSNAME, className);
        JsonElement elem = context.serialize(src);
        retValue.add(INSTANCE, elem);
        return retValue;
    }

    @Override
    public Obstacle deserialize(JsonElement json, Type typeOfT,
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
            case ".StiffBranch":
                return JsonEntityFactory.createStiffBranch(manager, instance, scale);
            default:
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