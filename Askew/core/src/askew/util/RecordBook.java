package askew.util;

import askew.util.json.JSONLoaderSaver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * A model class containing variables which we set as configurable for faster prototyping and modding.
 * Uses the singleton pattern.
 */
public class RecordBook {

    private static final String TIMES_PATH = "levels/records.json";
    private static RecordBook instance;
    private final JsonPrimitive DEFAULT_COMPLETION_TIME = new JsonPrimitive(9999999.0f);
    private final JsonPrimitive DEFAULT_NUM_GRABS = new JsonPrimitive(9999999);

    private JsonObject dataBlob;

    private RecordBook() {
        dataBlob = new JsonObject();
    }

    public static RecordBook getInstance() {
        if (instance == null) {
            update();
        }
        return instance;
    }

    /* Creates a new instance populated with the current values of the records.json. */
    private static void update() {
        JsonObject newBlob = JSONLoaderSaver.loadArbitrary(TIMES_PATH).orElseThrow(RuntimeException::new);
        instance = new RecordBook();
        instance.dataBlob = newBlob;
    }

    public void addLevel(String lvlname) {
        JsonArray entry = new JsonArray();
        entry.add(DEFAULT_COMPLETION_TIME);
        entry.add(DEFAULT_NUM_GRABS);
        dataBlob.add(lvlname, entry);
    }

    public float getRecord(String lvlname) {
        try {
            return dataBlob.get(lvlname).getAsJsonArray().get(0).getAsFloat();
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
            return 0f;
        }
    }

    public int getRecordGrabs(String lvlname) {
        try {
            return dataBlob.get(lvlname).getAsJsonArray().get(1).getAsInt();
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
            return 9999999;
        }
    }

    public void resetRecord(String lvlname) {
        try {
            dataBlob.remove(lvlname);
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
        }
        addLevel(lvlname);
    }

    public boolean setRecord(String lvlname, float record) {
        try {
            float oldRecord = getRecord(lvlname);
            if (record < oldRecord) {
                dataBlob.get(lvlname).getAsJsonArray().set(0, new JsonPrimitive(record));
            }
            return (record < oldRecord);
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
            return false;
        }
    }

    public boolean setRecordGrabs(String lvlname, int grabs) {
        try {
            int oldGrabs= getRecordGrabs(lvlname);
            if (grabs < oldGrabs) {
                dataBlob.get(lvlname).getAsJsonArray().set(1, new JsonPrimitive(grabs));
            }
            return (grabs < oldGrabs);
        } catch (NullPointerException e) {
            System.err.print("Level record doesn't exist!");
            return false;
        }
    }
}
