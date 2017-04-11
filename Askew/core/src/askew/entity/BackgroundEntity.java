package askew.entity;

import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class BackgroundEntity extends Entity{

    private transient Vector2 dimension;
    protected transient Vector2 position;
    protected transient float zDepth;
    protected transient float angle; //angle in radians

    private transient TextureRegion texture;
    protected transient String texturePath;
    /** The texture origin for drawing */
    protected transient Vector2 origin;

    protected transient  Vector2 positionCache = new Vector2();
    private transient Vector2 sizeCache = new Vector2();

    public BackgroundEntity() {
        position = new Vector2();
        zDepth = -1;

        drawScale = new Vector2(1,1);
        objectScale = new Vector2(1,1);
    }

    public Vector2 getDimension() {
        return sizeCache.set(dimension);
    }
    public void setDimension(Vector2 value) {
        setDimension(value.x, value.y);
    }
    public void setDimension(float width, float height) {
        dimension.set(width, height);
    }

    public float getWidth(){return dimension.x;}
    public float getHeight(){return dimension.y;}

    @Override
    public Vector2 getPosition() {
        return positionCache.set(position);
    }

    @Override
    public void setPosition(Vector2 value) {
        position = value;
    }

    @Override
    public void setPosition(float x, float y) {
        position.set(x,y);
    }

    @Override
    public float getX() {
        return position.x;
    }

    @Override
    public void setX(float x) {
        position.set(x,position.y);
    }

    @Override
    public float getY() {
        return position.y;
    }

    @Override
    public void setY(float y) {
        position.set(position.x,y);
    }

    public float getAngle(){
        return angle;
    }

    public void setAngle(float rads){
        angle = rads;
    }

    @Override
    public void setTextures(MantisAssetManager manager) {
        Texture tex = manager.get(texturePath);
        texture = new TextureRegion(tex);
        origin.set(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void draw(GameCanvas canvas) {
        draw(canvas, Color.WHITE);
    }

    public void draw(GameCanvas canvas, Color tint) {
        if (texture != null) {
            canvas.draw(texture,tint,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),
                    (1.0f/texture.getRegionWidth()) *   getWidth() * getDrawScale().x * objectScale.x,
                    (1.0f/texture.getRegionHeight()  * getHeight()* getDrawScale().y * objectScale.y));
        }
    }

}
