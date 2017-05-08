package askew.playermode.gamemode.Particles;


import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.playermode.gamemode.GameModeController;
import askew.playermode.gamemode.Particles.Particle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
        this.gmc = gmc;
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

    public void setTextures(MantisAssetManager manager){
        Texture tex = manager.get(effect1_texturePath);
        effect1_texture = new TextureRegion(tex);

    }


    public void update(float delta){
        if (gmc.isPaused()){
            return;
        }
        int counter = 0;
        while (counter < spawned.size()){
            Particle curParticle = spawned.get(counter);
            if(curParticle.accumulator < curParticle.deathTime){
                curParticle.update(delta);
                counter += 1;
            }
            else{
                unspawned.add(curParticle);
                spawned.remove(counter);
            }
        }
    }

    public void draw(GameCanvas canvas, Particle p){

        if(p.accumulator > 0) {
            if (p instanceof Particle) {
                if (effect1_texture != null) {
                    TextureRegion tex = effect1_texture;
                    canvas.drawBackgroundEntity(tex, p.tint, tex.getRegionWidth() / 2, tex.getRegionHeight() / 2,
                            p.x * drawScale.x, p.y * drawScale.y, p.depth, 0,
                            (1.0f / tex.getRegionWidth()) * p.width * drawScale.x, (1.0f / tex.getRegionHeight() * p.height * drawScale.y),
                            1);
                }

            }
        }
    }


    private int effect1_num = 15;
    private String effect1_texturePath = "texture/particle/test.png";
    private TextureRegion effect1_texture;


    public void effect1(float x, float y){
        Particle current;
            for(int i = 0; i < effect1_num; i++){
                if(unspawned.size() > 0) {
                    current = unspawned.getFirst();
                    unspawned.removeFirst();
                    current.spawn(x, y);
                    spawned.add(current);
                }
            }
    }




}
