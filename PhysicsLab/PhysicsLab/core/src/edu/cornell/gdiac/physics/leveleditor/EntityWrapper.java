package edu.cornell.gdiac.physics.leveleditor;

import com.google.gson.*;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import edu.cornell.gdiac.physics.platform.Vine;
import edu.cornell.gdiac.physics.platform.sloth.SlothModel;

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
        switch(obstacleClass) {
            case ".SlothModel":
                JsonObject instance = jsonObject.get("INSTANCE").getAsJsonObject();
                SlothModel ret;
                ret = new SlothModel(instance.get("x").getAsFloat(), instance.get("y").getAsFloat());
                ret.setDrawScale(parent.getScale().x, parent.getScale().y);
                ret.setPartTextures();
                return ret;
            case ".Vine":
                instance = jsonObject.get("INSTANCE").getAsJsonObject();
                Vine vine;
                float x = instance.get("x").getAsFloat();
                float y = instance.get("y").getAsFloat();
                float numlinks = instance.get("numLinks").getAsFloat();
                vine = new Vine(x, y, numlinks, 0.25f, 1.0f);
                vine.setDrawScale(parent.getScale().x, parent.getScale().y);
                vine.setTextures();
                return vine;
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