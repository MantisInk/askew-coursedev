package askew.entity;

import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class BackgroundEntity extends Entity{

    private float x;
    private float y;
    private float width;
    private float height;
    private float depth;
    private float angle; //angle in radians
    private float alpha;
    private float scalex;
    private float scaley;
    private String texturePath;

    private transient TextureRegion texture;
    protected transient Vector2 origin;

    protected transient  Vector2 positionCache = new Vector2();
    private transient Vector2 sizeCache = new Vector2();


    public BackgroundEntity() {

        this(0,0);
    }

    public BackgroundEntity(float x, float y){
        this(x,y,1,1,-1,0,1,1,1,"texture/background/fern.png");
    }

    public BackgroundEntity(float x, float y, String path){
        this(x,y,1,1,-1,0,1,1,1,path);
    }


    public BackgroundEntity(float x, float y, float width, float height, float depth,
                            float angle, float alpha, float scalex, float scaley, String path){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.angle = angle;
        this.alpha = alpha;
        this.scalex = scalex;
        this.scaley = scaley;
        this.texturePath = path;

        drawScale = new Vector2(1,1);
        objectScale = new Vector2(1,1);
        setObjectScale(scalex,scaley);
        origin = new Vector2(1,1);

    }



    @Override
    public Vector2 getPosition() {
        return positionCache.set(x,y);
    }

    @Override
    public void setPosition(Vector2 value) {
        x = value.x;
        y = value.y;
    }

    @Override
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    public float getDepth(){
        return depth;
    }

    public void setDepth(float d){
        depth = d;
    }

    public float getAngle(){
        return angle;
    }

    public void setAngle(float rads){
        angle = rads;
    }

    public float getAlpha(){return alpha;}

    public void setAlpha(float a){
        alpha = a;
    }

    public float getWidth(){
        return width;
    }

    public void setWidth(float w){
        width = w;
    }

    public float getHeight(){
        return height;
    }

    public void setHeight(float h){
        height = h;
    }

    @Override
    public void setObjectScale(float x, float y) {
        super.setObjectScale(x, y);
        scalex = x;
        scaley = y;
    }

    @Override
    public void setObjectScale(Vector2 value) {
        super.setObjectScale(value);
        scalex = value.x;
        scaley = value.y;
    }
    public String getTexturePath(){
        return texturePath;
    }

    public void setTexturePath(String path){
        texturePath = path;
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
        draw(canvas, new Color(0xffffffff));
    }

    public void draw(GameCanvas canvas, Color tint) {
        if (texture != null) {
            canvas.draw(texture,tint,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),
                    (1.0f/texture.getRegionWidth()) *   getWidth() * getDrawScale().x * objectScale.x,
                    (1.0f/texture.getRegionHeight()  * getHeight()* getDrawScale().y * objectScale.y));
        }
    }

    public void fillJSON() {
        this.x = getPosition().x;
        this.y = getPosition().y;
    }
}
