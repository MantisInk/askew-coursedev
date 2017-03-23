package edu.cornell.gdiac.physics;


import com.google.gson.JsonObject;
import edu.cornell.gdiac.physics.leveleditor.JSONLoaderSaver;

/**
 * A model class containing variables which we set as configurable for faster prototyping and modding.
 * Uses the singleton pattern.
 */
class GlobalConfiguration {

    private static final String CONFIG_PATH = "./config.json";
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
     * Returns the JsonObject represented by a key. This is so that data can be of an arbitrary format.
     * @param key The unique key at the top level of the config.json
     * @return The object or null if it does not exist.
     */
    public JsonObject getJsonObjectForKey(String key) {
        return dataBlob.getAsJsonObject(key);
    }
}
