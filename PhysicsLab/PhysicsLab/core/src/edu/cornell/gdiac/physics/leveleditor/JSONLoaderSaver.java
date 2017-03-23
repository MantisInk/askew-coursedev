package edu.cornell.gdiac.physics.leveleditor;


import com.badlogic.gdx.math.Vector2;
import com.google.gson.*;
import edu.cornell.gdiac.physics.obstacle.Obstacle;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class JSONLoaderSaver {

    private Gson gson;

    @Setter @Getter
    private Vector2 scale;

    public JSONLoaderSaver() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(Obstacle.class, new EntityWrapper(this));
        gson = gsonBuilder.create();
    }

    public LevelModel loadLevel(String levelName) throws FileNotFoundException {
        FileReader fr = new FileReader("./levels/" + levelName + ".json");
        LevelModel loaded = gson.fromJson(fr, LevelModel.class);
        return loaded;
    }

    public boolean saveLevel(LevelModel toSave, String levelName) {
        try {
            FileWriter fw = new FileWriter("./levels/" + levelName + ".json");
            gson.toJson(toSave, fw);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String gsonToJson(Obstacle o) {
        return gson.toJson(o, Obstacle.class);
    }

    public Obstacle obstacleFromJson(String s) {
        return gson.fromJson(s, Obstacle.class);
    }

    /**
     * Loads an arbitrary file into a generic JSON object.
     * @param assetPath the path in assets referencing the file
     * @return the JsonObject optional
     */
    public static Optional<JsonObject> loadArbitrary(String assetPath) {
        FileReader fr = null;
        try {
            fr = new FileReader(assetPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(fr);
        return Optional.of(je.getAsJsonObject());
    }

}
