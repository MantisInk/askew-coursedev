package askew.playermode.gamemode.Particles;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.LinkedList;

public class TestEffect extends Effect {

    private final int effect1_num = 15;
    private final String effect1_texturePath = "texture/particle/test.png";
    private TextureRegion effect1_texture;

    public TestEffect(ParticleController pc, LinkedList<Particle> unspawned) {
        super(pc, unspawned);
    }

    public void spawn(float x, float y) {
        Particle current;
        for (int i = 0; i < effect1_num; i++) {
            if (unspawned.size() > 0) {
                current = unspawned.getFirst();
                unspawned.removeFirst();
                current.spawnDefault(x, y);
                spawned.add(current);
            }
        }
    }

    @Override
    public void setTexture(MantisAssetManager manager) {
        Texture tex = manager.get(effect1_texturePath);
        effect1_texture = new TextureRegion(tex);
    }

    @Override
    public void draw(GameCanvas canvas) {
        for (Particle p : spawned) {
            if (p.accumulator > 0) {
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
}
