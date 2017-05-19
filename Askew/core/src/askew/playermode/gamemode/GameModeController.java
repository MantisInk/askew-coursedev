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
import askew.entity.owl.OwlModel;
import askew.entity.sloth.SlothModel;
import askew.playermode.WorldController;
import askew.playermode.gamemode.Particles.Effect;
import askew.playermode.gamemode.Particles.ParticleController;
import askew.playermode.leveleditor.LevelModel;
import askew.util.RecordBook;
import askew.util.SoundController;
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Gameplay specific controller for Askew.
 * <p>
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class GameModeController extends WorldController {


	public static final float MAX_MUSIC_VOLUME = 0.45f;
	Affine2 camTrans = new Affine2();

	/** Track asset loading from all instances and subclasses */
	private AssetState platformAssetState = AssetState.EMPTY;

	/** Track asset loading from all instances and subclasses */
	@Getter
	protected static boolean playerIsReady = false;
	@Getter
	protected boolean paused = false;
	protected boolean prevPaused = false;
	private boolean victory = false;
	// fern selection indicator locations for pause menu options
	protected Vector2[] pause_locs = {
			new Vector2(0.68f,0.53f),
			new Vector2(0.55f,0.43f),
			new Vector2(0.68f,0.33f)};
	protected Vector2[] victory_locs = {
			new Vector2(0.08f,0.53f),
			new Vector2(0.22f,0.43f),
			new Vector2(0.12f,0.33f)};

	public static final String[] GAMEPLAY_MUSIC = new String[] {
			"sound/music/askew.ogg",
			"sound/music/flowwantshisorherbaby.ogg",
			"sound/music/Presentation2.ogg",
			"sound/music/Presentation3.ogg",
			"sound/music/Presentation1.ogg"
	};

	public static final String OK_SOUND = "sound/effect/youdidokay.wav";
	public static final String GREAT_SOUND = "sound/effect/youdidgreat.wav";
	public static final String GRAB_SOUND = "sound/effect/grab.wav";
	public static final String VICTORY_SOUND = "sound/effect/realvictory.wav";
	public static final String RELEASE_SOUND = "sound/effect/release.wav";
	public static final String ARM_SOUND = "sound/effect/arm.wav";
	public static final String WIND_SOUND = "sound/effect/wind.wav";
	public static final String GHOST_SOUND = "sound/effect/ghostkillsyou.wav";
	public static final String FALL_MUSIC = "sound/music/fallingtoyourdeath" +
			".wav";

	Sound grabSound;
	Sound releaseSound;
	Sound victorySound;
	Sound ghostSound;
	Sound okSound;
	Sound greatSound;

	@Setter
	protected String loadLevel, DEFAULT_LEVEL;
	protected LevelModel levelModel; 				// LevelModel for the level the player is currently on
	private int numLevel, MAX_LEVEL, MAX_MULTI_LEVEL; 	// track int val of
	// lvl #

	protected float currentTime, recordTime;	// track current and record time to complete level
	protected int currentGrabs, recordGrabs;
	protected boolean leftPrevGrab, rightPrevGrab, leftNewGrab, rightNewGrab;
	private boolean storeTimeRecords;
	private RecordBook records = RecordBook.getInstance();

	protected PhysicsController collisions;

	private JSONLoaderSaver jsonLoaderSaver;
	private float initFlowX;
	private float initFlowY;
	private int PAUSE_RESUME = 0;
	private int PAUSE_RESTART = 1;
	private int PAUSE_MAINMENU = 2;
	protected int pause_mode = PAUSE_RESUME;
	private int VICTORY_NEXT = 0;
	private int VICTORY_RESTART = 1;
	private int VICTORY_MAINMENU = 2;
	private int victory_mode = VICTORY_NEXT;
	protected Texture background;
	protected Texture pauseTexture;
	protected Texture victoryTexture;
	protected Texture fern;
	protected Texture edgefade;
	private static final float NEAR_FALL_DEATH_DISTANCE = 9;
	private static final float LOWEST_ENTITY_FALL_DEATH_THRESHOLD = 12;
	protected static final float CYCLES_OF_INTRO = 50f;
	private float fallDeathHeight;
	private String selectedTrack;
	private String lastLevel;
	private MantisAssetManager manager;
	protected float cameraX;
	protected float cameraY;
	protected float cameraVelocityX;
	protected float cameraVelocityY;
	private VictoryCutscene victoryCutscene;
	private boolean multiplayer;
	private float owlOPosX;
	private float owlOPosY;

	//For playtesting control schemes
	private int currentMovement;
	private int currentControl;
	private int graphicsSetting = 0;
	private float windVolume;
	private int framesToDie;


	/** The opacity of the black text covering the screen. Game can start
	 * when this is zero. */
	protected float coverOpacity;

	protected ParticleController particleController;
	protected static final int MAX_PARTICLES = 2000;
	protected static final int INITIAL_FOG = 50;

	protected float fogTime,eyeTime;
	private int levelCompleteJunkState;
	private int showStatsTimer;
	private int victorySloth;

	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(MantisAssetManager manager) {
		if (platformAssetState != AssetState.EMPTY) {
			return;
		}
		for (String soundName : GAMEPLAY_MUSIC) {
			manager.load(soundName, Sound.class);
		}
		manager.load("sound/music/levelselect.ogg", Sound.class);
		manager.load(FALL_MUSIC, Sound.class);
		manager.load(ARM_SOUND, Sound.class);
		manager.load(WIND_SOUND, Sound.class);
		manager.load(GHOST_SOUND, Sound.class);
		manager.load(OK_SOUND, Sound.class);
		manager.load(GREAT_SOUND, Sound.class);

		manager.load(VICTORY_SOUND, Sound.class);
		manager.load(GRAB_SOUND, Sound.class);
		manager.load(RELEASE_SOUND, Sound.class);


		platformAssetState = AssetState.LOADING;
		jsonLoaderSaver.setManager(manager);
		this.manager = manager;
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
		if (platformAssetState != AssetState.LOADING) {
			return;
		}

		for (String soundName : GAMEPLAY_MUSIC) {
			SoundController.getInstance().allocate(manager, soundName);
		}
		SoundController.getInstance().allocate(manager, "sound/music/levelselect" +
				".ogg");

		SoundController.getInstance().allocate(manager, FALL_MUSIC);
		SoundController.getInstance().allocate(manager, ARM_SOUND);
		SoundController.getInstance().allocate(manager, WIND_SOUND);

		grabSound = Gdx.audio.newSound(Gdx.files.internal(GRAB_SOUND));
		releaseSound = Gdx.audio.newSound(Gdx.files.internal(RELEASE_SOUND));
		victorySound = Gdx.audio.newSound(Gdx.files.internal(VICTORY_SOUND));
		ghostSound = Gdx.audio.newSound(Gdx.files.internal(GHOST_SOUND));
		okSound = Gdx.audio.newSound(Gdx.files.internal(OK_SOUND));
		greatSound = Gdx.audio.newSound(Gdx.files.internal(GREAT_SOUND));

		pauseTexture = manager.get("texture/background/pause.png", Texture.class);
		victoryTexture = manager.get("texture/background/victory.png", Texture.class);
		fern = manager.get("texture/background/fern.png");
		edgefade = manager.get("texture/postprocess/edgefade.png");

		particleController.setTextures(manager);
		victoryCutscene = new VictoryCutscene(manager);

		super.loadContent(manager);
		platformAssetState = AssetState.COMPLETE;
	}


	// Physics constants for initialization
	/** The new heavier gravity for this world (so it is not so floaty) */
	private static final float  DEFAULT_GRAVITY = -12.5f;//-15.7f;

	// Physics objects for the game
	protected static OwlModel owl;

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public GameModeController() {
		super(DEFAULT_GRAVITY);
		collisions = new PhysicsController();
		world.setContactListener(collisions);
		DEFAULT_LEVEL = GlobalConfiguration.getInstance().getAsString("defaultLevel");
		MAX_LEVEL = GlobalConfiguration.getInstance().getAsInt("maxLevel");
		MAX_MULTI_LEVEL = GlobalConfiguration.getInstance().getAsInt
				("maxMultiLevel");
		loadLevel = DEFAULT_LEVEL;
		storeTimeRecords = GlobalConfiguration.getInstance().getAsBoolean("storeTimeRecords");
		jsonLoaderSaver = new JSONLoaderSaver(false);
		slothList = new ArrayList<>();
		particleController = new ParticleController(this, MAX_PARTICLES);
	}

	// for use in progressing through levels
	public void setLevel() {
		if (GlobalConfiguration.getInstance().getAsBoolean("multiplayer")) {
			multiplayer = true;
			int lvl = GlobalConfiguration.getInstance().getCurrentMultiLevel();
			if (lvl > MAX_MULTI_LEVEL) {
				loadLevel = "multilevel1";
				listener.exitScreen(this, EXIT_GM_MM);
			} else
				loadLevel = "multilevel" + lvl;
		} else {
			int lvl = GlobalConfiguration.getInstance().getCurrentLevel();
			if (lvl > MAX_LEVEL) {
				loadLevel = "level1";
				listener.exitScreen(this, EXIT_GM_MM);
			} else
				loadLevel = "level"+lvl;
			multiplayer = false;
		}
	}

	// for use in loading levels that aren't part of the progression
	public void setLevel(String lvlName) {
		loadLevel = lvlName;
	}

	public void pause(){
		if (isComplete()) return;
		prevPaused = paused;
		if (!paused) {
			manager.getMenuManager().setupPauseMenu();
			paused = true;
			pause_mode = PAUSE_RESUME;
		}
		else {
			manager.getMenuManager().setupLevelCompleteMenu();
			paused = false;
		}
		playerIsReady = false;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		super.reset();
		framesToDie = 60;
		showStatsTimer = 60;
		Gdx.input.setCursorCatched(true);
		coverOpacity = 2f; // start at 2 for 1 second of full black
		this.windVolume = 0;
		playerIsReady = false;
		paused = false;
		Vector2 gravity = new Vector2(world.getGravity() );

		InputControllerManager.getInstance().inputControllers().forEach(InputController::releaseGrabs);

		particleController.reset();
		fogTime = 0;
		eyeTime = 0;
		for(Entity obj : entities) {
			if( (obj instanceof Obstacle))
				((Obstacle)obj).deactivatePhysics(world);
		}

		entities.clear();
		world.dispose();
		world = new World(gravity,false);
		if(collisions == null){
			collisions = new PhysicsController();
		}
		collisions.reset();
		slothList.clear();

		world.setContactListener(collisions);
		setComplete(false);
		setFailure(false);
		victory = false;
		pause_mode = PAUSE_RESUME;
		victory_mode = VICTORY_NEXT;
		currentControl = GlobalConfiguration.getInstance().getAsInt("flowControlMode");
		currentMovement = GlobalConfiguration.getInstance().getAsInt("flowMovementMode");
		graphicsSetting = GlobalConfiguration.getInstance().getAsInt("graphics");
		particleController.setGraphicsSetting(graphicsSetting);
        populateLevel();
		fallDeathHeight = levelModel.getMinY() -
				LOWEST_ENTITY_FALL_DEATH_THRESHOLD;

		// Setup sound
		SoundController instance = SoundController.getInstance();
		if (playingMusic) {
			if (instance.isActive("menumusic")) instance.stop("menumusic");
			if (instance.isActive("bgmusic")) instance.stop("bgmusic");
			if (selectedTrack != null) {
				instance.play("bgmusic", selectedTrack, true, MAX_MUSIC_VOLUME);
			}
		}

		if (!instance.isActive("fallmusic")) {
			instance.play("fallmusic",
					FALL_MUSIC, true);
			instance.setVolume("fallmusic",0);
		} else {
			instance.setVolume("fallmusic",0);
		}

		if (!instance.isActive("armmusic")) {
			instance.play("armmusic",
					ARM_SOUND, true);
			instance.setVolume("armmusic",0);
		} else {
			instance.setVolume("armmusic",0);
		}

		if (!instance.isActive("windmusic")) {
			instance.play("windmusic",
					WIND_SOUND, true);
			instance.setVolume("windmusic",0);
		} else {
			instance.setVolume("windmusic",0);
		}

		victoryCutscene.reset();
		manager.getMenuManager().setupLevelCompleteMenu();
		if (multiplayer) {
			levelCompleteJunkState = -1;
		} else {
			levelCompleteJunkState = 0;
		}
	}

	/**
	 * Lays out the game geography.
	 */
	protected void populateLevel() {
		// Are we loading a new level?
		if (lastLevel == null || !lastLevel.equals(loadLevel)) {
			selectedTrack = GAMEPLAY_MUSIC[(GlobalConfiguration.getInstance()
					.getCurrentLevel() + GlobalConfiguration.getInstance()
					.getCurrentMultiLevel()) %
					GAMEPLAY_MUSIC.length];
		}
		lastLevel = loadLevel;
			levelModel = jsonLoaderSaver.loadLevel(loadLevel);
			if (levelModel != null) {
				background = manager.get(levelModel.getBackground(), Texture.class);
				recordTime = records.getRecord(loadLevel);
				recordGrabs = records.getRecordGrabs(loadLevel);
			}

			if (levelModel == null) {
				levelModel = new LevelModel();
			}

			int slothId = 0;

			for (Entity o : levelModel.getEntities()) {
				// drawing

				addObject(o);
				if (o instanceof SlothModel) {
					SlothModel sloth = (SlothModel) o;
					sloth.activateSlothPhysics(world);
					collisions.addSloth(sloth);
					if (slothId == 0) {
						initFlowX = sloth.getX();
						initFlowY = sloth.getY();
						cameraX = sloth.getX();
						cameraY = sloth.getY();
					}

					sloth.setControlMode(currentControl);
					sloth.setMovementMode(currentMovement);
					sloth.setId(slothId++);
					slothList.add(sloth);
				}
				if (o instanceof OwlModel) {
					owl = (OwlModel) o;
					owlOPosX = o.getPosition().x;
					owlOPosY = o.getPosition().y;
				}
				o.setDrawScale(worldScale);

			}

			if (slothId == 2) {
				if (!multiplayer) multiplayer = true;
				// Attach the sloths
//				Vine wtfVine = new Vine(initFlowX, initFlowY, 6, 0, 0, 0,
//						false);
//				wtfVine.setTextures(manager);
//				addObject(wtfVine);
//				entities.remove(wtfVine);
//				entities.add(0, wtfVine);
//
//				List<Obstacle> lazy = new ArrayList<>();
//				wtfVine.getBodies().forEach(lazy::add);
//				Filter f = new Filter();
//				f.maskBits = 0;
//				f.categoryBits = 0;
//				wtfVine.getBodies().forEach(body -> body.setFilterData(f));
//				Obstacle left = lazy.get(0);
//
//				// Definition for a revolute joint
//				RevoluteJointDef jointDef = new RevoluteJointDef();
//
//				// Initial joint
//				jointDef.bodyB = slothList.get(0).getMainBody();
//				jointDef.bodyA = left.getBody();
//				jointDef.localAnchorB.set(new Vector2(0, 0.2f));
//				jointDef.localAnchorA.set(new Vector2(0, Vine.lheight / 2));
//				jointDef.collideConnected = false;
//				world.createJoint(jointDef);
//
//				// Definition for a revolute joint
//				jointDef = new RevoluteJointDef();
//
//				// Initial joint
//				jointDef.bodyB = slothList.get(1).getMainBody();
//				jointDef.bodyA = lazy.get(lazy.size() - 1).getBody();
//				jointDef.localAnchorB.set(new Vector2(0, 0.2f));
//				jointDef.localAnchorA.set(new Vector2(0, -Vine.lheight / 2));
//				jointDef.collideConnected = false;
//				world.createJoint(jointDef);
			}
			for(int i = 0; i < INITIAL_FOG; i++) {
				particleController.fogEffect.spawn(levelModel.getMaxX()-levelModel.getMinX(),levelModel.getMaxY()-levelModel.getMinY() );
			}
			particleController.eyeEffect.spawn();
			currentTime = 0f;
			currentGrabs = 0;
			leftPrevGrab = false;
			rightPrevGrab = false;
	}

	/**For drawing force lines*/
	public SlothModel getSloth(){return slothList.get(0);}

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

		if ((input.didLeftDPadPress() || input.isLKeyPressed()) && !paused && !victory) {
			System.out.println("LE");
			listener.exitScreen(this, EXIT_GM_LE);
			return false;
		} else if ((input.didTopDPadPress()) && !paused && !victory) {
			System.out.println("MM");
			listener.exitScreen(this, EXIT_GM_MM);
			return false;
		} else if (input.didBottomDPadPress() && !paused && !victory) {
			System.out.println("reset");
			reset();
		}

		if (victory) {

			paused = false;
			showStatsTimer--;
			String updateString = manager.getMenuManager().update(false).orElse
					("");
			if (!updateString.contains("ACTION")) {
				if (updateString.contains("Main Menu")) {
					okSound.play();
					listener.exitScreen(this, EXIT_GM_MM);
				} else if (updateString.contains("Restart")) {
					okSound.play();
					reset();
				} else if (updateString.contains("BUTT") || ((showStatsTimer < 0) && levelCompleteJunkState !=2 )) {
					if (levelCompleteJunkState == -1) {
						manager.getMenuManager().throwJunkOnTheScreen
								("Sloth " + victorySloth + " Won!");
						levelCompleteJunkState++;
						showStatsTimer = 80;
						greatSound.play();
					}
					else if (levelCompleteJunkState == 0) {
						manager.getMenuManager().throwJunkOnTheScreen
								("Completion Time: " +  String.format("%.2f",
										currentTime));
						levelCompleteJunkState++;
						showStatsTimer = 80;
						okSound.play();
					} else if (levelCompleteJunkState == 1) {
						boolean newRecord = RecordBook.getInstance()
								.setRecord(loadLevel,
										currentTime);
						if (newRecord) {
							manager.getMenuManager().throwJunkOnTheScreen
									("New Record!");
							greatSound.play();
						} else {
							float record = RecordBook.getInstance
									().getRecord(loadLevel);
							manager.getMenuManager().throwJunkOnTheScreen
									("Record Time: " + String.format("%.2f",
											record));
							okSound.play();
						}
						levelCompleteJunkState++;
					}
				}
				else if (updateString.contains("Level") &&
							levelCompleteJunkState == 2) {
							playerIsReady = false;
					if (multiplayer) {
						int current = GlobalConfiguration.getInstance().getCurrentMultiLevel();
						GlobalConfiguration.getInstance().setCurrentMultiLevel
								(current +	1);
					} else {
						int current = GlobalConfiguration.getInstance().getCurrentLevel();
						GlobalConfiguration.getInstance().setCurrentLevel(current + 1);
					}
							setLevel();
							listener.exitScreen(this, EXIT_GM_GM);
						}

			}
		}

		if (!isComplete() && !victory && paused) {
			if (!prevPaused) {
				prevPaused = paused;
				return false;
			}

			String updateString = manager.getMenuManager().update().orElse("");
			if (!updateString.contains("ACTION")) {
				if (updateString.contains("Resume")) {
					paused = false;
					playerIsReady = false;
				} else if (updateString.contains("Restart")) {
					reset();
				} else if (updateString.contains("Main Menu")) {
					listener.exitScreen(this, EXIT_GM_MM);
				}
			}
		}

		//Checks to see if player has selected the button on the starting screen
		if(!playerIsReady){
			if(checkReady()){
				playerIsReady = true;
			}
			else{
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks to see if the player has pressed any button. This is used to
	 * indicate that the player is ready to start the level.
	 *
	 * @return whether the player has pressed a button
	 */
	public boolean checkReady(){
		if (paused) return false;
		if (loadLevel.contains("multi"))
			return InputControllerManager.getInstance().inputControllers().stream()
				.map(controller ->controller.getRightGrab() || controller
						.getLeftGrab())
				.reduce(true,(acc,el)->acc && el);
		else
			return InputControllerManager.getInstance().getController(0)
					.getRightGrab() || InputControllerManager.getInstance()
					.getController(0).getLeftGrab();
	}

	public void printHelp(){
		//Display waiting text if not ready
		//displayFont.setColor(Color.YELLOW);
		//canvas.drawText("Hold RB/LB \n to start!", displayFont, initFlowX * worldScale.x, initFlowY * worldScale.y + 150f);
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

		if (InputControllerManager.getInstance().getController(0).isZKeyPressed()) {
			particleController.testEffect.spawn(10,18);
		}

		if (!paused) {

			if (!victory) currentTime += dt;

			if (multiplayer) {
				if (slothList.stream().map(SlothModel::isDismembered).reduce
						(Boolean::logicalAnd).orElse(true)) {
					reset();
				}
			} else {
				if (framesToDie < 0) {
					reset();
				}
				if (slothList.stream().map(SlothModel::isDismembered).reduce
						(Boolean::logicalAnd).orElse(true)) {
					framesToDie--;
				}
			}
			// Prevent control input if flow is win
			if (!collisions.isFlowWin()) {
				for (int i = 0; i < slothList.size(); i++){
					SlothModel sloth = slothList.get(i);
					// Process actions in object model
					Body leftCollisionBody = collisions.getLeftBody(sloth);
					Body rightCollisionBody = collisions.getRightBody(sloth);
					sloth.setLeftHori(InputControllerManager.getInstance().getController(i).getLeftHorizontal());
					sloth.setLeftVert(InputControllerManager.getInstance().getController(i).getLeftVertical());
					sloth.setRightHori(InputControllerManager.getInstance().getController(i).getRightHorizontal());
					sloth.setRightVert(InputControllerManager.getInstance().getController(i).getRightVertical());
					sloth.setLeftGrab(InputControllerManager.getInstance().getController(i).getLeftGrab());
					sloth.setRightGrab(InputControllerManager.getInstance().getController(i).getRightGrab());
					sloth.setSafeGrab(InputControllerManager.getInstance().getController(i).isBottomButtonPressed(), leftCollisionBody, rightCollisionBody, world);
					sloth.setOneGrab(InputControllerManager.getInstance().getController(i).getRightGrab());
					sloth.setLeftStickPressed(InputControllerManager.getInstance().getController(i).getLeftStickPressed());
					sloth.setRightStickPressed(InputControllerManager.getInstance().getController(i).getRightStickPressed());

					if (sloth.isLeftGrab()) {
						sloth.grab(world, leftCollisionBody, true);
					} else {
						sloth.releaseLeft(world);
					}
					if (sloth.isRightGrab()) {
						sloth.grab(world, rightCollisionBody, false);
					} else {
						sloth.releaseRight(world);
					}

					if (sloth.shouldDie) {
						if (sloth.dismember(world)) {
							ghostSound.play();
							if (!multiplayer)
								fallDeathHeight = sloth.getMainBody().getPosition()
									.y -
									NEAR_FALL_DEATH_DISTANCE;
						}
					}

					// Check if flow is falling
					float slothY = sloth.getBody().getPosition().y;
					if (slothY < fallDeathHeight + NEAR_FALL_DEATH_DISTANCE) {
						if (slothY < fallDeathHeight) {
							if (multiplayer) {
								sloth.dismember(world);
							} else {
								reset();
							}
						} else {
							if (!multiplayer) {
								float normalizedDistanceFromDeath = (slothY -
										fallDeathHeight) / NEAR_FALL_DEATH_DISTANCE;
								coverOpacity = 2 * (1 - normalizedDistanceFromDeath);
								if (coverOpacity > 1) coverOpacity = 1;
								SoundController.getInstance().setVolume("fallmusic", (1 -
										normalizedDistanceFromDeath)
										*MAX_MUSIC_VOLUME*2);
								SoundController.getInstance().setPitch
										("fallmusic",
												normalizedDistanceFromDeath
														*0.1f+0.8f);
								if (playingMusic)
									SoundController.getInstance().setVolume("bgmusic",
											normalizedDistanceFromDeath*MAX_MUSIC_VOLUME);
							}
						}
					} else {
						SoundController.getInstance().setVolume("fallmusic", 0);
						if (playingMusic)
							SoundController.getInstance().setVolume("bgmusic",
									MAX_MUSIC_VOLUME);
						if ((playerIsReady || paused) && (!collisions.isFlowWin())) {
							coverOpacity = 0;
						}
					}

					if (isFailure()) {
						framesToDie--;
						if (sloth.dismember(world)) {
                            ghostSound.play();
                            fallDeathHeight = sloth.getMainBody().getPosition()
									.y -
									NEAR_FALL_DEATH_DISTANCE;
                        }
                    }

                    if (multiplayer &&  (sloth.getMainBody().getPosition().x
							< cameraX - bounds.width/2f)) {
						sloth.dismember(world);
					}
                    Body rightHand = sloth.getRightHand();
					Obstacle rightArm = sloth.getRightArm();
					if(rightHand != null && rightArm != null)
                    	particleController.handTrailEffect.spawn(rightHand.getPosition().x, rightHand.getPosition().y, rightArm.getAngle());
					Body leftHand = sloth.getLeftHand();
					Obstacle leftArm = sloth.getLeftArm();
					if(leftHand != null && leftArm != null)
						particleController.handTrailEffect.spawn(leftHand.getPosition().x, leftHand.getPosition().y, leftArm.getAngle());
                }
            }
			leftNewGrab = (!leftPrevGrab && slothList.get(0).isActualLeftGrab());
			rightNewGrab = (!rightPrevGrab && slothList.get(0).isActualRightGrab());
			leftPrevGrab = slothList.get(0).isActualLeftGrab();
			rightPrevGrab = slothList.get(0).isActualRightGrab();
			if (leftNewGrab) {
				currentGrabs++;
			}
			if (rightNewGrab) {
				currentGrabs++;
			}

			currentTime += dt;
			if (currentTime - fogTime > .1f) {
               particleController.fogEffect.spawn(cameraX, cameraY);

                fogTime = currentTime;
            }
            particleController.update(dt);


            //#TODO Collision states check
            if (!collisions.isFlowWin()) setFailure(collisions.isFlowKill());

            if (!isFailure() && collisions.isFlowWin()) {
                if (!owl.isDoingVictory()) {
                    victorySound.play(0.10f);
                    SoundController.getInstance().stop("bgmusic");
                    victorySloth = collisions.winningSloth();
					SlothModel sloth = slothList.get(victorySloth);
                    sloth.releaseLeft(world);
                    sloth.releaseRight(world);
                    if (collisions.getLeftBody( sloth) != null && collisions.getLeftBody( sloth).equals(owl.getBody()))
                        sloth.grab(world, owl.getBody(), true);
                    else if (collisions.getRightBody( sloth) != null && collisions.getRightBody( sloth).equals(owl.getBody()))
                        sloth.grab(world, owl.getBody(), false);
                    else {
                        sloth.grab(world, owl.getBody(), true);
                        sloth.grab(world, owl.getBody(), false);
                    }
                }

                coverOpacity = owl.doVictory();

                if (owl.didVictory()) {
                    setComplete(true);
                }
            }

            slothList.forEach(sloth -> {
                if (sloth.isGrabbedEntity() && !collisions.isFlowWin()) {
					releaseSound.play();
                }

                if (sloth.isReleasedEntity() && !collisions.isFlowWin()) {
//                    releaseSound.play();
                }

                // Normal physics
                sloth.doThePhysics();
            });

            // Play arm sound based on arm power
//			float slothPower = sloth.getPower();
//			SoundController.getInstance().setVolume("armmusic", slothPower * 0.2f);
//			SoundController.getInstance().setPitch("armmusic", 0.9f + slothPower * 0.9f);

            // Play wind sound based on flow speed
            float slothSpeed = slothList
                    .parallelStream()
                    .map(sloth -> sloth.getMainBody().getLinearVelocity().len())
                    .reduce((acc, el) -> acc + el)
                    .orElse(0f) / slothList.size();
            float windVolume = slothSpeed / 180f;
            this.windVolume += (windVolume - this.windVolume) * 0.04f;
            if (this.windVolume > 1) this.windVolume = 1;
            SoundController.getInstance().setVolume("windmusic", this.windVolume);
            SoundController.getInstance().setPitch("windmusic", 1.0f + this.windVolume * 0.9f);

            // If we use sound, we must remember this.
            SoundController.getInstance().update();

            if (isComplete()) {
				SoundController instance = SoundController.getInstance();
				if (instance.isActive("bgmusic"))
					instance.stop("bgmusic");
                victory = true;
                playerIsReady = false;
				bounds.width = 9.6f;
				bounds.height = 5.4f;
				owlOPosX =  6.72f;
				for (SlothModel sloth : slothList) {
					entities.remove(sloth);
				}
				entities.remove(owl);

				setWorldScale(canvas);
				for(Entity e: entities){
					e.setDrawScale(worldScale);
				}
                float recordT = currentTime;
                int recordG = currentGrabs -1; // cuz grabbing the owl adds an extra grab
				instance.play("bgmusic", "sound/music/levelselect.ogg", true,
						MAX_MUSIC_VOLUME);
                if (storeTimeRecords) {
					// TODO: work this into end of level screen
//                    if (records.setRecord(loadLevel, recordT)) {
//                        System.out.println("New record time for this level!");
//                    }
//                    if (records.setRecordGrabs(loadLevel, recordG) && storeTimeRecords) {
//						System.out.println("New record grabs for this level!");
//					}
                }
            }
        }
    }

    public void draw(float delta) {
		canvas.clear();

		if (victory) {
			// TODO
			camTrans.setToTranslation(-1 * owlOPosX * worldScale.x
					, -1 * owlOPosY * worldScale.y);

			camTrans.translate(canvas.getWidth() / 2f, canvas.getHeight() / 2f);
			canvas.getCampos().set(owlOPosX * worldScale.x
					, owlOPosY * worldScale.y );

			canvas.begin();
			canvas.draw(background);
			canvas.end();
			canvas.begin(camTrans);
			for(Entity e : entities){
				e.draw(canvas);
			}
			canvas.end();
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Color coverColor = new Color(0, 0, 0, 0.25f);
			canvas.drawRectangle(coverColor, 0, 0, canvas.getWidth(), canvas
					.getHeight());
			Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			canvas.begin();
			victoryCutscene.draw(canvas);
			canvas.end();
//			Gdx.gl.glDisable(GL20.GL_BLEND);
			canvas.begin();
			manager.getMenuManager().draw();
			canvas.end();
		}
		else if (paused) {
			canvas.begin();
			canvas.draw(background);
			canvas.end();
			canvas.begin();
			manager.getMenuManager().draw();
			canvas.end();
		}
		else {

			canvas.begin();
			canvas.draw(background);
			canvas.end();

			float slothX = -100000;
			float slothY = -100000;
			boolean foundOne = false;
			for (SlothModel sloth : slothList) {
				if (sloth.isDismembered()) continue;
				if (sloth.getBody().getPosition().x > slothX) {
					slothX = sloth.getBody().getPosition().x;
					slothY = sloth.getBody().getPosition().y;
					foundOne = true;
				}
			}
			if (!foundOne) {
				slothX = slothList.get(0).getBody().getPosition().x;
				slothY = slothList.get(0).getBody().getPosition().y;
			}

			float velocityModifier = 0.18f;
			if (multiplayer) {
				velocityModifier = 0.01f;
			}
			cameraVelocityX = cameraVelocityX * 0.4f + (slothX - cameraX) * velocityModifier;
			cameraVelocityY = cameraVelocityY * 0.4f + (slothY - cameraY) * velocityModifier;
			cameraX += cameraVelocityX;
			cameraY += cameraVelocityY;

			// Check for camera in bounds
			// Y Checks
			if (cameraY - bounds.height / 2f < levelModel.getMinY()) {
				cameraY = levelModel.getMinY() + bounds.height / 2f;
			}

			if (cameraY + bounds.height / 2f > levelModel.getMaxY()) {
				cameraY = levelModel.getMaxY() - bounds.height / 2f;
			}

			// X Checks
			if (cameraX - bounds.width / 2 < levelModel.getMinX()) {
				cameraX = levelModel.getMinX() + bounds.width / 2f;
			}

			if (cameraX + bounds.width / 2f > levelModel.getMaxX()) {
				cameraX = levelModel.getMaxX() - bounds.width / 2f;
			}

			camTrans.setToTranslation(-1 * cameraX * worldScale.x
					, -1 * cameraY * worldScale.y);

			camTrans.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
			canvas.getCampos().set(cameraX * worldScale.x
					, cameraY * worldScale.y);


			//noinspection unchecked
			Collections.sort(entities);


			canvas.begin(camTrans);
			for(Entity e : entities){
				e.setDrawScale(worldScale);
				e.draw(canvas);
			}
			canvas.end();

			int n = 0;
			canvas.beginParticle(camTrans);
			for(Effect e: particleController.effects){
				e.draw(canvas);
				n += e.size();
			}
			canvas.end();




			/*
			int n = particleController.numParticles();
			int total = entities.size() + n;
			int i = 0;
			int j = 0;
			Particle p;
			Entity ent;

			while (i + j < total) {
				p = null;
				ent = null;
				if (i < n) {
					p = particles[i];
				}
				if (j < entities.size()) {
					ent = entities.get(j);
				}
				// FIXME: I think there is an issue with the logic here. or at
				// least semantics. You normally shouldnt do a null compareTo,
				// but it looks like that's whats happening here -
				// trevor
				//noinspection ConstantConditions
				if (ent != null && ent.compareTo(p) < 0) {
					ent.setDrawScale(worldScale);
					ent.draw(canvas);
					j++;
				} else if (p != null) {
					particleController.draw(canvas, p);
					i++;
				}
			}
			*/


			canvas.begin();
			if (!playerIsReady && !paused && coverOpacity <= 0)
				printHelp();
			canvas.end();
			slothList.forEach(x -> x.drawGrab(canvas, camTrans));

			if (debug) {
				canvas.beginDebug(camTrans);
				entities.stream().filter(obj -> obj instanceof Obstacle).forEachOrdered(obj -> ((Obstacle) obj).drawDebug(canvas));
				canvas.endDebug();
				canvas.begin();
				// text
				canvas.drawTextStandard("FPS: " + 1f / delta, 10.0f, 100.0f);
				canvas.end();
				slothList.forEach(sloth -> sloth.drawForces(canvas, camTrans));
			}


			canvas.begin();
			canvas.draw(edgefade, 0.000001f);
			canvas.end();

			if (coverOpacity > 0) {
				Gdx.gl.glEnable(GL20.GL_BLEND);
				displayFont.setColor(Color.WHITE);
				Color coverColor = new Color(0, 0, 0, coverOpacity);
				canvas.drawRectangle(coverColor, 0, 0, canvas.getWidth(), canvas
						.getHeight());
				coverOpacity -= (1 / CYCLES_OF_INTRO);
//				Gdx.gl.glDisable(GL20.GL_BLEND);
				canvas.begin();
				if (!playerIsReady && !paused && !victory)
					canvas.drawTextCentered(levelModel.getTitle(), displayFont, 0f);
				canvas.end();
			}

		}
	}

}