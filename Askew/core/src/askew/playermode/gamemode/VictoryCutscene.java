package askew.playermode.gamemode;

import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class VictoryCutscene {

    Animation cutsceneAnimation;

    private float elapseTime;
    private boolean didFirstFrame;

    public VictoryCutscene(MantisAssetManager manager) {
        cutsceneAnimation = new Animation(0.090f, manager.getTextureAtlas()
                .findRegions("cutscene"), Animation.PlayMode.NORMAL);
        if (cutsceneAnimation.getKeyFrames().length == 0) System.err.println
                ("did not find cutscene");
        reset();
    }

    public void reset() {
        elapseTime = 0;
        didFirstFrame = false;
    }

    public void draw(GameCanvas canvas) {
        elapseTime += Gdx.graphics.getDeltaTime();

        if (elapseTime == 0) return;
        if (!didFirstFrame) {
            elapseTime = 0;
            didFirstFrame = true;
        }

        TextureRegion drawFrame = cutsceneAnimation.getKeyFrame(elapseTime,
                false);
        canvas.draw(drawFrame, Color.WHITE,canvas.getWidth()/2,(int)(1.0*canvas
                        .getHeight()/4.0),canvas
                        .getWidth()/2,
                canvas
                .getHeight()/2);
    }
}
