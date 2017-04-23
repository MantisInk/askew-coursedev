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
import askew.entity.obstacle.SimpleObstacle;
import askew.entity.owl.OwlModel;
import askew.entity.sloth.SlothModel;
import askew.entity.tree.Trunk;
import askew.entity.wall.WallModel;
import askew.util.SoundController;
import askew.util.json.JSONLoaderSaver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ObjectSet;
import lombok.Getter;

/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class TutorialModeController extends GameModeController {

	Affine2 camTrans = new Affine2();

	/** Track asset loading from all instances and subclasses */
	private AssetState platformAssetState = AssetState.EMPTY;
	private MantisAssetManager manager;

	/** Track asset loading from all instances and subclasses */
	@Getter
	private static boolean playerIsReady = false;
	private boolean paused = false;
	private boolean prevPaused = false;
	// fern selection indicator locations for pause menu options
	private Vector2[] pause_locs = {new Vector2(11f,4.8f), new Vector2(9f,3.9f), new Vector2(11f,3f)};

	private PhysicsController collisions;

	private JSONLoaderSaver jsonLoaderSaver;
	private float initFlowX;
	private float initFlowY;
	private int PAUSE_RESUME = 0;
	private int PAUSE_RESTART = 1;
	private int PAUSE_MAINMENU = 2;
	private int pause_mode = PAUSE_RESUME;
	private Texture background;
	private Texture pauseTexture;
	private Texture fern;

	private final int DID_NOTHING = 0;
	private final int MOVED_LEFT = 1;
	private final int MOVED_RIGHT = 2;
	private final int GRABBED_LEFT = 3;
	private final int GRABBED_RIGHT = 4;
	private final int SWING_LEFT = 5;
	private final int REGRABBED_LEFT = 6;
	private int stepsDone = DID_NOTHING;

	private final float CONTROLLER_DEADZONE = 0.15f;

	TextureRegion[] joystickTextures;
	TextureRegion[] LeftBumperTextures;
	TextureRegion[] RightBumperTextures;

	private int joystick_idx;
	private int left_bumper_idx;
	private int right_bumper_idx;


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
		manager.load("sound/music/askew.ogg", Sound.class);
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

		//SoundController sounds = SoundController.getInstance();
		SoundController.getInstance().allocate(manager, "sound/music/askew.ogg");
		background = manager.get("texture/background/background1.png", Texture.class);
		pauseTexture = manager.get("texture/background/pause.png", Texture.class);
		fern = manager.get("texture/background/fern.png");

		this.manager = manager;

		super.loadContent(manager);
		platformAssetState = AssetState.COMPLETE;
	}


	// Physics constants for initialization
	/** The new heavier gravity for this world (so it is not so floaty) */
	private static final float  DEFAULT_GRAVITY = -10.7f;



	// Physics objects for the game
	/** Reference to the character avatar */

	private static SlothModel sloth;
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
	public TutorialModeController() {
		super();
		setDebug(false);
		setComplete(false);
		setFailure(false);
		collisions = new PhysicsController();
		world.setContactListener(collisions);
		sensorFixtures = new ObjectSet<Fixture>();

		jsonLoaderSaver = new JSONLoaderSaver();



	}

	public void pause(){
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

		playerIsReady = false;
		paused = false;
		collisions.clearGrab();
		Vector2 gravity = new Vector2(world.getGravity() );

		InputController.getInstance().releaseGrabs();
		for(Entity obj : objects) {
			if( (obj instanceof Obstacle && !(obj instanceof SlothModel)))
				((Obstacle)obj).deactivatePhysics(world);
		}

		objects.clear();
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		if(collisions == null){
			collisions = new PhysicsController();
		}
		collisions.reset();

		world.setContactListener(collisions);
		setComplete(false);
		setFailure(false);
		populateLevel();
//		if (!SoundController.getInstance().isActive("bgmusic"))
//			SoundController.getInstance().play("bgmusic","sound/music/askew.ogg",true);

		stepsDone=0;
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {

		float sloth_x = 0;
		float sloth_y = 0;

		//Create sloth
		sloth = new SlothModel(sloth_x ,sloth_y);
		sloth.setTextures(manager);
		addObject(sloth);
		sloth.activateSlothPhysics(world);
		collisions.setSloth(sloth);
		initFlowX = sloth.getX();
		initFlowY = sloth.getY();

		///Create wall
		float[] points = {-4.0f,0.0f, -4.0f,1.0f, 16.0f,1.0f, 16.0f,0.0f};
		WallModel platform = new WallModel(sloth_x-2,sloth_y-1.5f,points,false);
		platform.setTextures(manager);
		addObject(platform);

		Trunk branch = new Trunk(sloth_x-1f,sloth_y+1.5f,11,0.25f,1,0,worldScale,-90.0f);
		branch.setTextures(manager);
		branch.setName("long branch");
		addObject(branch);

		Trunk branch1 = new Trunk(sloth_x-1f,sloth_y+1.5f,3,0.25f,1,0,worldScale,-90.0f);
		branch1.setTextures(manager);
		branch1.setName("short branch");
		addObject(branch1);

		//Create Ebb TODO Replace owl with Ebb
		OwlModel ebb = new OwlModel(sloth_x + 10.5f, sloth_y);
		ebb.setTextures(manager);
		addObject(ebb);

	}

	/**For drawing force lines*/
	public static SlothModel getSloth(){return sloth;}

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

		if (input.didLeftButtonPress() || input.isLKeyPressed()) {
			System.out.println("LE");
			listener.exitScreen(this, EXIT_GM_LE);
			return false;
		} else if (input.didTopButtonPress()) {
			System.out.println("MM");
			listener.exitScreen(this, EXIT_GM_MM);
			return false;
		}

		if (paused) {
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

			if (input.didTopDPadPress() && pause_mode > 0) {
				pause_mode--;
			}
			if (input.didBottomDPadPress() && pause_mode < 2) {
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
		InputController input = InputController.getInstance();
		if (!paused) {
			// Process actions in object model
			sloth.setLeftHori(InputController.getInstance().getLeftHorizontal());
			sloth.setLeftVert(InputController.getInstance().getLeftVertical());
			sloth.setRightHori(InputController.getInstance().getRightHorizontal());
			sloth.setRightVert(InputController.getInstance().getRightVertical());
			sloth.setLeftGrab(InputController.getInstance().getLeftGrab());
			sloth.setRightGrab(InputController.getInstance().getRightGrab());
			switch (stepsDone){
				case DID_NOTHING:
					sloth.setRightHori(0);
					sloth.setRightVert(0);
				case MOVED_LEFT:
					sloth.setLeftGrab(false);
				case MOVED_RIGHT:
					sloth.setRightGrab(false);
					break;
				case GRABBED_LEFT:
					sloth.setLeftGrab(true);
					break;
				case GRABBED_RIGHT:
					sloth.setRightGrab(true);
					//Set right arm to be 0 too?
					break;
				case SWING_LEFT:
					//Let go of left grab
					sloth.setRightGrab(true);
					break;
				case REGRABBED_LEFT:
					break;
				default:
					System.err.println(stepsDone);
			}

			joystick_idx = (joystick_idx++) % 30;
			left_bumper_idx = (left_bumper_idx++) % 20;
			right_bumper_idx = (right_bumper_idx++) % 20;


			//#TODO Collision states check
			setFailure(collisions.isFlowKill());

			Body leftCollisionBody = collisions.getLeftBody();
			Body rightCollisionBody = collisions.getRightBody();

			if (collisions.isFlowWin()) {
				System.out.println("VICTORY");
				setComplete(true);
			}




			// Physics tiem
			// Gribby grab
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

			// Normal physics
			sloth.doThePhysics();

			// If we use sound, we must remember this.
			//SoundController.getInstance().update();

			if (isComplete()) {
				int current = GlobalConfiguration.getInstance().getCurrentLevel();
				GlobalConfiguration.getInstance().setCurrentLevel(current + 1);
				System.out.println("GG");
				listener.exitScreen(this, EXIT_TL_GM);
			}

			if (isFailure()) {
				System.out.println("Fail");
				reset();
			}

			//Increment Steps
			if(stepsDone==DID_NOTHING){
				//Check for left joystick movement
				if(Math.abs(input.getLeftHorizontal())>CONTROLLER_DEADZONE || Math.abs(input.getLeftVertical())>CONTROLLER_DEADZONE){
					stepsDone++;
				}
			}
			else if(stepsDone==MOVED_LEFT){
				//Check for right joystick movement
				if(Math.abs(input.getRightHorizontal())>CONTROLLER_DEADZONE || Math.abs(input.getRightVertical())>CONTROLLER_DEADZONE){
					stepsDone++;
				}
			}
			else if(stepsDone==MOVED_RIGHT){
				//Check for left grab
				if(sloth.isActualLeftGrab()){
					stepsDone++;
				}
			}
			else if(stepsDone==GRABBED_LEFT){
				//Check for right grab
				if(sloth.isActualRightGrab()){
					stepsDone++;
				}
			}
			else if(stepsDone==GRABBED_RIGHT){
				//Check for left joystick movement
				if(Math.abs(input.getLeftHorizontal())>CONTROLLER_DEADZONE || Math.abs(input.getLeftVertical())>CONTROLLER_DEADZONE){
					stepsDone++;
				}
			}
			else if(stepsDone==SWING_LEFT){
				//Check for left again
				if(sloth.isActualLeftGrab()){ //TODO Check for left hand crossing right hand and grabbing
					stepsDone++;
				}
			}
			else if(stepsDone==REGRABBED_LEFT){
				//End of scripted checks, else if statement left here for sanity's sake
				//stepsDone++;
			}
		}
		prevPaused = paused;
	}

	public void setTextures(MantisAssetManager manager) {
		joystickTextures = new TextureRegion[3];
		LeftBumperTextures = new TextureRegion[2];
		RightBumperTextures = new TextureRegion[2];
		Texture bumperUR = manager.get("texture/tutorial/bumperUpRight.png");
		Texture bumperDR = manager.get("texture/tutorial/bumperDownRight.png");
		Texture bumperUL = manager.get("texture/tutorial/bumperUpLeft.png");
		Texture bumperDL = manager.get("texture/tutorial/bumperDownLeft.png");
		Texture stick0 = manager.get("texture/tutorial/joytick0.png");
		Texture stick1 = manager.get("texture/tutorial/joytick1.png");
		Texture stick2 = manager.get("texture/tutorial/joytick2.png");
		//Texture bumper4 = manager.get("texture/tutorial/backarm_moving.png");
		RightBumperTextures[0] = new TextureRegion(bumperUR);
		RightBumperTextures[1] = new TextureRegion(bumperDR);
		LeftBumperTextures [0] = new TextureRegion(bumperUL);
		LeftBumperTextures [1] = new TextureRegion(bumperDL);
		joystickTextures[0] = new TextureRegion(stick0);
		joystickTextures[1] = new TextureRegion(stick1);
		joystickTextures[2] = new TextureRegion(stick2);
		//partTextures[7] = new TextureRegion(managedBackArmMoving);

//		if (bodies.size == 0) {
//			init();
//		} else {
//			for(int ii = 0; ii <= 2; ii++) {
//				((SimpleObstacle)bodies.get(ii)).setTexture(partTextures[partToAsset(ii)]);
//			}
//		}
	}

	public void draw(float delta){
		canvas.clear();

		camTrans.setToTranslation(-1 * sloth.getBody().getPosition().x * worldScale.x
				, -1 * sloth.getBody().getPosition().y * worldScale.y);

    	camTrans.translate(canvas.getWidth()/2,canvas.getHeight()/2);

    	canvas.begin();
		canvas.draw(background);
		canvas.end();

		canvas.begin(camTrans);

		for(Entity obj : objects) {
			if(obj instanceof Trunk){
				if(stepsDone >= MOVED_RIGHT && ((Trunk) obj).getName().equals("short branch")){
					obj.setDrawScale(worldScale);
					obj.draw(canvas);
				}
				if(stepsDone >= GRABBED_LEFT && ((Trunk) obj).getName().equals("long branch")){
					obj.setDrawScale(worldScale);
					obj.draw(canvas);
				}
			}
			else if(obj instanceof OwlModel){
				if(stepsDone >= GRABBED_RIGHT){
					obj.setDrawScale(worldScale);
					obj.draw(canvas);
				}
			}
			else {
				obj.setDrawScale(worldScale);
				obj.draw(canvas);
			}
		}

		if (!playerIsReady && !paused)
			printHelp();
		canvas.end();
		sloth.drawGrab(canvas, camTrans);


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

		canvas.begin();
		TextureRegion joystickTexture = joystickTextures[right_bumper_idx/10];
		TextureRegion LeftBumperTexture = LeftBumperTextures [right_bumper_idx/10];
		TextureRegion RightBumperTexture = RightBumperTextures [right_bumper_idx/10];

		canvas.draw(joystickTexture,500,500);
		canvas.draw(LeftBumperTexture ,600,600);
		canvas.draw(RightBumperTexture ,700,600);
		canvas.end();
	}

}