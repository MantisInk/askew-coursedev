package askew;

import askew.util.json.JSONLoaderSaver;
import com.google.gson.JsonObject;

/**
 * A model class containing variables which we set as configurable for faster prototyping and modding.
 * Uses the singleton pattern.
 */
public class GlobalConfiguration {

    private static final String CONFIG_PATH = "config.json";
    private static GlobalConfiguration instance;

    private JsonObject dataBlob;

    public static GlobalConfiguration getInstance() {
        if (instance == null) {
            update();
        }

        return instance;
    }

    /**
     * Creates a new instance populated with the current values of the config.json.
     */
    public static void update() {
       JsonObject newBlob = JSONLoaderSaver.loadArbitrary(CONFIG_PATH).orElseGet(JsonObject::new);
        instance = new GlobalConfiguration();
        instance.dataBlob = newBlob;
    }

    private GlobalConfiguration() {
        dataBlob = new JsonObject();
    }

    /**
     * Returns the boolean represented by a key.
     * @param key The unique key at the top level of the config.json
     * @return The object or null if it does not exist.
     */
    public boolean getAsBoolean(String key) {
        return dataBlob.get(key).getAsBoolean();
    }

    /**
     * Similar to getAsBoolean
     */
    public String getAsString(String key) {
        return dataBlob.get(key).getAsString();
    }

    /**
     * Similar to getAsBoolean
     */
    public int getAsInt(String key) {
        return dataBlob.get(key).getAsInt();
    }

    /**
     * Similar to getAsBoolean
     */
    public float getAsFloat(String key) {
        return dataBlob.get(key).getAsFloat();
    }
}
