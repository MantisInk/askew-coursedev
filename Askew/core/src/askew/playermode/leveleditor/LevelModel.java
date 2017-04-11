package askew.playermode.leveleditor;

import askew.entity.Entity;
import askew.entity.obstacle.Obstacle;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public @Data
class LevelModel {

    public static final int LATEST_LEVEL_MODEL_VERSION = 1;

    int levelModelVersion;

    String title;

    int goalTimeGold;

    int goalTimeSilver;

    int goalTimeBronze;

    String background;

    // UNIMPLEMENTED
    String soundtrack;

    /** Meters. Height of level, not player view. */
    int height;

    /** Meters. Width of level, not player view. */
    int width;

    List<Entity> entities;

    public LevelModel() {
        this.entities = new ArrayList<Entity>();
        this.height = 10;
        this.width = 10;
        this.levelModelVersion = LATEST_LEVEL_MODEL_VERSION;
        this.goalTimeGold = 30;
        this.goalTimeSilver = 45;
        this.goalTimeBronze = 60;
    }

    public void addEntity(Entity o) {
        entities.add(o);
    }
}
