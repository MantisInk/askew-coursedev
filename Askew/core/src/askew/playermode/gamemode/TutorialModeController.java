/*
 * PlatformController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package askew.playermode.gamemode;

import askew.GlobalConfiguration;
import askew.InputController;
import askew.InputControllerManager;
import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.obstacle.Obstacle;
import askew.entity.tree.Trunk;
import askew.entity.vine.Vine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static askew.entity.sloth.SlothModel.*;

/**
 * Gameplay specific controller for the platformer game.
 * <p>
 * You will notice that asset loading is not grabbedAll with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
@SuppressWarnings("FieldCanBeLocal")
public class TutorialModeController extends GameModeController {

    private final int MAX_TUTORIAL;

    private final int STAGE_PINNED = 1;
    private final int STAGE_GRAB = 2;
    private final int STAGE_SHIMMY = 3;
    private final int STAGE_FLING = 4;
    private final int STAGE_VINE = 5;
    private int currentStage = STAGE_PINNED;
    private boolean next = false;

    private final float CONTROLLER_DEADZONE = 0.15f;

    private boolean pressedA = false;
    private boolean prevRightGrab;
    private boolean prevLeftGrab;
    private boolean grabbedAll;

    private float time = 0f;

    private Animation joystickAnimation;
    private Animation bumperLAnimation;
    private Animation bumperRAnimation;
    private float elapseTime;
    private float count;

    // selected animation textures to be drawn
    private TextureRegion joystickNeutralTexture;
    private TextureRegion joystickTexture;
    private TextureRegion bumperLTexture;
    private TextureRegion bumperRTexture;
    private Texture container;

    // list of entities for stage of tutorial
    private final ArrayList<Trunk> trunkEntities = new ArrayList<>();
    private final ArrayList<Boolean> trunkGrabbed = new ArrayList<>();
    private final ArrayList<Vine> vineEntities = new ArrayList<>();

    // margin allowance for measuring distance from setpoints
    private final float[] inRangeAllowance = {0.02f, 0.02f, 0.02f, ARMSPAN / 2, 0.05f};
    // list of setpoints for drawing helplines & other vars
    private int inRangeSetPt = -1;            // step progression within tutorial level
    private final int MINUS30 = -2;            // constant as signal for drawing help lines -30 degrees from moving arm
    private final int MINUS10 = -1;
    private final int NEUTRAL = 0;            // constant as signal for drawing help lines when moving arm close to target
    private final int PLUS10 = 1;
    private final int PLUS30 = 2;            // constant as signal for drawing help lines +30 degrees from moving arm
    private final int EAST = 3;
    private int targetLine = NEUTRAL;        // variable to store decision on what type of help line to draw
    private float angleDiff = 0f;            // keeps track of (arm angle minus target angle) for sloth to draw
    private boolean nextSetPt = false;
    private int ind = -1;
    private float omega = 0;
    private final float omega_0 = 0.15f;
    private boolean swing = false;
    private boolean back = false;
    private final float[] grabSetPoints = {14.019997f, 11.73999f, 9.399997f, 7.0199966f, 4.720001f};
    private final Vector2[] shimmySetPoints = {
            new Vector2(12f, 13f),
            new Vector2(12f, 9f),
            new Vector2(17f, 9f),
            new Vector2(17f, 14f),
            new Vector2(22.5f, 14f)};
    private final Vector2[] flingSetPoints = {
            new Vector2(2f, 14f),
            new Vector2(8f, 16f),
            new Vector2(14f, 14f),
            new Vector2(22f, 14f)};
    private final Vector2[] flingLandPoints0 = {
            new Vector2(-2f, 12f),
            new Vector2(4f, 14f),
            new Vector2(10f, 12f),
            new Vector2(18f, 11.5f)
    };
    private final Vector2[] flingLandPointsf = {
            new Vector2(5.5f, 16f),
            new Vector2(11.5f, 14f),
            new Vector2(19.5f, 14f),
            new Vector2(23f, 16f)
    };
    private final Vector2[] vineSetPoints = {
            new Vector2(4f, 13f),
            new Vector2(6f, 16.5f),
            new Vector2(9.6f, 12.7f),
            new Vector2(13f, 12f),        // vine
            new Vector2(16f, 12f),
            new Vector2(19f, 12f),        // vine
            new Vector2(23f, 12f),    // vine
            new Vector2(29f, 14f)        // owl
    };
    private final ArrayList<Integer> vineInds = new ArrayList<>(Arrays.asList(3, 5, 6));
    // list of instructions
    private final boolean[] shimmyGrabbed = {false, false, false, false, false};
    private final int[] shimmyDir = {SHIMMY_S, SHIMMY_E, SHIMMY_N, SHIMMY_E, SHIMMY_SE};
    private final boolean[] flingGrabbed = {false, false, false, false};
    private int[] flingDir = {SHIMMY_NE, SHIMMY_SE, SHIMMY_E, SHIMMY_SE};
    private final boolean[] vineGrabbed = {false, false, false, false, false, false, false, false};
    private final int[] vineDir = {SHIMMY_E, SHIMMY_SE, SHIMMY_E, SHIMMY_E, SHIMMY_N, SHIMMY_E, SHIMMY_E, SHIMMY_NE};

    /**
     * Load the assets for this controller.
     * <p>
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(MantisAssetManager manager) {
        super.loadContent(manager);
        container = manager.get("texture/tutorial/infoContainer.png");
        // reset animation frames
        if (joystickAnimation == null) {
            joystickAnimation = new Animation(0.15f, manager.getTextureAtlas().findRegions("joy"), Animation.PlayMode.LOOP);
            bumperLAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperL"), Animation.PlayMode.LOOP);
            bumperRAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperR"), Animation.PlayMode.LOOP);
        }
        DEFAULT_LEVEL = "tutorial0";
        loadLevel = DEFAULT_LEVEL;
    }

    // Physics entities for the game

    /**
     * Reference to the character avatar
     */

    public TutorialModeController() {
        super();
        currentStage = 0;
        trunkEntities.clear();
        MAX_TUTORIAL = GlobalConfiguration.getInstance().getAsInt("maxTutorial");
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        loadLevel = "tutorial" + currentStage;
        trunkEntities.clear();
        trunkGrabbed.clear();
        super.reset();
        time = 0;
        inRangeSetPt = -1;
        targetLine = NEUTRAL;
        angleDiff = 0f;
        nextSetPt = false;
        omega = 0;
        count = 0f;
        ind = -1;
        swing = false;
        back = false;
        for (int i = 0; i < shimmyGrabbed.length; i++)
            shimmyGrabbed[i] = false;
        for (int i = 0; i < flingGrabbed.length; i++)
            flingGrabbed[i] = false;
        for (int i = 0; i < vineGrabbed.length; i++)
            vineGrabbed[i] = false;

        joystickTexture = joystickAnimation.getKeyFrame(0);
        bumperLTexture = bumperLAnimation.getKeyFrame(0);
        bumperRTexture = bumperRAnimation.getKeyFrame(0);
    }

    /**
     * Lays out the game geography.
     */
    @Override
    protected void populateLevel() {
        super.populateLevel();
        for (Entity e : entities) {
            if (e instanceof Trunk) {
                trunkEntities.add((Trunk) e);
                trunkGrabbed.add(false);
            }
            if (e instanceof Vine) {
                vineEntities.add((Vine) e);
            }
        }

        if (currentStage == STAGE_PINNED) {
            slothList.get(0).pin(world);
            slothList.get(0).setPinned();
        }

    }

    /**
     * Returns whether to process the update loop
     * <p>
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }


        InputController input = InputControllerManager.getInstance().getController(0);
        if (next) {
            currentStage++;
            next = false;
            if (currentStage <= MAX_TUTORIAL) {
                System.out.println("moving on");
                listener.exitScreen(this, EXIT_TL_TL);
                return false;
            } else {
                System.out.println("tutorial completed");
                GlobalConfiguration.getInstance().setCurrentLevel(1);
                listener.exitScreen(this, EXIT_TL_GM);
                return false;
            }
        }
        pressedA = input.didBottomButtonPress();
        return true;
    }

    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class askew.playermode.WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate entities.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        super.update(dt);
        if (!paused) {
            elapseTime += dt;
            time = time + dt;
            // TODO: move sloth movement in slothmodel
            Vector2 set;

            switch (currentStage) {
                case STAGE_PINNED:
                    if ((int) (time / 3) % 2 == 0) {
                        slothList.get(0).getRightArm().setAngle((float) Math.PI);
                    } else {
                        slothList.get(0).getLeftArm().setAngle((float) Math.PI);
                    }
                    break;
                case STAGE_GRAB:
                    grabbedAll = trunkGrabbed.get(0);
                    for (Boolean aTrunkGrabbed : trunkGrabbed) {
                        grabbedAll = aTrunkGrabbed && grabbedAll;
                    }
                    if (inRangeSetPt + 1 >= grabSetPoints.length) {
                        break;
                    }
                    Vector2 setpt = new Vector2(slothList.get(0).getX(), grabSetPoints[inRangeSetPt + 1]);
                    if (inRange(setpt)) {
                        inRangeSetPt++;
                    }
                    break;
                case STAGE_SHIMMY:
                    if (inRangeSetPt + 1 >= shimmySetPoints.length) {
                        break;
                    }
                    if (inRange(shimmySetPoints[inRangeSetPt + 1])) {
                        inRangeSetPt++;
                    }
                    if (inRangeSetPt >= 0 && !shimmyGrabbed[inRangeSetPt]) {
                        shimmyGrabbed[inRangeSetPt] = checkGrabbedPt(shimmySetPoints[inRangeSetPt], shimmyDir[inRangeSetPt]);
                    }
                    break;
                case STAGE_FLING:
                    // if done with setpoints
//					System.out.println("\n progression "+inRangeSetPt);
                    if (inRangeSetPt + 1 >= flingSetPoints.length) {
                        angleDiff = 0f;
                        targetLine = NEUTRAL;
                        break;
                    }
                    set = flingSetPoints[inRangeSetPt + 1];
                    Vector2 backpt = flingLandPoints0[inRangeSetPt + 1];
                    Vector2 landpt = flingLandPointsf[inRangeSetPt + 1];
                    inRange(set);
                    if (inRangeSetPt + 2 < flingSetPoints.length && inRange(flingSetPoints[inRangeSetPt + 2])) {
                        inRangeSetPt++;
                    }
                    if (inRange(flingSetPoints[inRangeSetPt + 1])) {
                        // update omega
                        try {
                            omega = slothList.get(0).getMostRecentlyGrabbed().getLinearVelocity().len();
                        } catch (NullPointerException e) {
                            omega = 0;
                        }
                        // set new target to land0
//						System.out.println("swing "+swing);
//						System.out.print("\n  omega: "+omega);
//						System.out.println("   thresh: "+(omega>=omega_0));
                        if ((omega_0 - omega) < 0.05) {
//							System.out.print("   back "+back);
                            if (!back) {
                                inRange(backpt);
//								targetLine = MINUS30;
                                back = reachedBackPt(backpt);
                                count = 0;
                            } else {
                                inRange(landpt);
//								targetLine = NEUTRAL;
                                count++;
                                if (count > 120) {
                                    count = 0;
                                    back = false;
                                    swing = false;
                                }
                            }
                        }
                        // if enough momentum, set new target to landf
                        else {
                            swing = true;
                        }
                        if (inRange(landpt)) {
                            targetLine = NEUTRAL;
                            count++;
                            if (count > 120) {
                                count = 0;
                                back = false;
                                swing = false;
                            }
                            inRangeSetPt++;
                            break;
                        }
                    }
                    if (swing && flingGrabbed[inRangeSetPt + 1]) {
                        //reset
                        count = 0;
                        back = false;
                        swing = false;
                        inRangeSetPt++;
                    }
                    break;
                case STAGE_VINE:
//					System.out.print("\n progression "+inRangeSetPt);
                    if (inRangeSetPt == -1) {
                        targetLine = EAST;
                    }
                    if (inRangeSetPt + 1 > vineSetPoints.length) {
                        angleDiff = 0f;
                        targetLine = NEUTRAL;
                        break;
                    }
//					System.out.print("   targetLINE "+targetLine);
//					System.out.print("   setpt ");
//					printVector(vineSetPoints[inRangeSetPt+1]);
//					System.out.print("   grabpt ");
//					try {
//						printVector(sloth.getMostRecentlyGrabbed().getPosition());
//					} catch (NullPointerException e) {
//						printVector(new Vector2());
//					}
//					System.out.print("   inRange "+inRange(vineSetPoints[inRangeSetPt+1]));
                    if (inRange(vineSetPoints[inRangeSetPt + 1]) && vineGrabbed[inRangeSetPt + 1]) {
                        inRangeSetPt++;
                        if (!swing) {
                            ind = vineInds.indexOf(inRangeSetPt + 1);
                        }
                        if (nextSetPt) {
                            nextSetPt = false;
                            swing = false;
                        }
                    }
//					System.out.print("   ind "+ind);
                    if (ind != -1) {
                        swing = true;
//						ind = vineInds.indexOf(inRangeSetPt+1);
                        Vine v = vineEntities.get(ind);
                        vineSetPoints[inRangeSetPt + 1] = v.getEndpt().getPosition();
                        inRange(vineSetPoints[inRangeSetPt + 1]);
                        setTarget(vineEntities.get(ind));
                    }
//					System.out.print("   swing "+swing);
                    if (!vineGrabbed[inRangeSetPt + 1]) {
                        if (!swing) {
                            inRange(vineSetPoints[inRangeSetPt + 1]);
                        }
                        vineGrabbed[inRangeSetPt + 1] = checkGrabbedPt(vineSetPoints[inRangeSetPt + 1], vineDir[inRangeSetPt + 1]);
                        if (vineGrabbed[inRangeSetPt + 1]) {
                            nextSetPt = true;
                        }
                    }
                    break;
                default:
                    System.err.println(currentStage);
            }
            if (moveToNextStage())
                next = true;
            prevLeftGrab = slothList.get(0).isActualLeftGrab();
            prevRightGrab = slothList.get(0).isActualRightGrab();
        }
    }

    private boolean checkGrabbedPt(Vector2 setpt, int dir) {
//		System.out.print("   setpt: ("+setpt.x+","+setpt.y+")   ");
        Body rTarget, lTarget, tTarget, bTarget;
        Vector2 rtPos, ltPos, ttPos, btPos;
        boolean xrange = false;
        boolean yrange = false;
        float other = (currentStage == STAGE_VINE) ? 0.6f : 0.05f;
//		System.out.print("  dir "+dir);
        if (dir == SHIMMY_E || dir == SHIMMY_SE || dir == SHIMMY_NE) {
            rTarget = slothList.get(0).getRightmostTarget();
            if (rTarget == null) {
                return false;
            }
            rtPos = rTarget.getPosition();
//			System.out.print("   E: ("+rtPos.x+","+rtPos.y+")");

            if (rtPos.x - 0.05 >= setpt.x) {
                xrange = true;
            }
            if (dir == SHIMMY_E && Math.abs(setpt.y - rtPos.y) <= other) {
//				System.out.print("  setpt.y "+setpt.y+"   pos.y "+rtPos.y);
                yrange = true;
            }

        } else if (dir == SHIMMY_W || dir == SHIMMY_SW || dir == SHIMMY_NW) {
            lTarget = slothList.get(0).getLeftmostTarget();
            if (lTarget == null) {
                return false;
            }
            ltPos = lTarget.getPosition();
//			System.out.print("   W: ("+ltPos.x+","+ltPos.y+")");

            if (ltPos.x + 0.05 <= setpt.x) {
                xrange = true;
            }
            if (dir == SHIMMY_W && Math.abs(setpt.y - ltPos.y) <= other) {
//				System.out.print("  setpt.y "+setpt.y+"   pos.y "+ltPos.y);
                yrange = true;
            }

        } else if (dir == SHIMMY_S || dir == SHIMMY_SE || dir == SHIMMY_SW) {
            bTarget = slothList.get(0).getBottomTarget();

            if (bTarget == null) {
                return false;
            }
            btPos = bTarget.getPosition();
//			System.out.print("   S: ("+btPos.x+","+btPos.y+")");

            if (btPos.y + 0.05f <= setpt.y) {
                yrange = true;
            }
            if (dir == SHIMMY_S && Math.abs(setpt.x - btPos.x) <= other) {
                xrange = true;
            }

        } else if (dir == SHIMMY_N || dir == SHIMMY_NE || dir == SHIMMY_NW) {
            tTarget = slothList.get(0).getTopTarget();
            if (tTarget == null) {
                return false;
            }
            ttPos = tTarget.getPosition();
//			System.out.print("    N: ("+ttPos.x+","+ttPos.y+")");

            if (ttPos.y - 0.05 >= setpt.y) {
                yrange = true;
            }
            if (dir == SHIMMY_N && Math.abs(setpt.x - ttPos.x) <= other) {
                xrange = true;
            }
        }
//		System.out.print("  xrange "+xrange+"   yrange "+yrange);
        return xrange && yrange;
    }

    private boolean reachedBackPt(Vector2 backpt) {
        try {
            Vector2 handPos = slothList.get(0).getMostRecentlyGrabbed().getPosition();
            Vector2 diff = handPos.cpy().sub(backpt);
//			System.out.print("   len "+diff.len());
//			System.out.print("   backpt: "); printVector(backpt);
//			System.out.print("   hand: "); printVector(handPos);
            return diff.len() < ARMSPAN;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean inRange(Vector2 setpt) {
        return inRange(setpt, inRangeAllowance[currentStage - 1]);
    }

    // checks if next set point is in range for changing arm help
    private boolean inRange(Vector2 setpt, float allowance) {
        Body lTarget = slothList.get(0).getLeftTarget();
        Body rTarget = slothList.get(0).getRightTarget();
        Body lHand = slothList.get(0).getLeftHand();
        Body rHand = slothList.get(0).getRightHand();

        Vector2 lhPos = lHand.getPosition();
        Vector2 rhPos = rHand.getPosition();
        Vector2 grabPos = new Vector2();

        if (lTarget == null && rTarget == null) {
            return false;
        }
        Vector2 lPos, rPos;
        boolean xrange = false;
        boolean yrange = false;
        float tAngle = 0f;
        float aAngle = 0f;
        float diff;
        if (lTarget != null && rTarget != null) {
            // if both hands grabbing
            lPos = lTarget.getPosition();
            rPos = rTarget.getPosition();
            if (lPos.x > rPos.x) {
                // move rh
                tAngle = (setpt.cpy().sub(rPos).angle() + 360) % 360;
                aAngle = (lhPos.cpy().sub(rhPos).angle() + 360) % 360;

                xrange = Math.abs(setpt.x - lPos.x) <= ARMSPAN + allowance;
                yrange = Math.abs(setpt.y - lPos.y) <= ARMSPAN + allowance;
                grabPos = lPos;
            } else {
                // move lh
                tAngle = (setpt.cpy().sub(lPos).angle() + 360) % 360;
                aAngle = (rhPos.cpy().sub(lhPos).angle() + 360) % 360;

                xrange = Math.abs(setpt.x - rPos.x) <= ARMSPAN + allowance;
                yrange = Math.abs(setpt.y - rPos.y) <= ARMSPAN + allowance;
                grabPos = rPos;
            }
        }
        if (lTarget != null) {
            // move lh
            lPos = lTarget.getPosition();
            tAngle = (setpt.cpy().sub(lPos).angle() + 360) % 360;
            aAngle = (rhPos.cpy().sub(lhPos).angle() + 360) % 360;

            xrange = Math.abs(setpt.x - lPos.x) <= ARMSPAN + allowance;
            yrange = Math.abs(setpt.y - lPos.y) <= ARMSPAN + allowance;
            grabPos = lPos;
        }
        if (rTarget != null) {
            // move rh
            rPos = rTarget.getPosition();
            tAngle = (setpt.cpy().sub(rPos).angle() + 360) % 360;
            aAngle = (lhPos.cpy().sub(rhPos).angle() + 360) % 360;

            xrange = Math.abs(setpt.x - rPos.x) <= ARMSPAN + allowance;
            yrange = Math.abs(setpt.y - rPos.y) <= ARMSPAN + allowance;
            grabPos = rPos;
        }

        diff = (((aAngle - tAngle) % 360) + 360) % 360;
        if (30 < diff && diff < 180) {
            targetLine = MINUS30;
        } else if (180 <= diff && diff < 330) {
            targetLine = PLUS30;
        } else {
            targetLine = NEUTRAL;
        }
//		System.out.println("aAngle "+aAngle+"  tAngle "+tAngle);
        angleDiff = diff;
        checkCloseToCorner(setpt, grabPos);
        return xrange && yrange;
    }

    private void setTarget(Vine v) {
        Obstacle sHand = slothList.get(0).getMostRecentlyGrabbed();
        if (sHand == null) {
            return;
        }
        Body hand = slothList.get(0).getMostRecentlyGrabbed().getBody();
        Body otherHand = (hand == slothList.get(0).getRightHand()) ? slothList.get(0).getLeftHand() : slothList.get(0).getRightHand();
        float aAngle = otherHand.getPosition().sub(hand.getPosition()).angle();
        float vAngle = v.getEndpt().getPosition().sub(v.getPosition()).angle();
        angleDiff = (((aAngle - vAngle) % 360) + 360) % 360;
        float lv = v.getEndpt().getLinearVelocity().x;
//		System.out.print("   lv "+lv);
        if (10 < angleDiff && angleDiff < 350) {
            if (lv < -0.05f) {
                targetLine = MINUS10;
            } else if (lv > 0.05f) {
                targetLine = PLUS10;
            } else {
                targetLine = NEUTRAL;
            }
        } else {
            targetLine = NEUTRAL;
        }

    }

    private void checkCloseToCorner(Vector2 setpt, Vector2 grabpt) {
        float xrange = Math.abs(setpt.x - grabpt.x);
        float yrange = Math.abs(setpt.y - grabpt.y);
        if (xrange < 0.08 && yrange < 0.08) {
            shimmyGrabbed[inRangeSetPt + 1] = true;
        }
    }

    private boolean moveToNextStage() {
        if (currentStage == STAGE_PINNED) {
            return (time > 1.5f && pressedA);
        } else if (currentStage == STAGE_GRAB) {
            return (grabbedAll && pressedA);
        } else if (currentStage > STAGE_PINNED) {
            return owl.isDoingVictory();
        }
        return false;
    }

    private void drawHelpLines() {
        switch (currentStage) {
            case STAGE_PINNED:
                break;
            case STAGE_GRAB:
                if (!grabbedAll)
                    slothList.get(0).drawHelpLines(canvas, camTrans, SHIMMY_S, 0f);
                break;
            case STAGE_SHIMMY:
            case STAGE_FLING:
            case STAGE_VINE:
                switch (targetLine) {
                    case PLUS30:
                        slothList.get(0).drawHelpLines(canvas, camTrans, PLUS_30, 0f);
                        break;
                    case MINUS30:
                        slothList.get(0).drawHelpLines(canvas, camTrans, MINUS_30, 0f);
                        break;
                    case PLUS10:
                        slothList.get(0).drawHelpLines(canvas, camTrans, PLUS_10, 0f);
                        break;
                    case MINUS10:
                        slothList.get(0).drawHelpLines(canvas, camTrans, MINUS_10, 0f);
                        break;
                    case NEUTRAL:
                        slothList.get(0).drawHelpLines(canvas, camTrans, DEFAULT, angleDiff);
                        break;
                    case EAST:
                        slothList.get(0).drawHelpLines(canvas, camTrans, SHIMMY_E, 0f);
                        break;
                }
                break;
        }

    }

    public void drawInstructions() {
        joystickNeutralTexture = joystickAnimation.getKeyFrame(0);
        joystickTexture = joystickAnimation.getKeyFrame(elapseTime, true);
        bumperLTexture = bumperLAnimation.getKeyFrame(elapseTime, true);
        bumperRTexture = bumperRAnimation.getKeyFrame(elapseTime, true);

        canvas.draw(container, Color.WHITE, container.getWidth() / 2, 0, 425, 300, 0, worldScale.x * 5 / container.getWidth(), worldScale.y * 5 / container.getHeight());
        if (currentStage == STAGE_PINNED) {
            if ((int) (time / 3) % 2 == 0) {
                canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
                canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
            } else {
                canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
                canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
            }
        } else if (currentStage == STAGE_GRAB) {
            if (!grabbedAll) {
                canvas.drawTextCentered("Try to grab all 5 branches", displayFont, 200f);
            }
        } else if (currentStage == STAGE_SHIMMY) {
            canvas.drawTextCentered("Try to shimmy across", displayFont, 200f);
        }
        if (currentStage >= STAGE_GRAB && currentStage < STAGE_VINE) {
            if (slothList.get(0).isActualRightGrab()) {
                canvas.draw(bumperRTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
                canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
                canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
            } else {
                canvas.draw(bumperLTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
                canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
                canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
            }
//		} else if (currentStage == MOVED_LEFT) {
//		} else if (currentStage == MOVED_RIGHT) {
//			canvas.draw(bumperLTexture, Color.WHITE, bumperLTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperLTexture.getRegionWidth(), worldScale.y * 3 / bumperLTexture.getRegionHeight());
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//		} else if (currentStage == GRABBED_LEFT) {
//			canvas.draw(bumperRTexture, Color.WHITE, bumperRTexture.getRegionWidth() / 2, 0, 400, 400, 0, worldScale.x * 3 / bumperRTexture.getRegionWidth(), worldScale.y * 3 / bumperRTexture.getRegionHeight());
//			canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
//			canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
//		} else if (currentStage >= GRABBED_RIGHT) {
        }
        if ((currentStage == STAGE_PINNED && time > 6f) ||
                (currentStage == STAGE_GRAB && grabbedAll)) {
            canvas.drawTextCentered("Press A to continue", displayFont, 200f);
        }
    }

    public void draw(float delta) {
        // GameMode draw with changes
        canvas.clear();

        canvas.begin();
        canvas.draw(background);
        canvas.end();

        camTrans.setToTranslation(-1 * slothList.get(0).getBody().getPosition().x * worldScale.x
                , -1 * slothList.get(0).getBody().getPosition().y * worldScale.y);
        camTrans.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.getCampos().set(slothList.get(0).getBody().getPosition().x * worldScale.x
                , slothList.get(0).getBody().getPosition().y * worldScale.y);

        canvas.begin(camTrans);
        //noinspection unchecked
        Collections.sort(entities);
        slothList.get(0).setTutorial();
        for (Entity obj : entities) {
            obj.setDrawScale(worldScale);
            // if stage 2, tint trunks if already grabbed
            if (currentStage == STAGE_GRAB && obj instanceof Trunk) {
                Trunk trunk = (Trunk) obj;
                int ind = trunkEntities.indexOf(obj);

                for (Obstacle plank : trunk.getBodies()) {
                    if (plank.getBody().getUserData() instanceof Obstacle && ((Obstacle) plank.getBody().getUserData()).isGrabbed()) {
                        trunkGrabbed.set(ind, true);
                    }
                }
            } else {
                obj.draw(canvas);
            }
        }

        // trunk tinting done here
        for (int i = 0; i < trunkEntities.size(); i++) {
            if (trunkGrabbed.get(i)) {
                (trunkEntities.get(i)).draw(canvas, Color.GRAY);
            } else {
                trunkEntities.get(i).draw(canvas);
            }
        }

        if (!playerIsReady && !paused && coverOpacity <= 0)
            printHelp();
        canvas.end();
        slothList.get(0).drawGrab(canvas, camTrans);

        drawHelpLines();

//		sloth.drawHelpLines();

        if (debug) {
            canvas.beginDebug(camTrans);
            entities.stream().filter(obj -> obj instanceof Obstacle).forEachOrdered(obj -> ((Obstacle) obj).drawDebug(canvas));
            canvas.endDebug();
            canvas.begin();
            // text
            canvas.drawTextStandard("FPS: " + 1f / delta, 10.0f, 100.0f);
            canvas.end();
            slothList.get(0).drawForces(canvas, camTrans);
        }

        // draw instructional animations
//		canvas.begin();
//		drawInstructions();
//		canvas.end();

        if (coverOpacity > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            displayFont.setColor(Color.WHITE);
            Color coverColor = new Color(0, 0, 0, coverOpacity);
            canvas.drawRectangle(coverColor, 0, 0, canvas.getWidth(), canvas
                    .getHeight());
            coverOpacity -= (1 / CYCLES_OF_INTRO);
            Gdx.gl.glDisable(GL20.GL_BLEND);
            canvas.begin();
            if (!playerIsReady && !paused)
                canvas.drawTextCentered(levelModel.getTitle(), displayFont, 0f);
            canvas.end();
        }

        // draw pause menu stuff over everything
        if (paused) {
            canvas.begin();
            canvas.draw(pauseTexture);
            canvas.draw(fern, Color.WHITE, fern.getWidth() / 2, fern.getHeight() / 2,
                    pause_locs[pause_mode].x * canvas.getWidth(), pause_locs[pause_mode].y * canvas.getHeight(),
                    0, 2 * worldScale.x / fern.getWidth(), 2 * worldScale.y / fern.getHeight());
            canvas.end();
        }

    }

    public void restart() {
        //change back to 1
        currentStage = 1;
    }

    public void printVector(Vector2 v) {
        System.out.print("(" + v.x + "," + v.y + ")");
    }

}
