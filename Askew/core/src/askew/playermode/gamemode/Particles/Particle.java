package askew.playermode.gamemode.Particles;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import lombok.Getter;

public class Particle implements Comparable {

    float x;
    float y;
    float vx;
    float vy;
    float fx;
    float fy;
    float width;
    float height;
    @Getter
    float depth;
    float angle;
    float rot;
    @Getter
    int drawNumber;
    float accumulator;
    float deathTime;
    int type;
    Color tint;
    Texture texture;


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
        this.x = x + (float)((Math.random() - .5) * 15 );
        this.y = y + (float)((Math.random() - .5) * 15 );;
        this.width = 1;
        this.height = 1;
        this.depth = 6f;
        this.tint = new Color(0xFFFFFF4F);
        this.deathTime = 4 + (float)((Math.random() - .5) * 2 );
        this.accumulator = (float)((Math.random() - .5) * 2 );
        this.type = 0;
        return this;
    }

    public void update(float delta){
        accumulator += delta;

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
