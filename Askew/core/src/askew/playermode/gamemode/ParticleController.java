package askew.playermode.gamemode;


import askew.GameCanvas;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;

public class ParticleController {

    GameModeController gmc;
    LinkedList<Particle> unspawned = new LinkedList<Particle>();
    LinkedList<Particle> spawned = new LinkedList<Particle>();
    Particle[] tempArray;
    @Setter
    Vector2 drawScale;

    public ParticleController(GameModeController gmc, int maxParticles) {
        tempArray = new Particle[maxParticles];
        for (int i = 0; i < maxParticles; ++i) {
            Particle particle = new Particle();
            unspawned.add(particle);
        }
    }

    public Particle[] getSorted(){
        Collections.sort(spawned);
        return spawned.toArray(tempArray);
    }

    public int numParticles(){
        return spawned.size();
    }

    public void reset(){
        for(Particle p : spawned){
            unspawned.add(p);
            spawned.removeFirst();
        }
    }


    public void update(float delta){
        if (gmc.paused){
            return;
        }

        int counter = 0;
        while (counter < spawned.size()){
            Particle curParticle = spawned.get(counter);
            if(curParticle.accumulator > curParticle.deathTime){
                curParticle.update();
                counter += 1;
            }
            else{
                unspawned.add(curParticle);
                spawned.remove(counter);
            }
        }
    }

    public void draw(GameCanvas canvas, Particle p){



    }


    private int effect1_num = 15;
    private String texturePath = "";


    public void effect1(){





    }




}
