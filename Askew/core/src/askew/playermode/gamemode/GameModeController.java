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
import askew.entity.Entity;
import askew.entity.owl.OwlModel;
import askew.playermode.WorldController;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import askew.entity.obstacle.BoxObstacle;
import askew.entity.obstacle.Obstacle;
import askew.util.json.JSONLoaderSaver;
import askew.playermode.leveleditor.LevelModel;
import askew.entity.sloth.SlothModel;
import askew.util.*;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;

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

	@Setter
	private String loadLevel;

	private PhysicsController collisions;

	private JSONLoaderSaver jsonLoaderSaver;

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
	public void preLoadContent(AssetManager manager) {
		if (platformAssetState != AssetState.EMPTY) {
			return;
		}
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
	public void loadContent(AssetManager manager) {
		if (platformAssetState != AssetState.LOADING) {
			return;
		}

		SoundController sounds = SoundController.getInstance();

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
	public GameModeController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		collisions = new PhysicsController();
		world.setContactListener(collisions);
		sensorFixtures = new ObjectSet<Fixture>();
		loadLevel = GlobalConfiguration.getInstance().getAsString("defaultLevel");
		jsonLoaderSaver = new JSONLoaderSaver();
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		playerIsReady = false;
		collisions.clearGrab();
		Vector2 gravity = new Vector2(world.getGravity() );

		InputController.getInstance().releaseGrabs();
		for(Obstacle obj : objects) {
			if(! (obj instanceof SlothModel))
				obj.deactivatePhysics(world);
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

	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {

			jsonLoaderSaver.setScale(this.scale);
			try {
				LevelModel lm = jsonLoaderSaver.loadLevel(loadLevel);
				System.out.println(loadLevel);
				if (lm == null) {
					lm = new LevelModel();
				}

				for (Entity o : lm.getEntities()) {
					// drawing
					if (o instanceof Obstacle) {
						addObject((Obstacle) o);
					} else {
						System.err.println("Trying to add non-obstacle ent!");
					}
					if (o instanceof SlothModel) {
						sloth = (SlothModel) o;
						sloth.activateSlothPhysics(world);
						collisions.setSloth(sloth);
					}
					if (o instanceof OwlModel) {
						owl = (OwlModel) o;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

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


		//Checks to see if player has selected the button on the starting screen
		if(!playerIsReady){
			if(checkReady()){
				playerIsReady = true;
			}
			else{
				return false;
			}
		}
		//System.out.println("Boop");

		//Put here?

//		if (!isFailure() && avatar.getY() < -1) {
//			setFailure(true);
//			return false;
//		}

		return true;
	}

	/**
	 * Checks to see if the player has pressed any button. This is used to
	 * indicate that the player is ready to start the level.
	 *
	 * @return whether the player has pressed a button
	 */
	public boolean checkReady(){
		InputController the_controller = InputController.getInstance();

		//If the player pressed "A"
		if(the_controller.didBottomButtonPress()){
			return true;
		}
		//If the player pressed "X"
//		else if(the_controller.didLeftButtonPress()){
//			return true;
//		}
		//If the player pressed "B"
		else if(the_controller.didRightButtonPress()){
			return true;
		}
		//If the player pressed "Y"
//		else if(the_controller.didTopButtonPress()){
//			return true;
//		}
		else if(the_controller.getRightGrab()){
			return true;
		}
		else if(the_controller.getLeftGrab()){
			return true;
		}

		return false;
		//return true;
	}

	public void printHelp(){
		//Display waiting text if not ready
		//if (!PlatformController.getPlayerIsReady()) {//Boop
		displayFont.setColor(Color.YELLOW);
//			canvas.begin(); // DO NOT SCALE
		//canvas.drawTextCentered("Hold R at the start!", displayFont, 0.0f);
		//canvas.drawText(String text, BitmapFont font, float x, float y) {
		SlothModel sloth = getSloth();
//			float x_pos = -1 * sloth.getBody().getPosition().x * sloth.getDrawScale().x;
//			float y_pos = -1 * sloth.getBody().getPosition().y * sloth.getDrawScale().y;
		float x_pos = sloth.getBody().getPosition().x;
		float y_pos = sloth.getBody().getPosition().y;
		canvas.drawText("Hold R \n to start!", displayFont, 0.0f, 550.0f);
//			canvas.end();
		//}
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
		// Process actions in object model
		sloth.setLeftHori(InputController.getInstance().getLeftHorizontal());
		sloth.setLeftVert(InputController.getInstance().getLeftVertical());
		sloth.setRightHori(InputController.getInstance().getRightHorizontal());
		sloth.setRightVert(InputController.getInstance().getRightVertical());
		sloth.setLeftGrab(InputController.getInstance().getLeftGrab());
		sloth.setRightGrab(InputController.getInstance().getRightGrab());

		//#TODO Collision states check
		setComplete(collisions.isComplete());

		Body leftCollisionBody = collisions.getLeftBody();
		Body rightCollisionBody = collisions.getRightBody();

		if ((leftCollisionBody != null && leftCollisionBody.getUserData() == owl) || (rightCollisionBody != null && rightCollisionBody.getUserData() == owl)) {
			System.out.println("VICTORY");
			listener.exitScreen(this, EXIT_GM_LE);
		}

		// Physics tiem
		// Gribby grab
		if (sloth.isLeftGrab()) {
			sloth.grab(world,leftCollisionBody, true);
		} else {
			sloth.releaseLeft(world);
		}

		if (sloth.isRightGrab()) {
			sloth.grab(world,rightCollisionBody, false);
		} else {
			sloth.releaseRight(world);
		}

		// Normal physics
		sloth.doThePhysics();

		// If we use sound, we must remember this.
		SoundController.getInstance().update();
	}

	public void draw(float delta){
		canvas.clear();
		camTrans.setToTranslation(-1 * sloth.getBody().getPosition().x * sloth.getDrawScale().x
				, -1 * sloth.getBody().getPosition().y * sloth.getDrawScale().y);
		camTrans.translate(canvas.getWidth()/2,canvas.getHeight()/2);
		canvas.begin(camTrans);

		for(Obstacle obj : objects) {
			obj.draw(canvas);
		}



		if (!playerIsReady)
			printHelp();
		canvas.end();

		if (debug) {
			canvas.beginDebug(camTrans);
			for(Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}

			canvas.endDebug();
			sloth.drawForces(canvas, camTrans);
		}

	}

}