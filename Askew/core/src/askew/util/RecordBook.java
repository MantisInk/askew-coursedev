package askew.util;

import askew.util.json.JSONLoaderSaver;
import com.google.gson.JsonObject;

/**
 * A model class containing variables which we set as configurable for faster prototyping and modding.
 * Uses the singleton pattern.
 */
public class RecordBook {

    private static final String CONFIG_PATH = "levels/records.json";
    private static RecordBook instance;
    private final float DEFAULT_COMPLETION_TIME = 9999999.0f;

    private JsonObject dataBlob;

    public static RecordBook getInstance() {
        if (instance == null) {
            update();
        }
        return instance;
    }

    /* Creates a new instance populated with the current values of the records.json. */
    private static void update() {
        JsonObject newBlob = JSONLoaderSaver.loadArbitrary(CONFIG_PATH).orElseThrow(RuntimeException::new);
        instance = new RecordBook();
        instance.dataBlob = newBlob;
    }

    private RecordBook() {
        dataBlob = new JsonObject();
    }

    public void addLevel(String lvlname) {
        dataBlob.addProperty(lvlname, DEFAULT_COMPLETION_TIME);
    }

    public float getRecord(String lvlname) {
        try {
            return dataBlob.get(lvlname).getAsFloat();
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
            return 0f;
        }
    }

    public void resetRecord(String lvlname) {
        try {
            dataBlob.remove(lvlname);
            dataBlob.addProperty(lvlname, DEFAULT_COMPLETION_TIME);
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
            dataBlob.addProperty(lvlname, DEFAULT_COMPLETION_TIME);
        }
    }

    public boolean setRecord(String lvlname, float record) {
        try {
            dataBlob.remove(lvlname);
            dataBlob.addProperty(lvlname, record);
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
            return false;
        }
        return true;
    }

//    public boolean save() {
//        try {
//            FileWriter fw = new FileWriter(CONFIG_PATH);
//            GsonBuilder gsonBuilder = new GsonBuilder();
//            gsonBuilder.setPrettyPrinting();
//            gsonBuilder.registerTypeAdapter(Entity.class, wrapper);
//            Gson gson = gsonBuilder.create();
//            gson.toJson(toSave, fw);
//            fw.flush();
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
}
