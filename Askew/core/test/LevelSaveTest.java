import askew.playermode.leveleditor.FullAssetTracker;
import askew.playermode.leveleditor.LevelModel;
import askew.entity.obstacle.sloth.SlothModel;
import org.junit.Test;
import askew.playermode.leveleditor.JSONLoaderSaver;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LevelSaveTest {

    private LevelModel makeLevel() {
        LevelModel lm = new LevelModel();
        lm.setBackground("test background");
        lm.setTitle("test title");
        lm.setGoalTimeBronze(160);
        lm.setGoalTimeSilver(120);
        lm.setGoalTimeGold(60);

        return lm;
    }

    @Test
    public void testLevelSaveAndLoad() {
        JSONLoaderSaver jls = new JSONLoaderSaver();
        LevelModel lm = makeLevel();
        assertTrue(jls.saveLevel(lm,"test_save"));
        LevelModel loaded = null;
        try {
            loaded = jls.loadLevel("test_save");
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        }
        assertTrue(loaded.getTitle().equals(lm.getTitle()));
    }

    @Test
    public void testObstacleSaveLoad() {
        FullAssetTracker fat = FullAssetTracker.getInstance();
        JSONLoaderSaver jls = new JSONLoaderSaver();
        LevelModel lm = makeLevel();
        lm.addEntity(new SlothModel(0,0));
        assertTrue(jls.saveLevel(lm,"test_save_obstacle"));
        LevelModel loaded = null;
        try {
            loaded = jls.loadLevel("test_save_obstacle");
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        }
        assertTrue(loaded.getTitle().equals(lm.getTitle()));
    }
}
