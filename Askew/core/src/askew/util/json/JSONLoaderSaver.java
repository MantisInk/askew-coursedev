package askew.util.json;


import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.EntityWrapper;
import askew.playermode.leveleditor.LevelModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.*;

import java.io.*;
import java.util.Optional;

public class JSONLoaderSaver {

    private Gson gson;
    private EntityWrapper wrapper;
    private JsonParser jsonParser;

    public JSONLoaderSaver(boolean record) {
        if (!record) {
            wrapper = new EntityWrapper();
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.registerTypeAdapter(Entity.class, wrapper);
            gson = gsonBuilder.create();
            jsonParser = new JsonParser();
        }
    }

    public LevelModel loadLevel(String levelName) throws FileNotFoundException {
        FileHandle fileHandle = Gdx.files.internal("levels/" + levelName + ".json");
        if (fileHandle.exists() && !fileHandle.isDirectory()) {
            String contents = fileHandle.readString();

            // Scan the raw level in case we need to perform conversions
            JsonObject rawLevel = jsonParser.parse(contents).getAsJsonObject();
            int levelVersion = rawLevel.get("levelModelVersion").getAsInt();
            if (levelVersion < LevelModel.LATEST_LEVEL_MODEL_VERSION) {
                // Convert!
                contents = convertLevel(rawLevel);
            }
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
        return jsonParser.parse(gson.toJson(o, Entity.class)).getAsJsonObject();
    }

    public JsonObject gsonToJsonObject(Object o) {
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


    public void setManager(MantisAssetManager manager) {
        wrapper.setManager(manager);
    }

    /**
     * Converts a level object to the latest version by version patch functions
     * @param levelObject
     * @return
     */
    private String convertLevel(JsonObject levelObject) {
        switch(levelObject.get("levelModelVersion").getAsInt()) {
            case 1:
                levelObject.addProperty("levelModelVersion",2);
                for (JsonElement ent : levelObject.getAsJsonArray("entities")) {
                    if (ent.getAsJsonObject().getAsJsonPrimitive("CLASSNAME").getAsString().equals("askew.entity.BackgroundEntity")) {
                        if (ent.getAsJsonObject().getAsJsonObject("INSTANCE").get("texturePath").getAsString().equals("texture/background/sil4.png")) {
                            ent.getAsJsonObject().getAsJsonObject("INSTANCE").addProperty("texturePath","texture/background/BackgroundStuff/sil4.png");
                        }
                        if (ent.getAsJsonObject().getAsJsonObject("INSTANCE").get("texturePath").getAsString().equals("texture/background/sil1.png")) {
                            ent.getAsJsonObject().getAsJsonObject("INSTANCE").addProperty("texturePath","texture/background/BackgroundStuff/sil1.png");
                        }
                    }
                }
                return convertLevel(levelObject);
            case 2:
                levelObject.addProperty("levelModelVersion",3);
                for (JsonElement ent : levelObject.getAsJsonArray("entities")) {
                    if (ent.getAsJsonObject().getAsJsonPrimitive("CLASSNAME").getAsString().equals("askew.entity.ghost.GhostModel")) {
                        JsonObject inst = ent.getAsJsonObject().getAsJsonObject("INSTANCE");
                        if (inst.has("patroldx")) {
                            inst.addProperty("patroldx1",inst.get("patroldx").getAsString());
                            inst.addProperty("patroldy1",inst.get("patroldy").getAsString());
                            inst.addProperty("patroldx2",inst.get("x").getAsString());
                            inst.addProperty("patroldy2",inst.get("y").getAsString());
                            inst.remove("patroldx");
                            inst.remove("patroldy");
                        }
                    }
                }
                return convertLevel(levelObject);
            case LevelModel.LATEST_LEVEL_MODEL_VERSION:
                return gson.toJson(levelObject);
            default:
                System.err.println("Trying to convert a level model without a conversion function!");
                break;
        }
        return null;
    }
}
