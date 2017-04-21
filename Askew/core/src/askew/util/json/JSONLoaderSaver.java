package askew.util.json;


import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.playermode.leveleditor.LevelModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.*;
import askew.entity.obstacle.Obstacle;
import lombok.Cleanup;
import lombok.Getter;

import java.io.*;
import java.util.Optional;

public class JSONLoaderSaver {

    private Gson gson;

    private EntityWrapper wrapper;

    public JSONLoaderSaver() {
        wrapper = new EntityWrapper();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Entity.class, wrapper);
        gson = gsonBuilder.create();
    }

    public LevelModel loadLevel(String levelName) throws FileNotFoundException {
        FileHandle fileHandle = Gdx.files.internal("levels/" + levelName + ".json");
        if (fileHandle.exists() && !fileHandle.isDirectory()) {
            String contents = fileHandle.readString();
            return gson.fromJson(contents, LevelModel.class);
        }

        System.err.println("Missing: " + levelName);
        return null;
    }

    public boolean saveLevel(LevelModel toSave, String levelName) {
        try {
            FileWriter fw = new FileWriter("levels/" + levelName + ".json");
            gson.toJson(toSave, fw);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public LevelModel levelFromJson(JsonObject olm) {
        return gson.fromJson(olm, LevelModel.class);
    }

    public String gsonToJson(Entity o) {
        return gson.toJson(o, Entity.class);
    }

    public JsonObject gsonToJsonObject(Entity o) {
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(gson.toJson(o, Entity.class)).getAsJsonObject();
    }

    public JsonObject gsonToJsonObject(Object o) {
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(gson.toJson(o)).getAsJsonObject();
    }

    public String stringFromJson(JsonElement o) {
        return gson.toJson(o);
    }

    public Entity entityFromJson(String s) {
        return gson.fromJson(s, Entity.class);
    }

    /**
     * Loads an arbitrary file into a generic JSON object. Note- this must be run _after_ GDX init.
     * So don't try to grab any config for the desktop loader.
     * @param assetPath the path in assets referencing the file
     * @return the JsonObject optional
     */
    public static Optional<JsonObject> loadArbitrary(String assetPath) {
        FileHandle fileHandle = Gdx.files.internal(assetPath);
        if (fileHandle.exists() && !fileHandle.isDirectory()) {
            String contents = fileHandle.readString();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(contents);
            return Optional.of(je.getAsJsonObject());
        }

        System.err.println("Missing: " + assetPath);
        return Optional.empty();
    }

    public static void saveArbitrary(String s, String text) {
        try {
            FileWriter fw = new FileWriter(s);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            // Not much we can do, unlikely to happen anyway.
            e.printStackTrace();
        }
    }

    public String prettyJson(JsonObject notPrettyJson) {
        return gson.toJson(notPrettyJson);
    }

    public void setScale(Vector2 scale) {
        wrapper.setScale(scale);
    }

    public void setManager(MantisAssetManager manager) {
        wrapper.setManager(manager);
    }
}
