package askew.playermode.gamemode;

import askew.GameCanvas;
import askew.MantisAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class VictoryCutscene {

    Animation happyFlowAnimation;
    Animation sadFlowAnimation;
    Animation happyEbbAnimation;

    private float elapseTime;
    private boolean didFirstFrame;
    private int frameNumber;
    private int previousFrame;

    private static final int CUTOFF_A = 32;
    private static final int CUTOFF_B = 64;

    public VictoryCutscene(MantisAssetManager manager) {
        happyFlowAnimation = new Animation(0.070f, manager.getTextureAtlas()
                .findRegions("happyswing"), Animation.PlayMode.NORMAL);
        happyFlowAnimation = new Animation(0.070f, manager.getTextureAtlas()
                .findRegions("sadswing"), Animation.PlayMode.NORMAL);
        happyEbbAnimation = new Animation(0.060f, manager.getTextureAtlas()
                .findRegions("ebbswing"), Animation.PlayMode.NORMAL);
        reset();
    }

    public void reset() {
        elapseTime = 0;
        frameNumber = 0;
        previousFrame = 0;
        didFirstFrame = false;
    }

    public void draw(GameCanvas canvas) {
        elapseTime += Gdx.graphics.getDeltaTime();

        if (elapseTime == 0) return;
        if (!didFirstFrame) {
            elapseTime = 0;
            didFirstFrame = true;
        }

        float cw = canvas.getWidth();
        float ch = canvas.getHeight();

        TextureRegion drawFrame;
        if (frameNumber < CUTOFF_A) {
            drawFrame = happyFlowAnimation.getKeyFrame(elapseTime, true);
            canvas.draw(drawFrame, Color.WHITE,(int)(cw/2) + (cw/6f), (int)(ch/4)
                    ,(int)
                            (cw/4f), (int)(ch/4f));

            drawFrame = happyEbbAnimation.getKeyFrame(elapseTime,
                    true);
            canvas.draw(drawFrame, Color.WHITE,(int)(cw/2),
                    (int)
                            (ch/4) + ((cw/4f)-(cw/6f))/2f,(int)
                            (cw/6f), (int)(ch/6f));
            int ebbFrame = happyEbbAnimation.getKeyFrameIndex(elapseTime);
            if (ebbFrame != previousFrame) frameNumber++;
            previousFrame = ebbFrame;
        } else if (frameNumber < CUTOFF_B) {

        } else {

        }

    }
}
