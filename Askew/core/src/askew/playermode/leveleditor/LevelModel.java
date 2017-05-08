package askew.playermode.leveleditor;

import askew.entity.Entity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public @Data
class LevelModel {

    public static final int LATEST_LEVEL_MODEL_VERSION = 3;

    int levelModelVersion;

    int goalTimeGold;
    int goalTimeSilver;
    int goalTimeBronze;
    float recordTime;

    /** Determines when Flow should die and limits camera movement */
    float maxX;
    float maxY;
    float minX;
    float minY;

    String title;
    String background;
    String soundtrack;

    List<Entity> entities;

    public LevelModel() {
        this.entities = new ArrayList<Entity>();
        this.levelModelVersion = LATEST_LEVEL_MODEL_VERSION;
        this.goalTimeGold = 30;
        this.goalTimeSilver = 45;
        this.goalTimeBronze = 60;
        this.recordTime = 9999999;
        this.title = "Title";
        this.background = "texture/background/background1.png";
        this.soundtrack = "TODO";
        this.minX = 0;
        this.minY = 0;
        this.maxX = 20;
        this.maxY = 20;
    }

    public void addEntity(Entity o) {
        entities.add(o);
    }
}
