package askew.playermode.gamemode.Particles;


import askew.MantisAssetManager;
import askew.playermode.gamemode.GameModeController;
import com.badlogic.gdx.math.Vector2;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;

@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
public class ParticleController {

    public final GameModeController gmc;
    private final LinkedList<Particle> unspawned = new LinkedList<>();
    public final ArrayList<Effect> effects = new ArrayList<>();
    private final Particle[] tempArray;
    public TestEffect testEffect;
    public FogEffect fogEffect;
    public HandTrailEffect handTrailEffect;
    public EyesEffect eyeEffect;
    @Setter
    public int graphicsSetting;

    @Setter
    Vector2 drawScale;


    public ParticleController(GameModeController gmc, int maxParticles) {
        this.gmc = gmc;

        this.setDrawScale(gmc.getWorldScale());
        tempArray = new Particle[maxParticles];
        testEffect = new TestEffect(this, unspawned);
        effects.add(testEffect);
        fogEffect = new FogEffect(this,unspawned);
        effects.add(fogEffect);
        handTrailEffect = new HandTrailEffect(this,unspawned);
        effects.add(handTrailEffect);
        eyeEffect = new EyesEffect(this, unspawned);
        effects.add(eyeEffect);
        for (int i = 0; i < maxParticles; ++i) {
            Particle particle = new Particle();
            unspawned.add(particle);
        }
    }


    public void reset() {
        for(Effect e : effects){
            e.reset();
        }
    }

    public void setTextures(MantisAssetManager manager) {
        for(Effect e : effects){
            e.setTexture(manager);
        }


    }

    public void update(float delta) {
        if (gmc.isPaused()) {
            return;
        }
        for(Effect e : effects){
            e.update(delta);
        }
    }


}
