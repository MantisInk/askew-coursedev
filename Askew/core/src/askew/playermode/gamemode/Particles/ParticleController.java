package askew.playermode.gamemode.Particles;


import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.playermode.gamemode.GameModeController;
import askew.playermode.gamemode.Particles.Particle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
public class ParticleController {

    private final GameModeController gmc;
    private final LinkedList<Particle> unspawned = new LinkedList<>();
    private final LinkedList<Particle> spawned = new LinkedList<>();
    private final Particle[] tempArray;
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
        //noinspection unchecked
        Collections.sort(spawned);
        return spawned.toArray(tempArray);
    }

    public int numParticles(){
        return spawned.size();
    }

    public void reset(){
        int counter = 0;
        while (counter < spawned.size()){
            Particle p = spawned.getFirst();
            unspawned.add(p);
            spawned.removeFirst();
        }
    }

    public void setTextures(MantisAssetManager manager){
        Texture tex = manager.get(effect1_texturePath);
        effect1_texture = new TextureRegion(tex);

        tex = manager.get(fog_texturePath1);
        fog_textures.add(new TextureRegion(tex));

        tex = manager.get(fog_texturePath2);
        fog_textures.add(new TextureRegion(tex));


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
            if (p.type == 0) {
                if (effect1_texture != null) {
                    TextureRegion tex = effect1_texture;
                    canvas.drawBackgroundEntity(tex, p.tint, tex.getRegionWidth() / 2, tex.getRegionHeight() / 2,
                            p.x * drawScale.x, p.y * drawScale.y, p.depth, 0,
                            (1.0f / tex.getRegionWidth()) * p.width * drawScale.x, (1.0f / tex.getRegionHeight() * p.height * drawScale.y),
                            1);
                }
            }
            else if(p.type == 1){
                if(fog_textures.get(p.textureNum) != null) {
                    TextureRegion tex = fog_textures.get(p.textureNum);
                    canvas.drawBackgroundEntity(tex, p.tint, tex.getRegionWidth() / 2, tex.getRegionHeight() / 2,
                            p.x * drawScale.x, p.y * drawScale.y, p.depth, p.angle,
                            (1.0f / tex.getRegionWidth()) * p.width * drawScale.x, (1.0f / tex.getRegionHeight() * p.height * drawScale.y),
                            1);
                }
            }
        }
    }


    private final int effect1_num = 15;
    private final String effect1_texturePath = "texture/particle/test.png";
    private TextureRegion effect1_texture;


    public void effect1(float x, float y){
        Particle current;
            for(int i = 0; i < effect1_num; i++){
                if(unspawned.size() > 0) {
                    current = unspawned.getFirst();
                    unspawned.removeFirst();
                    current.spawnDefault( x, y);
                    spawned.add(current);
                }
            }
    }

    private final int fog_num = 3;
    private final String fog_texturePath1 = "texture/particle/cloud.png";
    private final String fog_texturePath2 = "texture/particle/cloud1.png";
    private final ArrayList<TextureRegion> fog_textures = new ArrayList<>();



    public void fog(float x, float y){
        Particle current;
        for(int i = 0; i < fog_num; i++){
            if(unspawned.size() > 0) {
                current = unspawned.getFirst();
                unspawned.removeFirst();
                current.spawnFog( x, y, gmc.getBounds().getWidth(), gmc.getBounds().getHeight());
                spawned.add(current);
            }
        }

    }




}
