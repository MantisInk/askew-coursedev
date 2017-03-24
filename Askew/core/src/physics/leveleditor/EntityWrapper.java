package physics.leveleditor;

import com.google.gson.*;
import physics.obstacle.Obstacle;
import physics.platform.StiffBranch;
import physics.platform.Trunk;
import physics.platform.Vine;
import physics.platform.sloth.SlothModel;

import java.lang.reflect.Type;

/**
 * Wraps entities so we can serialize them over level loaders
 */
public class EntityWrapper implements JsonSerializer<Obstacle>, JsonDeserializer<Obstacle> {

    private static final String CLASSNAME = "CLASSNAME";
    private static final String INSTANCE  = "INSTANCE";
    private JSONLoaderSaver parent;

    public EntityWrapper(JSONLoaderSaver parent) {
        this.parent = parent;
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
                return SlothModel.createFromJson(instance, parent.getScale());
            case ".Vine":
                return Vine.createFromJson(instance, parent.getScale());
            case ".Trunk":
                return Trunk.createFromJson(instance, parent.getScale());
            case ".StiffBranch":
                return StiffBranch.createFromJson(instance, parent.getScale());
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