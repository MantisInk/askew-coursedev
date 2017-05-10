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
import askew.entity.FilterGroup;
import askew.entity.obstacle.Obstacle;
import askew.entity.owl.OwlModel;
import askew.entity.sloth.SlothModel;
import askew.entity.tree.Trunk;
import askew.entity.vine.Vine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import lombok.Getter;

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

	private int MAX_TUTORIAL;

	private final int STAGE_PINNED = 1;
	private final int STAGE_GRAB = 2;
	private final int STAGE_EBB = 3;
	private final int STAGE_SHIMMY = 4;
	private final int STAGE_FLING = 5;
	private final int STAGE_VINE = 6;
	@Getter
	private int currentStage = STAGE_PINNED;
	private boolean next = false;

	private final float CONTROLLER_DEADZONE = 0.15f;

	private boolean pressedA = false;
	private boolean prevRightGrab;
	private boolean prevLeftGrab;
	private boolean grabbedAll;
	private boolean moveLeftArm = false;

	private float time = 0f;

	private Animation joystickAnimation;
	private Animation bumperLAnimation;
	private Animation bumperRAnimation;
	private float elapseTime;
	private float count;

	BitmapFont instrFont;

	// selected animation textures to be drawn
	TextureRegion joystickNeutralTexture;
	TextureRegion joystickTexture;
	TextureRegion bumperLTexture;
	TextureRegion bumperRTexture;

	private String lPressedPath = "texture/background/tutorial/lclicked.png";
	private String rPressedPath = "texture/background/tutorial/rclicked.png";
	private String lUpPath = "texture/background/tutorial/lup.png";
	private String rUpPath = "texture/background/tutorial/rup.png";;
	Texture lPressed;
	Texture rPressed;
	Texture lUp;
	Texture rUp;
	Texture holdUp;
	private String holdUpPath = "texture/background/tutorial/holdUp.png";
	private String holdDownPath = "texture/background/tutorial/holdDown.png";
	private String holdLeftPath = "texture/background/tutorial/holdLeft.png";
	private String holdRightPath = "texture/background/tutorial/holdRight.png";
	private String swing0Path = "texture/background/tutorial/swing0.png";
	private String swing1Path = "texture/background/tutorial/swing1.png";
	private String swing2Path = "texture/background/tutorial/swing2.png";
	private String swing3Path = "texture/background/tutorial/swing3.png";
	Texture holdDown;
	Texture holdLeft;
	Texture holdRight;
	Texture swing0;
	Texture swing1;
	Texture swing2;
	Texture swing3;

	// list of objects for stage of tutorial
	private ArrayList<Boolean> trunkGrabbed = new ArrayList<Boolean>();
	protected ArrayList<Trunk> trunkEntities = new ArrayList<Trunk>();
	protected  ArrayList<Vine> vineEntities = new ArrayList<Vine>();

	// stuff for vector prediction
	// margin allowance for measuring distance from setpoints
	private float[] inRangeAllowance = {0.02f, 0.02f, 0.02f, ARMSPAN/2, 0.05f};
	// list of setpoints for drawing helplines & other vars
	private int inRangeSetPt = -1;			// step progression within tutorial level
	private final int MINUS30 = -2;			// constant as signal for drawing help lines -30 degrees from moving arm
	private final int MINUS10 = -1;
	private final int NEUTRAL = 0;			// constant as signal for drawing help lines when moving arm close to target
	private final int PLUS10 = 1;
	private final int PLUS30 = 2;			// constant as signal for drawing help lines +30 degrees from moving arm
	private final int EAST = 3;
	private int targetLine = NEUTRAL;		// variable to store decision on what type of help line to draw
	private float angleDiff = 0f; 			// keeps track of (arm angle minus target angle) for sloth to draw
	private boolean nextSetPt = false;
	private int ind = -1;
	private float omega = 0;
	private float omega_0 = 0.15f;
	private boolean swing = false;
	private boolean back = false;
	private float[] grabSetPoints = {14.019997f, 11.73999f, 9.399997f, 7.0199966f, 4.720001f};
	private Vector2[] shimmySetPoints = {
			new Vector2(12f,13f),
			new Vector2(12f,9f),
			new Vector2(17f,9f),
			new Vector2(17f,14f),
			new Vector2(22.5f,14f) };
	private Vector2[] flingSetPoints = {
			new Vector2(2f,14f),
			new Vector2(8f,16f),
			new Vector2(14f, 14f),
			new Vector2(22f, 14f)  };
	private Vector2[] flingLandPoints0 = {
			new Vector2(-2f, 12f),
			new Vector2(4f, 14f),
			new Vector2(10f, 12f),
			new Vector2(18f, 11.5f)
	};
	private Vector2[] flingLandPointsf = {
			new Vector2(5.5f, 16f),
			new Vector2(11.5f, 14f),
			new Vector2(19.5f, 14f),
			new Vector2(23f, 16f)
	};
	private Vector2[] vineSetPoints = {
			new Vector2(4f, 13f),
			new Vector2(6f, 16.5f),
			new Vector2(9.6f, 12.7f),
			new Vector2(13f,12f), 		// vine
			new Vector2(16f, 12f),
			new Vector2(19f, 12f),		// vine
			new Vector2(23f, 12f), 	// vine
			new Vector2(29f, 14f) 		// owl
	};
	private Vector2[] ebbSetPoints = {
			new Vector2(2f, 11f),		// W
			new Vector2(4f, 9f),		// S
			new Vector2(6f, 11f),		// E
			new Vector2(4f, 13f) 		// N
	};
	private ArrayList<Integer> vineInds = new ArrayList<>(Arrays.asList(3,5,6));
	// list of instructions
	private boolean[] shimmyGrabbed = {false, false, false, false, false};
	private int[] shimmyDir = {SHIMMY_S, SHIMMY_E, SHIMMY_N, SHIMMY_E, SHIMMY_SE};
	private boolean[] flingGrabbed = {false, false, false, false};
	private int[] flingDir = {SHIMMY_NE, SHIMMY_SE, SHIMMY_E, SHIMMY_SE};
	private boolean[] vineGrabbed = {false, false, false, false, false, false, false, false};
	private int[] vineDir = {SHIMMY_E, SHIMMY_SE, SHIMMY_E, SHIMMY_E, SHIMMY_N, SHIMMY_E, SHIMMY_E, SHIMMY_NE};
	private boolean[] ebbGrabbed = flingGrabbed; //reuse the arraylist

	// stuff for follow ebb
	private final int ebbGrabPts = 0;
	private final int ebbFlag = 1;
	private final int ebbFling = 2;
	private final int ebbFlingUp = 3;
	private final int ebbVine1 = 4;
	private final int ebbVine2 = 5;
	private int ebbLvl = ebbGrabPts;
	private final int[] ebbTrunkNum = {2,2,5,6,7,8,8};
	private float grabs = 0;


	public void preLoadContent(MantisAssetManager manager) {
		super.preLoadContent(manager);
	}
	/**
	 * Load the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(MantisAssetManager manager) {
		super.loadContent(manager);
		// reset animation frames
		if(joystickAnimation == null) {
			joystickAnimation = new Animation(0.15f, manager.getTextureAtlas().findRegions("joy"), Animation.PlayMode.LOOP);
			bumperLAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperL"), Animation.PlayMode.LOOP);
			bumperRAnimation = new Animation(0.20f, manager.getTextureAtlas().findRegions("bumperR"), Animation.PlayMode.LOOP);
		}

		lPressed = manager.get(lPressedPath);
		rPressed = manager.get(rPressedPath);
		lUp = manager.get(lUpPath);
		rUp = manager.get(rUpPath);

		holdUp = manager.get(holdUpPath);
		holdDown = manager.get(holdDownPath);
		holdLeft = manager.get(holdLeftPath);
		holdRight = manager.get(holdRightPath);
		swing0 = manager.get(swing0Path);
		swing1 = manager.get(swing1Path);
		swing2 = manager.get(swing2Path);
		swing3 = manager.get(swing3Path);

		DEFAULT_LEVEL = "tutorial1";
		loadLevel = DEFAULT_LEVEL;

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/ReginaFree.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
		param.size = 56;
		param.color = Color.YELLOW;
		param.shadowColor = Color.LIGHT_GRAY;
		param.shadowOffsetX = 1;
		param.shadowOffsetY = 1;
		instrFont = generator.generateFont(param);
		generator.dispose();
	}

	// Physics objects for the game
	/** Reference to the character avatar */

	public TutorialModeController() {
		super();
		currentStage = 0;
		trunkEntities.clear();
		MAX_TUTORIAL = GlobalConfiguration.getInstance().getAsInt("maxTutorial");
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		bounds = new Rectangle(0,0,16.0f, 9.0f);
		loadLevel = "tutorial"+currentStage;
		trunkEntities.clear();
		trunkGrabbed.clear();
		super.reset();
		time = 0;
		moveLeftArm = false;
		grabs = 0;
		inRangeSetPt = -1;
		targetLine = NEUTRAL;
		angleDiff = 0f;
		nextSetPt = false;
		omega = 0;
		omega_0 = 0.15f;
		count = 0f;
		ind = -1;
		swing = false;
		back = false;
		ebbLvl = ebbGrabPts;
//		ebbLvl = ebbFling;
		for(int i = 0; i < shimmyGrabbed.length; i++)
			shimmyGrabbed[i] = false;
		for(int i = 0; i < flingGrabbed.length; i++)
			flingGrabbed[i] = false;
		for(int i = 0; i < vineGrabbed.length; i++)
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
		trunkEntities.clear();
		vineEntities.clear();
		for(Entity e: objects) {
			if(e instanceof Trunk) {
				trunkEntities.add((Trunk)e);
				trunkGrabbed.add(false);
			}
			if(e instanceof  Vine) {
				vineEntities.add((Vine) e);
			}
		}
		if(currentStage == STAGE_PINNED) {
			slothList.get(0).pin(world);
			slothList.get(0).setPinned();
		}
		if(currentStage == STAGE_EBB){
			Filter f = new Filter();
			f.maskBits = FilterGroup.NOCOLLIDE;
			for(Entity obj : objects) {
				f.categoryBits = FilterGroup.VINE;
				if (obj instanceof Trunk) {
					for (Obstacle plank : ((Trunk)obj).getBodies()) {
						plank.setFilterData(f);
					}
				}
				if (obj instanceof Vine) {
					for(Obstacle plank: ((Vine)obj).getBodies()) {
						plank.setFilterData(f);
					}
				}
				if (obj instanceof OwlModel) {
					f.categoryBits = FilterGroup.WALL;
					((OwlModel)obj).setFilterData(f);
				}
			}
		}
	}

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 *
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!super.preUpdate(dt)) {
			return false;
		}


		InputController input = InputControllerManager.getInstance().getController(0);
		if(next) {
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
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class askew.playermode.WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		super.update(dt);
		if (!paused) {
			elapseTime += dt;
			time = time+dt ;
			// TODO: move sloth movement in slothmodel
			slothList.get(0).setTutorial();
			InputController input = InputControllerManager.getInstance().getController(0);

			switch(currentStage) {
				case STAGE_PINNED:
					updatePINNED(input);
					break;
				case STAGE_GRAB:
					updateGRAB(input);
					break;
				case STAGE_EBB:
					updateEBB(input, dt);
				case STAGE_SHIMMY:
					updateSHIMMY(input);
					break;
				case STAGE_FLING:
					updateFLING(input);
					break;
				case STAGE_VINE:
					updateVINE(input);
					break;
				default:
					System.err.println(currentStage);
			}
			if(moveToNextStage())
				next = true;
			prevLeftGrab = slothList.get(0).isActualLeftGrab();
			prevRightGrab = slothList.get(0).isActualRightGrab();
		}
	}

	public void updatePINNED(InputController input) {
		if(input.getLeftGrab()){
			moveLeftArm = true;
		} else if (input.getRightGrab()) {
			moveLeftArm = false;
		}
		slothList.get(0).removeArm(moveLeftArm);
		return;
	}

	public void updateGRAB(InputController input) {
		grabbedAll = trunkGrabbed.get(0);
		for (int i = 0; i < trunkGrabbed.size(); i++) {
			grabbedAll = trunkGrabbed.get(i) && grabbedAll;
		}
		if(inRangeSetPt+1 >= grabSetPoints.length) { return; }
		Vector2 setpt = new Vector2(slothList.get(0).getX(),grabSetPoints[inRangeSetPt+1]);
		if (inRange(setpt)) {
			inRangeSetPt++;
		}
		return;
	}

	public void updateEBB(InputController input, float dt) {
//		System.out.println("ebblvl: "+ebbLvl);
		switch(ebbLvl) {
			case ebbGrabPts:
				for(int i = 0; i < ebbGrabbed.length; i++) {
					if (!ebbGrabbed[i]) {
						ebbGrabbed[i] = checkGrabbedPt(ebbSetPoints[i]);
						if(ebbGrabbed[i]) {
							grabs += 1;
						}
					}
				}
				if(grabs == 4) {
					ebbLvl++;
				}
				break;
			case ebbFlag:
				Vector2 lhPos = slothList.get(0).getLeftHand().getPosition();
				Vector2 rhPos = slothList.get(0).getRightHand().getPosition();
				omega = lhPos.sub(rhPos).angle();
				if (Math.abs(omega-omega_0) < 3 && (Math.abs(omega_0)%180 < 15 || 180-(Math.abs(omega_0)%180) < 15)) {
					if((getSloth().getLeftTarget() == null && getSloth().getRightTarget() != null)
							|| (getSloth().getLeftTarget() != null && getSloth().getRightTarget() == null))
					grabs += dt;
				} else {
					omega_0 = omega;
					grabs = 0;
				}
				if (grabs > 2.5) {
					ebbLvl++;
				}
//				System.out.print("time "+grabs+"   ");
				break;
			case ebbFling:
				for(Trunk t : trunkEntities) {
					for(Obstacle plank: t.getBodies()){
						if(plank.getBody().getUserData() instanceof Obstacle && ((Obstacle)plank.getBody().getUserData()).isGrabbed()) {
							ind = trunkEntities.indexOf(t);
							trunkGrabbed.set(ind,true);
							if(ind != 2 && ind != 3) {
								trunkGrabbed.set(2,false);
								trunkGrabbed.set(3,false);
							}
						}
					}
				}
				swing = trunkGrabbed.get(2);
				back = trunkGrabbed.get(3);
//				System.out.print(swing+"   "+back+"   "+ind);
				if((swing && ind == 3) || back && ind == 2) {
					ebbLvl++;
				}
				break;
			case ebbFlingUp:
				for(Trunk t : trunkEntities) {
					for(Obstacle plank: t.getBodies()){
						if(plank.getBody().getUserData() instanceof Obstacle && ((Obstacle)plank.getBody().getUserData()).isGrabbed()) {
							ind = trunkEntities.indexOf(t);
							if(ind !=5) {
								trunkGrabbed.set(2,false);
								trunkGrabbed.set(3,false);
							}
							trunkGrabbed.set(ind,true);
						}
					}
				}
				swing = trunkGrabbed.get(2);
				back = trunkGrabbed.get(3);
//				System.out.print(swing+"   "+back+"   "+ind+"   "+trunkGrabbed.get(5)+"   ");
				if((swing && ind == 5) || back && ind == 5) {
					ebbLvl++;
				}
				break;
			case ebbVine1:
				for(Trunk t : trunkEntities) {
					for(Obstacle plank: t.getBodies()){
						if(plank.getBody().getUserData() instanceof Obstacle && ((Obstacle)plank.getBody().getUserData()).isGrabbed()) {
							ind = trunkEntities.indexOf(t);
							trunkGrabbed.set(ind,true);
							if(ind != 6) {
								flingGrabbed[0] = false;
								trunkGrabbed.set(6,false);
							}
						}
					}
				}
				for(Vine v : vineEntities) {
					ind = vineEntities.indexOf(v);
					for (Obstacle plank : v.getBodies()) {
						if (plank.getBody().getUserData() instanceof Obstacle) {
							if(((Obstacle) plank.getBody().getUserData()).isGrabbed()) {
								flingGrabbed[ind] = true;
								trunkGrabbed.set(6, false);
								if (ind != 0) {
									flingGrabbed[0] = false;
								}
							}
						}
					}
				}
				swing = flingGrabbed[0];
				back = trunkGrabbed.get(6);
//				System.out.print(swing + "   " + back + "   " + ind + "   " + trunkGrabbed.get(6) + "   ");
				if (swing && back) {
					for(int i = 0; i < trunkGrabbed.size(); i++)
						trunkGrabbed.set(i, false);
					for(int i = 0; i < flingGrabbed.length; i++)
						flingGrabbed[i] = false;
					ebbLvl++;
				}
				break;
			case ebbVine2:
				for(Trunk t : trunkEntities) {
					for(Obstacle plank: t.getBodies()){
						if(plank.getBody().getUserData() instanceof Obstacle && ((Obstacle)plank.getBody().getUserData()).isGrabbed()) {
							ind = trunkEntities.indexOf(t);
							trunkGrabbed.set(ind,true);
							if(ind != 7) {
								flingGrabbed[1] = false;
								flingGrabbed[2] = false;
								trunkGrabbed.set(7,false);
							}
						}
					}
				}
				for(Vine v : vineEntities) {
					ind = vineEntities.indexOf(v);
					for (Obstacle plank : v.getBodies()) {
						if (plank.getBody().getUserData() instanceof Obstacle) {
							if(((Obstacle) plank.getBody().getUserData()).isGrabbed()) {
								flingGrabbed[ind] = true;
								if (ind == 0) {
									flingGrabbed[1] = false;
									flingGrabbed[2] = false;
								}
								if (ind == 1) {
									flingGrabbed[2] = false;
								}
								trunkGrabbed.set(7, false);
							}
						}
					}
				}
				swing = flingGrabbed[1];
				back = flingGrabbed[2];
//				System.out.print(swing + "   " + back + "   " + ind + "   " + trunkGrabbed.get(7) + "   ");
				if (swing && back && trunkGrabbed.get(7)) {
					ebbLvl++;
				}
				break;
		}
		Filter f = new Filter();
		f.maskBits = FilterGroup.WALL | FilterGroup.SLOTH | FilterGroup.HAND;
		f.categoryBits = FilterGroup.VINE;
		for (int i = 0; i < ebbTrunkNum[ebbLvl]; i++) {
			for(Obstacle plank: trunkEntities.get(i).getBodies()) {
				plank.setFilterData(f);
			}
		}
		if (ebbLvl >= ebbVine1) {
			for(Obstacle plank: vineEntities.get(0).getBodies()) {
//				System.out.println("getting filter");
				plank.setFilterData(f);
			}
		}
		if (ebbLvl >= ebbVine2) {
			for(Obstacle plank: vineEntities.get(1).getBodies()) {
				plank.setFilterData(f);
			}
			for(Obstacle plank: vineEntities.get(2).getBodies()) {
				plank.setFilterData(f);
			}
		}
		if (ebbLvl > ebbVine2) {
			f.maskBits = FilterGroup.SLOTH | FilterGroup.HAND;
			f.categoryBits = FilterGroup.WALL;
			owl.setFilterData(f);
		}
		return;
	}

	public void updateSHIMMY(InputController input) {
		if(inRangeSetPt+1 >= shimmySetPoints.length) { return; }
		if (inRange(shimmySetPoints[inRangeSetPt+1])) {
			inRangeSetPt++;
		}
		if (inRangeSetPt >= 0 && !shimmyGrabbed[inRangeSetPt]) {
			shimmyGrabbed[inRangeSetPt] = checkGrabbedPt(shimmySetPoints[inRangeSetPt], shimmyDir[inRangeSetPt]);
		}
		return;
	}

	public void updateFLING(InputController input) {
		//	System.out.println("\n progression "+inRangeSetPt);
		Vector2 set;
		if(inRangeSetPt+1 >= flingSetPoints.length) {
			// if done with setpoints
			angleDiff = 0f;
			targetLine = NEUTRAL;
			return;
		}
		set = flingSetPoints[inRangeSetPt+1];
		Vector2 backpt = flingLandPoints0[inRangeSetPt+1];
		Vector2 landpt = flingLandPointsf[inRangeSetPt+1];
		inRange(set);
		if(inRangeSetPt+2 < flingSetPoints.length && inRange(flingSetPoints[inRangeSetPt+2])){
			inRangeSetPt++;
		}
		if (inRange(flingSetPoints[inRangeSetPt+1])) {
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
			if ((omega_0-omega) < 0.05) {
				//	System.out.print("   back "+back);
				if(!back) {
					inRange(backpt);
					//	targetLine = MINUS30;
					back = reachedBackPt(backpt);
					count = 0;
				}
				else {
					inRange(landpt);
					//	targetLine = NEUTRAL;
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
			if(inRange(landpt)) {
				targetLine = NEUTRAL;
				count++;
				if (count > 120) {
					count = 0;
					back = false;
					swing = false;
				}
				inRangeSetPt++;
				return;
			}
		}
		if (swing && flingGrabbed[inRangeSetPt+1]) {
			//reset
			count = 0;
			back = false;
			swing = false;
			inRangeSetPt++;
		}
		return;
	}

	public void updateVINE(InputController input) {
		//	System.out.print("\n progression "+inRangeSetPt);
		if (inRangeSetPt == -1) {
			targetLine = EAST;
		}
		if (inRangeSetPt+1 > vineSetPoints.length) {
			angleDiff = 0f;
			targetLine = NEUTRAL;
			return;
		}
		//	System.out.print("   targetLINE "+targetLine);
		//	System.out.print("   setpt ");
		//	printVector(vineSetPoints[inRangeSetPt+1]);
		//	System.out.print("   grabpt ");
		//	try {
		//		printVector(sloth.getMostRecentlyGrabbed().getPosition());
		//	} catch (NullPointerException e) {
		//		printVector(new Vector2());
		//	}
		//	System.out.print("   inRange "+inRange(vineSetPoints[inRangeSetPt+1]));
		if (inRange(vineSetPoints[inRangeSetPt+1]) && vineGrabbed[inRangeSetPt+1]) {
			inRangeSetPt++;
			if (!swing) {
				ind = vineInds.indexOf(inRangeSetPt + 1);
			}
			if (nextSetPt) {
				nextSetPt = false;
				swing = false;
			}
		}
		//	System.out.print("   ind "+ind);
		if (ind != -1) {
			swing = true;
			Vine v = vineEntities.get(ind);
			vineSetPoints[inRangeSetPt+1] = v.getEndpt().getPosition();
			inRange(vineSetPoints[inRangeSetPt+1]);
			setTarget(vineEntities.get(ind));
		}
		//	System.out.print("   swing "+swing);
		if (!vineGrabbed[inRangeSetPt+1]) {
			if (!swing) {
				inRange(vineSetPoints[inRangeSetPt + 1]);
			}
			vineGrabbed[inRangeSetPt+1] = checkGrabbedPt(vineSetPoints[inRangeSetPt+1], vineDir[inRangeSetPt+1]);
			if (vineGrabbed[inRangeSetPt+1]) {
				nextSetPt = true;
			}
		}
		return;
	}

	public boolean checkGrabbedPt(Vector2 setpt) {
		try {
			Vector2 tPos = slothList.get(0).getMostRecentTarget().getPosition();
			boolean xrange = Math.abs(tPos.x - setpt.x) <= 0.5;
			boolean yrange = Math.abs(tPos.y - setpt.y) <= 0.5;
			return xrange && yrange;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean checkGrabbedPt(Vector2 setpt, int dir) {
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
			} else if (lv > 0.05f){
				targetLine = PLUS10;
			} else {
				targetLine = NEUTRAL;
			}
		} else {
			targetLine = NEUTRAL;
		}

	}

	public void checkCloseToCorner(Vector2 setpt, Vector2 grabpt) {
		float xrange = (float) Math.abs(setpt.x - grabpt.x);
		float yrange = (float) Math.abs(setpt.y - grabpt.y);
		if(xrange < 0.08 && yrange < 0.08) {
			shimmyGrabbed[inRangeSetPt+1] = true;
		}
	}

	public boolean moveToNextStage() {
		if(currentStage == STAGE_PINNED) {
			return (time > 1.5f && pressedA);
		} else if (currentStage == STAGE_GRAB) {
			return (grabbedAll && pressedA);
		}else if(currentStage > STAGE_PINNED) {
			return owl.isDoingVictory();
		}
		return false;
	}

	public void drawHelpLines() {
		switch (currentStage) {
			case STAGE_PINNED:
				break;
			case STAGE_GRAB:
				if(!grabbedAll)
					slothList.get(0).drawHelpLines(canvas, camTrans, SHIMMY_S, 0f);
				break;
			case STAGE_SHIMMY:
			case STAGE_FLING:
			case STAGE_VINE:
				switch(targetLine) {
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
		bumperLTexture = bumperLAnimation.getKeyFrame(elapseTime,true);
		bumperRTexture = bumperRAnimation.getKeyFrame(elapseTime, true);

//		canvas.draw(container, Color.WHITE, container.getWidth() / 2, 0, 425, 300, 0, worldScale.x * 5 / container.getWidth(), worldScale.y * 5 / container.getHeight());
				if(currentStage == STAGE_PINNED) {
					if(moveLeftArm){
						canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
						canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
					} else{
						canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
						canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
					}
				} else if(currentStage == STAGE_GRAB) {
					if (!grabbedAll) {
				canvas.drawTextCentered("Try to grab all 5 branches", instrFont, 250f);
			}
		} else if (currentStage == STAGE_SHIMMY) {
			canvas.drawTextCentered("Try to shimmy across to the owl", instrFont, 250f);
		} else if (currentStage == STAGE_FLING) {
			canvas.drawTextCentered("Try to fling from branch to branch", instrFont, 250f);
		} else if (currentStage == STAGE_VINE) {
			canvas.drawTextCentered("Learn to swing on the vines", instrFont, 250f);
		} else if (currentStage == STAGE_EBB) {
			switch(ebbLvl) {
				case ebbGrabPts:
					canvas.drawTextCentered("Try to grab all 4 endpoints", instrFont, 250f);
					break;
				case ebbFlag:
					canvas.drawTextCentered("Try to hold Flow's arms horizontally \nwithout grabbing for 2.5s", instrFont, 250f);
					canvas.drawText("time held: "+((int)(grabs*100))/100.0, instrFont, 0.4f*canvas.getWidth(), 0.3f*canvas.getHeight());
					break;
				case ebbFling:
					canvas.drawTextCentered("Try to fling from one short \ntrunk to the other", instrFont, 250f);
					break;
				case ebbFlingUp:
					canvas.drawTextCentered("Now try flinging upwards", instrFont, 250f);
					break;
				case ebbVine1:
					canvas.drawTextCentered("Use the vine to reach \nthe next trunk", instrFont, 250f);
					break;
				case ebbVine2:
					canvas.drawTextCentered("Use the next 2 vines \nto reach the following trunk", instrFont, 250f);
					break;
				default:
					canvas.drawTextCentered("Reach Cherry the owl \nto complete the tutorial", instrFont, 250f);
			}
		}
		if (currentStage >= STAGE_GRAB) {
			if(slothList.get(0).isActualRightGrab()) {
				canvas.draw(rPressed, Color.WHITE, rPressed.getWidth() / 2, 0, 450, 550, 0, worldScale.x / rPressed.getWidth(), worldScale.y / rPressed.getHeight());
				canvas.draw(lUp, Color.WHITE, lUp.getWidth() / 2, 0, 350, 550, 0, worldScale.x / lUp.getWidth(), worldScale.y / lUp.getHeight());
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x/ joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
			} else {
				canvas.draw(rUp, Color.WHITE, rUp.getWidth() / 2, 0, 450, 550, 0, worldScale.x / rUp.getWidth(), worldScale.y / rUp.getHeight());
				canvas.draw(lPressed, Color.WHITE, lPressed.getWidth() / 2, 0, 350, 550, 0, worldScale.x / lPressed.getWidth(), worldScale.y / lPressed.getHeight());
				canvas.draw(joystickNeutralTexture, Color.WHITE, joystickNeutralTexture.getRegionWidth() / 2, 0, 350, 450, 0, worldScale.x / joystickNeutralTexture.getRegionWidth(), worldScale.y / joystickNeutralTexture.getRegionHeight());
				canvas.draw(joystickTexture, Color.WHITE, joystickTexture.getRegionWidth() / 2, 0, 450, 450, 0, worldScale.x / joystickTexture.getRegionWidth(), worldScale.y / joystickTexture.getRegionHeight());
			}
		}
		if((currentStage == STAGE_PINNED && time > 6f) ||
				(currentStage == STAGE_GRAB && grabbedAll)) {
			canvas.drawTextCentered("Press A to continue", instrFont, 200f);
		}
		if(currentStage == STAGE_PINNED) {
			if (time < 6f) {
				canvas.drawTextCentered("Practice moving one arm at a time", instrFont, 250f);
			}
			if (moveLeftArm) {
				canvas.draw(rPressed, Color.WHITE, rPressed.getWidth() / 2, 0, 365, 150, 0, worldScale.x / rPressed.getWidth(), worldScale.y / rPressed.getHeight());
				canvas.drawTextCentered("Press RB to switch arms", instrFont, -250f);
			} else {
				canvas.draw(lPressed, Color.WHITE, lPressed.getWidth() / 2, 0, 365, 150, 0, worldScale.x / lPressed.getWidth(), worldScale.y / lPressed.getHeight());
				canvas.drawTextCentered("Press LB to switch arms", instrFont, -250f);
			}
		}
	}

	public void draw(float delta){
		canvas.clear();

		canvas.begin();
		canvas.draw(background);
		canvas.end();

		InputController input =  InputControllerManager.getInstance().getController(0);

		float slothX = slothList.stream().map(sloth->sloth.getBody().getPosition().x).reduce((x,y)->x+y).orElse(0f) / slothList.size();
		float slothY = slothList.stream().map(sloth->sloth.getBody().getPosition().y).reduce((x,y)->x+y).orElse(0f) / slothList.size();

		cameraVelocityX = cameraVelocityX * 0.4f + (slothX - cameraX) * 0.18f;
		cameraVelocityY = cameraVelocityY * 0.4f + (slothY - cameraY) * 0.18f;
		cameraX += cameraVelocityX;
		cameraY +=  cameraVelocityY;

		// Check for camera in bounds
		// Y Checks
		if (cameraY - bounds.height /2f < levelModel.getMinY()) {
			cameraY = levelModel.getMinY() + bounds.height/2f ;
		}

		if (cameraY + bounds.height / 2f > levelModel.getMaxY()) {
			cameraY = levelModel.getMaxY() - bounds.height/2f;
		}

		// X Checks
		if (cameraX - bounds.width/2 < levelModel.getMinX()) {
			cameraX = levelModel.getMinX() + bounds.width / 2f;
		}

		if (cameraX + bounds.width/2f > levelModel.getMaxX()) {
			cameraX = levelModel.getMaxX() - bounds.width / 2f;
		}

		camTrans.setToTranslation(-1 * cameraX * worldScale.x
				, -1 * cameraY * worldScale.y);

		camTrans.translate(canvas.getWidth()/2,canvas.getHeight()/2);
		canvas.getCampos().set( cameraX * worldScale.x
				, cameraY * worldScale.y);

		canvas.begin(camTrans);
		Collections.sort(objects);

		for(Entity obj : objects) {
			obj.setDrawScale(worldScale);
			// if stage 2, tint trunks if already grabbed
			if(!(obj instanceof SlothModel)) {
				if (currentStage == STAGE_GRAB && obj instanceof Trunk) {
					Trunk trunk = (Trunk) obj;
					int ind = trunkEntities.indexOf(obj);

					for (Obstacle plank : trunk.getBodies()) {
						if (plank.getBody().getUserData() instanceof Obstacle && ((Obstacle) plank.getBody().getUserData()).isGrabbed()) {
							trunkGrabbed.set(ind, true);
						}
					}
				} else if (currentStage != STAGE_EBB) {
					obj.draw(canvas);
				} else if (!(obj instanceof Trunk || obj instanceof Vine || obj instanceof OwlModel)) {
					obj.draw(canvas);
				}
			}
		}

		if(currentStage == STAGE_EBB) {
			switch(ebbLvl) {
				case ebbGrabPts:
					if(!ebbGrabbed[0]) {
						canvas.draw(holdLeft, new Color(0x7f7f7fB0), holdLeft.getWidth(), holdRight.getHeight()/2, 425, 1100, 0, 1, 1);
					}
					if(!ebbGrabbed[1]) {
						canvas.draw(holdDown, new Color(0x7f7f7fB0), holdDown.getWidth() / 2, holdDown.getHeight(), 405, 1110, 0, 1, 1);
					}
					if(!ebbGrabbed[2]) {
						canvas.draw(holdRight, new Color(0x7f7f7fB0), 0, holdRight.getHeight()/2, 375, 1090, 0, 1, 1);
					}
					if(!ebbGrabbed[3]) {
						canvas.draw(holdUp, new Color(0x7f7f7fB0), holdUp.getWidth() / 2, 0, 405, 1090, 0, 1, 1);
					}
					break;
				case ebbFlag:
					canvas.draw(holdRight, new Color(0x7f7f7fB0), 0, holdRight.getHeight()/2, 375, 1290, 0, 1, 1);
					break;
			}
		}

		if(currentStage != STAGE_EBB) {
			// trunk tinting done here
			for (int i = 0; i < trunkEntities.size(); i++) {
				if (trunkGrabbed.get(i)) {
					(trunkEntities.get(i)).draw(canvas, Color.GRAY);
				} else {
					trunkEntities.get(i).draw(canvas);
				}
			}
		} else {
//			System.out.println("ebblvl: "+ebbLvl);
			for (int i = 0; i < ebbTrunkNum[ebbLvl]; i++) {
				trunkEntities.get(i).draw(canvas);
			}
			if (ebbLvl >= ebbVine1) {
				vineEntities.get(0).draw(canvas);
			}
			if (ebbLvl >= ebbVine2) {
				vineEntities.get(1).draw(canvas);
				vineEntities.get(2).draw(canvas);
			}
			if (ebbLvl > ebbVine2) {
				owl.draw(canvas);
			}
		}

		for(SlothModel s : slothList) {
			s.draw(canvas);
		}

		if (!playerIsReady && !paused && coverOpacity <= 0)
			printHelp();
		canvas.end();
		slothList.get(0).drawGrab(canvas, camTrans);

		drawHelpLines();

		if (debug) {
			canvas.beginDebug(camTrans);
			for(Entity obj : objects) {
				if( obj instanceof Obstacle){
					((Obstacle)obj).drawDebug(canvas);
				}
			}
			canvas.endDebug();
			canvas.begin();
			// text
			canvas.drawTextStandard("FPS: " + 1f/delta, 10.0f, 100.0f);
			canvas.end();
			slothList.get(0).drawForces(canvas, camTrans);
		}

		// draw instructional animations
		canvas.begin();
		drawInstructions();
		canvas.end();

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
