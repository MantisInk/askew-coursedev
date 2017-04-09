package askew.entity;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonObject;

public abstract class Entity {
    public abstract void setTextures(MantisAssetManager manager);
    public abstract void update(float delta);
    public abstract void draw(GameCanvas canvas);
}
