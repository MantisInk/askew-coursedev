package askew.entity;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity implements Comparable{
    protected transient Vector2 drawScale;
    protected transient Vector2 objectScale;

    protected transient Vector2 drawScaleCache = new Vector2();
    protected transient Vector2 objectScaleCache = new Vector2();

    protected transient int drawNumber = 0;

    public abstract Vector2 getPosition();
    public abstract void setPosition(Vector2 value);
    public abstract void setPosition(float x, float y);

    public abstract float getX();
    public abstract void setX(float x);
    public abstract float getY();
    public abstract void setY(float y);

    public int getDrawNumber(){
        return drawNumber;
    }

    public void setDrawNumber(int n){
        drawNumber = n;
    }


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

    public Vector2 getModifiedPosition(float adjustedCxCamera, float adjustedCyCamera){
        Vector2 pos = getPosition();
        if(this instanceof BackgroundEntity){
            float offsetx = ((this.getPosition().x - adjustedCxCamera)) / ((BackgroundEntity) this).getDepth();
            float offsety = ((this.getPosition().y - adjustedCyCamera)) / ((BackgroundEntity) this).getDepth();
            pos.set(adjustedCxCamera  + offsetx, adjustedCyCamera + offsety);

        }
        return pos;
    }

    public void setModifiedPosition( float x, float y, float adjustedCxCamera, float adjustedCyCamera){
        if(this instanceof BackgroundEntity){
            this.setPosition(x * ((BackgroundEntity) this).getDepth(), y * ((BackgroundEntity) this).getDepth());
            this.setPosition( (x - adjustedCxCamera) * ((BackgroundEntity) this).getDepth() + adjustedCxCamera,
                    (y - adjustedCyCamera) * ((BackgroundEntity) this).getDepth() + adjustedCyCamera);

        } else{
            this.setPosition(x,y);
        }
    }

    @Override
    public int compareTo(Object o) {

        float thisDepth = 1;
        float oDepth = 1;
        int oDrawNum = 0;

        if( this instanceof  BackgroundEntity){
            thisDepth = ((BackgroundEntity)this).getDepth();
        }
        if(o instanceof Entity){
            if( o instanceof  BackgroundEntity){
                oDepth = ((BackgroundEntity)o).getDepth();
            }
            oDrawNum = ((Entity)o).getDrawNumber();
        }
        int comp =  java.lang.Float.compare(thisDepth,oDepth);
        if(comp == 0){
            comp = java.lang.Integer.compare(this.drawNumber, oDrawNum);
        }
        comp *= -1;


        return comp;
    }


}
