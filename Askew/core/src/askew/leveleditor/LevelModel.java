package askew.leveleditor;

import askew.entity.obstacle.Obstacle;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class LevelModel {

    public static final int LATEST_LEVEL_MODEL_VERSION = 1;

    @Getter
    int levelModelVersion;

    @Getter
    @Setter
    String title;
    @Getter
    @Setter
    int goalTimeGold;
    @Getter
    @Setter
    int goalTimeSilver;
    @Getter
    @Setter
    int goalTimeBronze;

    @Getter
    @Setter
    String background;
    @Getter
    @Setter
    String soundtrack;

    @Getter
    @Setter
    int height;

    @Getter
    @Setter
    int width;

    @Getter
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
