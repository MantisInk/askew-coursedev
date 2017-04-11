package askew.entity;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonObject;

public abstract class Entity {
    protected transient Vector2 drawScale;
    protected transient Vector2 objectScale;

    protected transient Vector2 drawScaleCache = new Vector2();
    protected transient Vector2 objectScaleCache = new Vector2();

    public abstract Vector2 getPosition();
    public abstract void setPosition(Vector2 value);
    public abstract void setPosition(float x, float y);

    public abstract float getX();
    public abstract void setX(float x);
    public abstract float getY();
    public abstract void setY(float y);

    /// DRAWING METHODS
    public Vector2 getDrawScale() {
        drawScaleCache.set(drawScale);
        return drawScaleCache;
    }
    public void setDrawScale(Vector2 value) {
        setDrawScale(value.x,value.y);
    }
    public void setDrawScale(float x, float y) {
        drawScale.set(x,y);
    }

    public Vector2 getObjectScale() {
        objectScaleCache.set(objectScale);
        return objectScaleCache;
    }
    public void setObjectScale(Vector2 value) {
        setObjectScale(value.x,value.y);
    }
    public void setObjectScale(float x, float y) {
        objectScale.set(x,y);
    }

    public abstract void setTextures(MantisAssetManager manager);
    public abstract void update(float delta);
    public abstract void draw(GameCanvas canvas);


}
