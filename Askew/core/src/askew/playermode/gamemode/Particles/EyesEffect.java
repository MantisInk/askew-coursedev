package askew.playermode.gamemode.Particles;


import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.LinkedList;

public class EyesEffect extends Effect {

    private int effect1_num = 15;
    private int ct = 0;
    private int cap = 5;
    private final String frame0Path = "texture/particle/eyes0.png";
    private final String frame1Path = "texture/particle/eyes1.png";
    private final String frame2Path = "texture/particle/eyes2.png";
    private final String frame3Path = "texture/particle/eyes3.png";
    private int frameNum = 0;
    private final ArrayList<TextureRegion> eye_textures = new ArrayList<>();
    private TextureRegion tex;

    public EyesEffect(ParticleController pc, LinkedList<Particle> unspawned) {
        super(pc, unspawned); frameNum = 0; ct = 0;
    }

    public void spawn() {
        if(particleController.graphicsSetting == 0){
            effect1_num = 4;
        }
        else{
            effect1_num = 15;
        }
        Particle current;
        Rectangle b = particleController.gmc.getBounds();
        float bx = (b.x / 1.3f) - 16f;
        float by = (b.y / 1.3f);
        float bwidth = b.width * 1.3f;
        float bheight = b.height * 1.3f;



        for (int i = 0; i < effect1_num; i++) {
            if (unspawned.size() > 0 ) {
                current = unspawned.getFirst();
                unspawned.removeFirst();
                current.spawnEyes( (bx + ((float) (Math.random() * bwidth * 1.69f))),by + ((float) (Math.random()*bheight* 1.69f)));
                spawned.add(current);
                ct++;
            }
        }
    }

    @Override
    public void setTexture(MantisAssetManager manager) {
        if(eye_textures.size() == 0) {
            Texture tex = manager.get(frame0Path);
            eye_textures.add(new TextureRegion(tex));

            tex = manager.get(frame1Path);
            eye_textures.add(new TextureRegion(tex));

            tex = manager.get(frame2Path);
            eye_textures.add(new TextureRegion(tex));

            tex = manager.get(frame3Path);
            eye_textures.add(new TextureRegion(tex));
        }
    }

    @Override
    public void draw(GameCanvas canvas) {

        for (Particle p : spawned) {

            if (p.accumulator > 0) {
                if (eye_textures.get(p.textureNum) != null) {
                    TextureRegion tex = eye_textures.get(p.textureNum);
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
