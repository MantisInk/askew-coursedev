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
import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.Obstacle;
import askew.entity.owl.OwlModel;
import askew.entity.sloth.SlothModel;
import askew.playermode.WorldController;
import askew.playermode.leveleditor.LevelModel;
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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.util.Collections;

/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class GameModeController extends WorldController {


	Affine2 camTrans = new Affine2();

	/** Track asset loading from all instances and subclasses */
	private AssetState platformAssetState = AssetState.EMPTY;

	/** Track asset loading from all instances and subclasses */
	@Getter
	private static boolean playerIsReady = false;
	protected boolean paused = false;
	private boolean prevPaused = false;
	// fern selection indicator locations for pause menu options
	private Vector2[] pause_locs = {new Vector2(11f,4.8f), new Vector2(9f,3.9f), new Vector2(11f,3f)};

	public static final String[] GAMEPLAY_MUSIC = new String[] {
			"sound/music/askew.ogg",
			"sound/music/flowwantshisorherbaby.ogg",
			"sound/music/youdidit.ogg"
	};

	public static final String GRAB_SOUND = "sound/effect/grab.wav";
	public static final String FALL_MUSIC = "sound/music/fallingtoyourdeath" +
			".ogg";

	Sound grabSound;

	@Setter
	protected String loadLevel, DEFAULT_LEVEL;
	private LevelModel levelModel; 				// LevelModel for the level the player is currently on
	private int numLevel, MAX_LEVEL; 	// track int val of lvl #

	private float currentTime, recordTime;	// track current and record time to complete level
	private boolean storeTimeRecords;

	protected PhysicsController collisions;

	private JSONLoaderSaver jsonLoaderSaver;
	private float initFlowX;
	private float initFlowY;
	private int PAUSE_RESUME = 0;
	private int PAUSE_RESTART = 1;
	private int PAUSE_MAINMENU = 2;
	private int pause_mode = PAUSE_RESUME;
	protected Texture background;
	private Texture pauseTexture;
	private Texture fern;
	private static final float NEAR_FALL_DEATH_DISTANCE = 9;
	private static final float LOWEST_ENTITY_FALL_DEATH_THRESHOLD = 12;
	private static final float CYCLES_OF_INTRO = 50f;
	private float fallDeathHeight;
	private String selectedTrack;
	private String lastLevel;

	//For playtesting control schemes
	private String typeMovement;
	private int currentMovement;
	private String typeControl;
	private int currentControl;

	private int control_three_wait;
	private int UI_WAIT = 5;

	/** The opacity of the black text covering the screen. Game can start
	 * when this is zero. */
	private float coverOpacity;

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
		manager.load(FALL_MUSIC, Sound.class);

		manager.load(GRAB_SOUND, Sound.class);

		platformAssetState = AssetState.LOADING;
		jsonLoaderSaver.setManager(manager);
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
		SoundController.getInstance().allocate(manager, FALL_MUSIC);

		grabSound = Gdx.audio.newSound(Gdx.files.internal(GRAB_SOUND));

		background = manager.get("texture/background/background1.png", Texture.class);
		pauseTexture = manager.get("texture/background/pause.png", Texture.class);
		fern = manager.get("texture/background/fern.png");

		super.loadContent(manager);
		platformAssetState = AssetState.COMPLETE;
	}


	// Physics constants for initialization
	/** The new heavier gravity for this world (so it is not so floaty) */
	private static final float  DEFAULT_GRAVITY = -10.7f;



	// Physics objects for the game
	/** Reference to the character avatar */

	protected SlothModel sloth;
	private static OwlModel owl;

	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public GameModeController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		collisions = new PhysicsController();
		world.setContactListener(collisions);
		sensorFixtures = new ObjectSet<Fixture>();
		DEFAULT_LEVEL = GlobalConfiguration.getInstance().getAsString("defaultLevel");
		MAX_LEVEL = GlobalConfiguration.getInstance().getAsInt("maxLevel");
		loadLevel = DEFAULT_LEVEL;
		storeTimeRecords = GlobalConfiguration.getInstance().getAsBoolean("storeTimeRecords");
		jsonLoaderSaver = new JSONLoaderSaver();

		// TODO: kill later
		typeMovement = "Current movement is: "+"0";
		currentMovement = 0;
		typeControl = "Current control is: "+"0";
		currentControl = 0;

		control_three_wait = 0;
	}

	public void setLevel() {
		//numLevel = lvl;
		int lvl = GlobalConfiguration.getInstance().getCurrentLevel();
		if (lvl == 0) {
			loadLevel = DEFAULT_LEVEL;
		} else if (lvl > MAX_LEVEL) {
			loadLevel = "level"+MAX_LEVEL;
			System.out.println("MM");
			listener.exitScreen(this, EXIT_GM_MM);
		} else
			loadLevel = "level"+lvl;
	}

	public void pause(){
		prevPaused = paused;
		if (!paused) {
			paused = true;
			pause_mode = PAUSE_RESUME;
		}
		else {
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
		Gdx.input.setCursorCatched(true);
		coverOpacity = 2f; // start at 2 for 1 second of full black
		playerIsReady = false;
		paused = false;
		collisions.clearGrab();
		Vector2 gravity = new Vector2(world.getGravity() );

		InputController.getInstance().releaseGrabs();
		fallDeathHeight = Float.MAX_VALUE;
		for(Entity obj : objects) {
			if( (obj instanceof Obstacle && !(obj instanceof SlothModel)))
				((Obstacle)obj).deactivatePhysics(world);
			float potentialFallDeath = obj.getY() -
					LOWEST_ENTITY_FALL_DEATH_THRESHOLD;
			if (potentialFallDeath < fallDeathHeight) {
				fallDeathHeight = potentialFallDeath;
			}
		}

		objects.clear();
		world.dispose();
		world = new World(gravity,false);
		if(collisions == null){
			collisions = new PhysicsController();
		}
		collisions.reset();

		world.setContactListener(collisions);
		setComplete(false);
		setFailure(false);
		setLevel();
		populateLevel();

		// Setup sound
		SoundController instance = SoundController.getInstance();
		if (instance.isActive("menumusic")) instance.stop("menumusic");
		if (instance.isActive("bgmusic")) instance.stop("bgmusic");
		if (selectedTrack != null) {
			instance.play("bgmusic", selectedTrack, true);

		}

		if (!instance.isActive("fallmusic")) {
			instance.play("fallmusic",
					FALL_MUSIC, true);
			instance.setVolume("fallmusic",0);
		} else {
			instance.setVolume("fallmusic",0);
		}
	}

	/**
	 * Lays out the game geography.
	 */
	protected void populateLevel() {
		jsonLoaderSaver.setScale(this.worldScale);
		// Are we loading a new level?
		if (lastLevel == null || !lastLevel.equals(loadLevel)) {
			selectedTrack = GAMEPLAY_MUSIC[(int)Math.floor(GAMEPLAY_MUSIC.length * Math.random())];
		}
		lastLevel = loadLevel;
		try {
			float level_num = Integer.parseInt(loadLevel.substring(5));
			// TODO: move to preupdate
			if (level_num==0){
				System.out.println("Tutorial");
				listener.exitScreen(this,EXIT_GM_TL);
				return;
			}

			levelModel = jsonLoaderSaver.loadLevel(loadLevel);
			recordTime = levelModel.getRecordTime();
			if (levelModel == null) {
				levelModel = new LevelModel();
			}

			for (Entity o : levelModel.getEntities()) {
				// drawing

				addObject( o);
				if (o instanceof SlothModel) {
					sloth = (SlothModel) o;
					sloth.activateSlothPhysics(world);
					collisions.setSloth(sloth);
					initFlowX = sloth.getX();
					initFlowY = sloth.getY();

					sloth.setControlMode(currentControl);
					sloth.setMovementMode(currentMovement);
				}
				if (o instanceof OwlModel) {
					owl = (OwlModel) o;
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		currentTime = 0f;

	}

	/**For drawing force lines*/
	public SlothModel getSloth(){return sloth;}

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

		InputController input = InputController.getInstance();

		if (input.didLeftDPadPress() || input.isLKeyPressed()) {
			System.out.println("LE");
			listener.exitScreen(this, EXIT_GM_LE);
			return false;
		} else if (input.didTopDPadPress()) {
			System.out.println("MM");
			listener.exitScreen(this, EXIT_GM_MM);
			return false;
		} else if (input.didBottomDPadPress() && !paused) {
			System.out.println("reset");
			reset();
		}

		if (paused) {
			if (!prevPaused) {
				prevPaused = paused;
				return false;
			}
			//InputController input = InputController.getInstance();
			if (input.didBottomButtonPress() && pause_mode == PAUSE_RESUME) {
				paused = false;
				playerIsReady = false;
			} else if (input.didBottomButtonPress() && pause_mode == PAUSE_RESTART) {
				reset();
			} else if (input.didBottomButtonPress() && pause_mode == PAUSE_MAINMENU) {
				System.out.println("MM");
				listener.exitScreen(this, EXIT_GM_MM);
			}

			if ((input.didTopDPadPress() || input.didUpArrowPress()) && pause_mode > 0) {
				pause_mode--;
			}
			if ((input.didBottomDPadPress() || input.didDownArrowPress()) && pause_mode < 2) {
				pause_mode++;
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
		InputController theController = InputController.getInstance();

		if (paused)
			return false;

		//If the player pressed "RB"
		if(theController.getRightGrab()){
			return true;
		}
		//If the player pressed "LB"
		else if(theController.getLeftGrab()){
			return true;
		}

		return false;
	}

	public void printHelp(){
		//Display waiting text if not ready
		displayFont.setColor(Color.YELLOW);
		canvas.drawText("Hold RB/LB \n to start!", displayFont, initFlowX * worldScale.x, initFlowY * worldScale.y + 200f);
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
		//Increment UI check
		if(currentControl==2){
			control_three_wait = (control_three_wait+1) % UI_WAIT;
		}

		//Check for change in grabbing movement
		if (InputController.getInstance().isOneKeyPressed()) {
			sloth.setMovementMode(0);
			currentMovement = 0;
			typeMovement = "Current movement is: "+"0";
		}
		if (InputController.getInstance().isTwoKeyPressed()) {
			sloth.setMovementMode(1);
			currentMovement = 1;
			typeMovement = "Current movement is: "+"1";
		}
		if (InputController.getInstance().isThreeKeyPressed()) {
			sloth.setMovementMode(2);
			currentMovement = 2;
			typeMovement = "Current movement is: "+"2";
			control_three_wait = 0;
		}

		//Check for change in arm movement
		if (InputController.getInstance().isZKeyPressed()) {
			sloth.setControlMode(0);
			currentControl = 0;
			typeControl = "Current control is: "+"0";
		}
		if (InputController.getInstance().isXKeyPressed()) {
			sloth.setControlMode(1);
			currentControl = 1;
			typeControl = "Current control is: "+"1";
		}

		if (!paused) {
			// Process actions in object model
			Body leftCollisionBody = collisions.getLeftBody();
			Body rightCollisionBody = collisions.getRightBody();

			// TODO: move to slothmodel
			sloth.setLeftHori(InputController.getInstance().getLeftHorizontal());
			sloth.setLeftVert(InputController.getInstance().getLeftVertical());
			sloth.setRightHori(InputController.getInstance().getRightHorizontal());
			sloth.setRightVert(InputController.getInstance().getRightVertical());
			boolean didSafe = InputController.getInstance()
					.isBottomButtonPressed();
			if (sloth.controlMode == 0) {
				// TODO: Make more elegant - trevor
				sloth.setLeftGrab(InputController.getInstance().getLeftGrab());
				sloth.setRightGrab(InputController.getInstance().getRightGrab());
			} else {
				if (!didSafe) {
					sloth.setOneGrab(InputController.getInstance()
							.getRightGrab());
				}
				sloth.setSafeGrab(didSafe, leftCollisionBody,
						rightCollisionBody, world);
			}
			sloth.setLeftStickPressed(InputController.getInstance().getLeftStickPressed());
			sloth.setRightStickPressed(InputController.getInstance().getRightStickPressed());
			currentTime += dt;

			//#TODO Collision states check
			if (!collisions.isFlowWin())
				setFailure(collisions.isFlowKill());

			if (!isFailure() && collisions.isFlowWin()) {
				if (!owl.isDoingVictory()) {
					sloth.releaseLeft(world);
					sloth.releaseRight(world);
					if (collisions.getLeftBody() != null && collisions.getLeftBody().equals(owl.getBody()))
						sloth.grab(world, owl.getBody(), true);
					else if (collisions.getRightBody() != null && collisions.getRightBody().equals(owl.getBody()))
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
			} else {
				// Physics tiem
				// Gribby grab
				if (sloth.controlMode == 0 || !didSafe) {
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
				}
			}

			if (sloth.isGrabbedEntity()) {
				grabSound.play();
			}

			// Normal physics
			sloth.doThePhysics();

			// If we use sound, we must remember this.
			SoundController.getInstance().update();

			// Check if flow is falling
			float slothY = sloth.getBody().getPosition().y;
			if (slothY < fallDeathHeight + NEAR_FALL_DEATH_DISTANCE) {
				if (slothY < fallDeathHeight) {
					reset();
				} else {
					float normalizedDistanceFromDeath = (slothY -
							fallDeathHeight) / NEAR_FALL_DEATH_DISTANCE;
					coverOpacity = 2 * (1 - normalizedDistanceFromDeath);
					if (coverOpacity > 1) coverOpacity = 1;
					SoundController.getInstance().setVolume("fallmusic", 1 -
							normalizedDistanceFromDeath);
					SoundController.getInstance().setVolume("bgmusic",
							normalizedDistanceFromDeath);
				}
			} else {
				SoundController.getInstance().setVolume("fallmusic", 0);
				SoundController.getInstance().setVolume("bgmusic",
						1);
				if ((playerIsReady || paused) && (collisions.isFlowKill() || !collisions.isFlowWin())) {
					coverOpacity = 0;
				}
			}

			if (isComplete()) {
				float record = currentTime;
				if (record < levelModel.getRecordTime() && storeTimeRecords) {
					levelModel.setRecordTime(record);
					if (jsonLoaderSaver.saveLevel(levelModel, loadLevel))
						System.out.println("New record time for this level!");
				}
				int current = GlobalConfiguration.getInstance().getCurrentLevel();
				GlobalConfiguration.getInstance().setCurrentLevel(current + 1);
				System.out.println("GG");
				listener.exitScreen(this, EXIT_GM_GM);
			}

			if (isFailure()) {
				if (sloth != null) {
					if (sloth.dismember(world))
						grabSound.play();
				}
//				reset();
			}
		}
	}

	public void draw(float delta){
		canvas.clear();

		canvas.begin();
		canvas.draw(background);
		canvas.end();

		camTrans.setToTranslation(-1 * sloth.getBody().getPosition().x * worldScale.x
				, -1 * sloth.getBody().getPosition().y * worldScale.y);

		camTrans.translate(canvas.getWidth()/2,canvas.getHeight()/2);
    
		canvas.getCampos().set( sloth.getBody().getPosition().x * worldScale.x
				, sloth.getBody().getPosition().y * worldScale.y);
		canvas.begin(camTrans);
		//canvas.draw(background, Color.WHITE, .25f*background.getWidth(),.75f * background.getHeight(),initFlowX*worldScale.x,initFlowY*worldScale.y,background.getWidth(), background.getHeight());


		Collections.sort(objects);
		for(Entity obj : objects) {
			obj.setDrawScale(worldScale);
			obj.draw(canvas);
		}

		if (!playerIsReady && !paused && coverOpacity <= 0)
			printHelp();
		canvas.end();
		sloth.drawGrab(canvas, camTrans);

		canvas.begin();
		canvas.drawTextStandard("current time:    "+currentTime, 10f, 70f);
		canvas.drawTextStandard("record time:     "+recordTime,10f,50f);
    
		//Draw control schemes
		canvas.drawTextStandard(typeMovement, 10f, 700f);
		canvas.drawTextStandard(typeControl,10f,680f);
		canvas.end();

		if (debug) {
			canvas.beginDebug(camTrans);
			for(Entity obj : objects) {
				if( obj instanceof  Obstacle){
					((Obstacle)obj).drawDebug(canvas);
				}

			}

			canvas.endDebug();
			canvas.begin();
			// text
			canvas.drawTextStandard("FPS: " + 1f/delta, 10.0f, 100.0f);
			canvas.end();
			sloth.drawForces(canvas, camTrans);
		}

		if (coverOpacity > 0) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			displayFont.setColor(Color.WHITE);
			Color coverColor = new Color(0,0,0,coverOpacity);
			canvas.drawRectangle(coverColor,0,0,canvas.getWidth(), canvas
					.getHeight());
			coverOpacity -= (1/CYCLES_OF_INTRO);
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

			canvas.draw(fern, Color.WHITE,fern.getWidth()/2, fern.getHeight()/2,
					pause_locs[pause_mode].x * worldScale.x, pause_locs[pause_mode].y* worldScale.y,
					0,worldScale.x/fern.getWidth(),worldScale.y/fern.getHeight());

			canvas.end();
		}

		canvas.begin();
		if (prevPaused && !paused && !playerIsReady)
			printHelp();
		canvas.end();
	}

}