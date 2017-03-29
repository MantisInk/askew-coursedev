package askew.playermode.leveleditor;

import askew.entity.obstacle.Obstacle;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

    String soundtrack;


    int height;


    int width;

    List<Obstacle> entities;

    public LevelModel() {
        this.entities = new ArrayList<Obstacle>();
        this.height = 768;
        this.width = 1024;
        this.levelModelVersion = LATEST_LEVEL_MODEL_VERSION;
        this.goalTimeGold = 30;
        this.goalTimeSilver = 45;
        this.goalTimeBronze = 60;
    }

    public void addEntity(Obstacle o) {
        entities.add(o);
    }
}
