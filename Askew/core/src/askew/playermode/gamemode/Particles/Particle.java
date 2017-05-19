package askew.playermode.gamemode.Particles;


import com.badlogic.gdx.graphics.Color;
import lombok.Getter;

@SuppressWarnings("UnusedParameters")
public class Particle implements Comparable {

    float x;
    float y;
    float width;
    float height;
    @Getter
    float depth;
    float angle;
    @Getter
    int drawNumber;
    float accumulator;
    float deathTime;
    int type;
    int textureNum;
    Color tint;
    private float vx;
    private float vy;
    private float fx;
    private float fy;
    private float rot;


    public Particle() {

    }

    public Particle spawn(int type, float x, float y, float vx, float vy, float fx, float fy,
                          float width, float height, float depth, int drawNumber, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.fx = fx;
        this.fy = fy;
        this.width = width;
        this.height = height;
        this.angle = 0;
        this.rot = 0;
        this.depth = depth;
        this.drawNumber = drawNumber;
        this.tint = color;
        return this;
    }


    public void spawnFog(float x, float y, float boundsx, float boundsy) {
        this.type = 1;
        this.depth = 6f + (float) ((Math.random() - .5) * 2 * 4.9);
        this.x = (x + (float) ((Math.random() - .7) * boundsx)) * depth;
        this.y = (y + (float) ((Math.random() - .5) * boundsy * 2)) * depth;
        this.vx = 4 + (float) ((Math.random() - .5) * 2);
        this.vy = (float) ((Math.random() - .5) * .2);
        this.fx = 0;
        this.fy = 0;
        this.width = 10;
        this.height = 5f;
        this.angle = 0;
        this.rot = 0;
        this.drawNumber = 0;
        this.tint = new Color(1, 1, 1, .09f);
        this.textureNum = (int) (Math.random() * 2);
        this.deathTime = 7 + (float) ((Math.random() - .5) * 6);
        this.accumulator = (float) ((Math.random() - .5) * 2);
    }

    public void spawnDefault(float x, float y) {
        this.x = x + (float) ((Math.random() - .5) * 15);
        this.y = y + (float) ((Math.random() - .5) * 15);
        this.width = 1;
        this.height = 1;
        this.depth = 6f + (float) ((Math.random() - .5) * 3);
        this.tint = new Color(0xFFFFFF4F);
        this.deathTime = 4 + (float) ((Math.random() - .5) * 2);
        this.accumulator = (float) ((Math.random() - .5) * 2);
        this.type = 0;
    }

    public void spawnHandTrail(float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.width = .2f;
        this.height = .2f;
        this.depth = 1.0001f;
        this.angle = angle;
        this.tint = new Color(0xFFFFFF4F);
        this.deathTime = .25f;
        this.accumulator = 0;
        this.type = 2;
    }

    public void spawnEyes(float x, float y) {
        this.x = x;
        this.y = y;
        this.width = 1f  + (float)(Math.random() *.4) ;
        this.height = .6f  + (float)(Math.random() *.2);
        this.depth = 2 + (float)(Math.random() *3);
        this.angle = 0;
        this.tint = new Color(1,1,1,(float) Math.random() * .3f);
        this.textureNum = 0;
        this.deathTime = 10f;
        this.accumulator = (float)Math.random()*1.5f;
        this.fx = (float)(Math.random() * 5) - 3; // = cooldown
        this.fy = (float)(Math.random() * 5) + 3;; //start time
        this.type = 3;
    }


    public void update(float delta) {
        accumulator += delta;
        float t = accumulator / deathTime;
        if (type == 1) {
            this.x += (this.vx * delta);
            this.y += (this.vy * delta);
            this.tint.set(1, 1, 1, .10f * (-.5f * (float) Math.cos(6.283 * t) + .5f));
        }
        if(type == 2){
            this.tint.set(1f, .93f, .91f, .25f * (1-t));
        }
        if(type == 3){
            if(fx < 0 ){
                this.fx += delta;
                if(fx >= 0){
                    fx = 4f;
                    this.tint = new Color(1,1,1,(float) Math.random() * .3f);
                }

            }
            else {
                this.fx -= delta;
                if(fx < 0){
                    fx = -1f;
                    this.tint = new Color(0,0,0,0);
                }
                this.textureNum = (int) (accumulator * fy) % 4;
            }
        }

    }

    @Override
    public int compareTo(Object o) {

        float thisDepth = this.depth;
        float oDepth = 1;
        int oDrawNum = 0;

        if (o instanceof Particle) {
            oDepth = ((Particle) o).depth;
            oDrawNum = ((Particle) o).drawNumber;
        }
        int comp = java.lang.Float.compare(thisDepth, oDepth);
        if (comp == 0) {
            comp = java.lang.Integer.compare(this.drawNumber, oDrawNum);
        }
        comp *= -1;


        return comp;
    }
}
