package askew.playermode.gamemode;


import askew.GameCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Particle implements Comparable {

    float x;
    float y;
    float vx;
    float vy;
    float fx;
    float fy;
    float width;
    float height;
    float depth;
    int drawNumber;
    float accumulator;
    float deathTime;
    Color tint;
    Texture texture;
    float active;


    public Particle(){

    }

    public Particle(float x, float y, float vx, float vy, float fx, float fy,
                    float width, float height, float depth, int drawNumber, Color color){
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.fx = fx;
        this.fy = fy;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.drawNumber = drawNumber;
        this.tint = color;

    }

    public Particle spawn(float x, float y){
        return this;
    }

    public void update(){

    }

    public void draw(GameCanvas canvas){
        if (texture != null) {
            canvas.drawBackgroundEntity(texture, tint ,texture.getWidth()/2,texture.getHeight()/2,getX()*drawScale.x,getY()*drawScale.y, getDepth(), getAngle(),
                    (1.0f/texture.getWidth()) *   width * getDrawScale().x  * aspectRatio,
                    (1.0f/texture.getHeight())  * height * getDrawScale().y * objectScale.y), 1);
        }

    }

    public void setTexture(Texture texture){
        this.texture = texture;

    }

    @Override
    public int compareTo(Object o) {

        float thisDepth = this.depth;
        float oDepth = 1;
        int oDrawNum = 0;

        if(o instanceof Particle){
            oDepth = ((Particle) o).depth;
            oDrawNum = ((Particle)o).drawNumber;
        }
        int comp =  java.lang.Float.compare(thisDepth,oDepth);
        if(comp == 0){
            comp = java.lang.Integer.compare(this.drawNumber, oDrawNum);
        }
        comp *= -1;


        return comp;
    }
}
