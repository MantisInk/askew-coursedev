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
    Animation owlAnimation;

    private float elapseTime;
    private boolean didFirstFrame;
    private int state;

    private static final float[] owlposx = {
            -0.10f, 0.00f, 0.07f, .16f, .28f,
            .32f,.35f,.42f,.45f,.50f,
            .57f,.70f,.75f,.80f,.85f,.90f,.95f,1f
    };
    private static final float[] owlposy = {
            0.3f, 0.3f, 0.3f, 0.3f, 0.3f,
            0.3f, 0.3f, 0.3f, 0.32f, 0.33f,
            .28f,.29f,.30f,.3f,.3f
            ,.3f,.3f,.3f,.3f,.3f,.3f,.3f
    };
    private float flowElapseTime;

    public VictoryCutscene(MantisAssetManager manager) {
        happyFlowAnimation = new Animation(0.070f, manager.getTextureAtlas()
                .findRegions("happyswing"), Animation.PlayMode.NORMAL);
        sadFlowAnimation = new Animation(0.070f, manager.getTextureAtlas()
                .findRegions("sadswing"), Animation.PlayMode.NORMAL);
        happyEbbAnimation = new Animation(0.080f, manager.getTextureAtlas()
                .findRegions("ebbswing"), Animation.PlayMode.NORMAL);
        owlAnimation = new Animation(0.080f, manager.getTextureAtlas()
                .findRegions("owlcatchomg"), Animation.PlayMode.NORMAL);
        reset();
    }

    public void reset() {
        elapseTime = 0;
        state = 0;
        flowElapseTime = 0;
        didFirstFrame = false;
    }

    public void draw(GameCanvas canvas) {
        elapseTime += Gdx.graphics.getDeltaTime();
        flowElapseTime += Gdx.graphics.getDeltaTime();

        if (elapseTime == 0) return;
        if (!didFirstFrame) {
            elapseTime = 0;
            didFirstFrame = true;
        }

        float cw = canvas.getWidth();
        float ch = canvas.getHeight();

        TextureRegion drawFrame;
        if (state < 1) {
            if (happyEbbAnimation.isAnimationFinished(elapseTime)) {
                state++;
                elapseTime = 0;
            } else {
                // flow
                drawFrame = happyFlowAnimation.getKeyFrame(flowElapseTime, true);
                canvas.draw(drawFrame, Color.WHITE, (int) (.75f*cw), (int) (ch / 4), (int) (cw / 3f), (int) (ch / 3f));

                // ebb
                drawFrame = happyEbbAnimation.getKeyFrame(elapseTime,
                        true);
                canvas.draw(drawFrame, Color.WHITE, (int) (.60f*cw),
                        (int)
                                (ch / 4) + ((cw / 3f) - (cw / 4f)) / 2f, (int)
                                (cw / 4f), (int) (ch / 4f));
            }
        }
        if (state == 1) {
            if (owlAnimation.isAnimationFinished(elapseTime)) {
                state++;
                elapseTime = 0;
            } else {
                int owlFrame = owlAnimation.getKeyFrameIndex(elapseTime);
                // owl
                drawFrame = owlAnimation.getKeyFrame(elapseTime,
                        false);
                canvas.draw(drawFrame, Color.WHITE,
                        (int) (owlposx[owlFrame]*cw),
                        (int) (owlposy[owlFrame]*ch),
                        (int) (cw / 3f),
                        (int) (ch / 1.5f)
                );

                if (owlFrame < 10) {
                    // ebb not rekt
                    // flow
                    drawFrame = happyFlowAnimation.getKeyFrame(flowElapseTime, true);
                    canvas.draw(drawFrame, Color.WHITE, (int) (.75f*cw), (int) (ch / 4), (int) (cw / 3f), (int) (ch / 3f));

                    // ebb
                    drawFrame = happyEbbAnimation.getKeyFrame(elapseTime,
                            true);
                    canvas.draw(drawFrame, Color.WHITE, (int) (.60f*cw),
                            (int)
                                    (ch / 4) + ((cw / 3f) - (cw / 4f)) / 2f, (int)
                                    (cw / 4f), (int) (ch / 4f));

                } else {
                    // ebb fkn rekt
                    // flow sad af
                    drawFrame = sadFlowAnimation.getKeyFrame(flowElapseTime, true);
                    canvas.draw(drawFrame, Color.WHITE, (int) (.75f*cw), (int) (ch / 4), (int) (cw / 3f), (int) (ch / 3f));
                }

            }
        }
        if (state == 2) {
            // flow lonely af
            drawFrame = sadFlowAnimation.getKeyFrame(flowElapseTime, true);
            canvas.draw(drawFrame, Color.WHITE, (int) (.75f*cw), (int) (ch / 4), (int) (cw / 3f), (int) (ch / 3f));
        }
    }
}
