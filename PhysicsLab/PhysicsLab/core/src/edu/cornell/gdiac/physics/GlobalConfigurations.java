package edu.cornell.gdiac.physics;


import com.google.gson.JsonObject;

/**
 * A model class containing variables which we set as configurable for faster prototyping and modding.
 * Uses the singleton pattern.
 */
class GlobalConfigurations {

    private static GlobalConfigurations instance;

    public static GlobalConfigurations getInstance() {
        if (instance == null) {
            update();
        }

        return instance;
    }

    /**
     * Creates a new instance populated with the current values of the config.json.
     */
    public static void update() {

    }

    private GlobalConfigurations() {

    }

    public JsonObject getJsonObjectForKey(String key) {

    }
}
