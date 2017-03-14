package edu.cornell.gdiac.physics.leveleditor;


import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONLoaderSaver {

    private Gson gson;

    public JSONLoaderSaver() {
        gson = new Gson();

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

}
