package askew.playermode.gamemode.Particles;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.math.Vector2;

import java.util.LinkedList;

public abstract class Effect {

    public final LinkedList<Particle> spawned = new LinkedList<>();
    public final LinkedList<Particle> unspawned;
    public ParticleController particleController;
    public Vector2 drawScale;

    public  Effect(ParticleController pc, LinkedList<Particle> unspawned){
        this.particleController = pc;
        this.unspawned = unspawned;
        setDrawScale(pc.drawScale);
    }

    //public abstract void spawn();

    public abstract void setTexture(MantisAssetManager manager);

    public abstract void draw(GameCanvas canvas);

    public void setDrawScale(Vector2 drawScale){
        this.drawScale = drawScale;
    }

    public int size(){
        return spawned.size();
    }


    public void update(float delta) {
        int i = 0;
        while (i < spawned.size()){
            Particle curParticle = spawned.get(i);
            if(curParticle.accumulator < curParticle.deathTime){
                curParticle.update(delta);
                i += 1;
            }
            else{
                unspawned.add(curParticle);
                spawned.remove(i);
            }
        }
    }

    public void reset() {
        while ( spawned.size() != 0) {
            Particle p = spawned.getFirst();
            unspawned.add(p);
            spawned.removeFirst();
        }
    }
}
