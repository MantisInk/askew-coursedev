package askew.playermode.gamemode.Particles;

import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.LinkedList;


public class FogEffect extends Effect {

    private final int fog_num = 4;
    private final String fog_texturePath1 = "texture/particle/cloud.png";
    private final String fog_texturePath2 = "texture/particle/cloud1.png";
    private final ArrayList<TextureRegion> fog_textures = new ArrayList<>();

    public FogEffect(ParticleController pc, LinkedList<Particle> unspawned) {
        super(pc, unspawned);
    }

    public void spawn(float x, float y){
        Particle current;
        for (int i = 0; i < fog_num; i++) {
            if (unspawned.size() > 0) {
                current = unspawned.getFirst();
                unspawned.removeFirst();
                current.spawnFog(x, y, particleController.gmc.getBounds().getWidth(), particleController.gmc.getBounds().getHeight());
                spawned.add(current);
            }
        }


    }

    @Override
    public void setTexture(MantisAssetManager manager) {
        if(fog_textures.size() == 0) {
            Texture tex = manager.get(fog_texturePath1);
            fog_textures.add(new TextureRegion(tex));

            tex = manager.get(fog_texturePath2);
            fog_textures.add(new TextureRegion(tex));
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);
        for (Particle p : spawned) {
            if (p.accumulator > 0) {
                if (fog_textures.get(p.textureNum) != null) {
                    TextureRegion tex = fog_textures.get(p.textureNum);
                    canvas.drawBackgroundEntity(tex,
                            p.tint,
                            tex.getRegionWidth() / 2,
                            tex.getRegionHeight() / 2,
                            p.x * drawScale.x, p.y * drawScale.y, p.depth, p.angle,
                            (1.0f / tex.getRegionWidth()) * p.width * drawScale.x, (1.0f / tex.getRegionHeight() * p.height * drawScale.y),
                            1);
                }

            }
        }
    }

}
