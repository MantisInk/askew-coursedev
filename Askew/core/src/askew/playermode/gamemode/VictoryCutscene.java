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
    private int state;

    private static final float[] owlposx = {0.1f, 0.2f, 0.3f};
    private static final float[] owlposy = {0.1f, 0.2f, 0.3f};

    public VictoryCutscene(MantisAssetManager manager) {
        happyFlowAnimation = new Animation(0.070f, manager.getTextureAtlas()
                .findRegions("happyswing"), Animation.PlayMode.NORMAL);
        sadFlowAnimation = new Animation(0.070f, manager.getTextureAtlas()
                .findRegions("sadswing"), Animation.PlayMode.NORMAL);
        happyEbbAnimation = new Animation(0.060f, manager.getTextureAtlas()
                .findRegions("ebbswing"), Animation.PlayMode.NORMAL);
        reset();
    }

    public void reset() {
        elapseTime = 0;
        state = 0;
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
        if (state < 2) {
            // flow
            drawFrame = happyFlowAnimation.getKeyFrame(elapseTime, true);
            canvas.draw(drawFrame, Color.WHITE, (int) (cw / 2) + (cw / 6f), (int) (ch / 4)
                    , (int)
                            (cw / 3f), (int) (ch / 3f));

            // ebb
            drawFrame = happyEbbAnimation.getKeyFrame(elapseTime,
                    true);
            canvas.draw(drawFrame, Color.WHITE, (int) (cw / 2),
                    (int)
                            (ch / 4) + ((cw / 3f) - (cw / 4f)) / 2f, (int)
                            (cw / 4f), (int) (ch / 4f));

            if (happyEbbAnimation.isAnimationFinished(elapseTime)) {
                state++;
                elapseTime = 0;
            }
        }
        if (state == 2) {
            // flow
            drawFrame = sadFlowAnimation.getKeyFrame(elapseTime, true);
            canvas.draw(drawFrame, Color.WHITE, (int) (cw / 2) + (cw / 6f), (int) (ch / 4)
                    , (int)
                            (cw / 3f), (int) (ch / 3f));

            // ebb
            drawFrame = happyEbbAnimation.getKeyFrame(elapseTime,
                    true);
            canvas.draw(drawFrame, Color.WHITE, (int) (cw / 2),
                    (int)
                            (ch / 4) + ((cw / 3f) - (cw / 4f)) / 2f, (int)
                            (cw / 4f), (int) (ch / 4f));

            if (happyEbbAnimation.isAnimationFinished(elapseTime)) {
                state++;
                elapseTime = 0;
            }
        } else {
            // flow
            drawFrame = happyFlowAnimation.getKeyFrame(elapseTime, true);
            canvas.draw(drawFrame, Color.WHITE, (int) (cw / 2) + (cw / 6f), (int) (ch / 4)
                    , (int)
                            (cw / 3f), (int) (ch / 3f));

            // ebb
            drawFrame = happyEbbAnimation.getKeyFrame(elapseTime,
                    true);
            canvas.draw(drawFrame, Color.WHITE, (int) (cw / 2),
                    (int)
                            (ch / 4) + ((cw / 3f) - (cw / 4f)) / 2f, (int)
                            (cw / 4f), (int) (ch / 4f));

            if (happyEbbAnimation.isAnimationFinished(elapseTime)) state++;
        }
    }
}
