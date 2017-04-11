package askew.entity;

import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;

public abstract class BackgroundEntity extends Entity{

    protected transient Vector2 drawScale;
    protected transient float zdepth;

    @Override
    public void setTextures(MantisAssetManager manager) {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void draw(GameCanvas canvas) {

    }
}
