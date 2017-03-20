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
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.leveleditor.FullAssetTracker;
import edu.cornell.gdiac.physics.leveleditor.JSONLoaderSaver;
import edu.cornell.gdiac.physics.leveleditor.LevelModel;
import edu.cornell.gdiac.physics.platform.sloth.SlothModel;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class PlatformController extends WorldController implements ContactListener {
	/** The texture file for the character avatar (no animation) */
	private static final String DUDE_FILE  = "platform/dude.png";
	/** The texture file for the spinning barrier */
	private static final String BARRIER_FILE = "platform/barrier.png";
	/** The texture file for the bullet */
	private static final String BULLET_FILE  = "platform/bullet.png";
	/** The texture file for the bridge plank */
	private static final String ROPE_FILE  = "platform/ropebridge.png";
	/** The texture file for the vine */
	private static final String VINE_FILE  = "platform/vine.png";
	/** The texture file for the branch */
	private static final String BRANCH_FILE  = "platform/branch.png";

	/** The sound file for a jump */
	private static final String JUMP_FILE = "platform/jump.mp3";
	/** The sound file for a bullet fire */
	private static final String PEW_FILE = "platform/pew.mp3";
	/** The sound file for a bullet collision */
	private static final String POP_FILE = "platform/plop.mp3";

	private Body leftBody;
	private Body rightBody;

	Affine2 camTrans = new Affine2();

	/** Texture asset for character avatar */
	private TextureRegion avatarTexture;
	/** Texture asset for the bridge plank */
	private TextureRegion bridgeTexture;
	/** Files for the body textures */
	private static final String[] RAGDOLL_FILES = { "ragdoll/trevorhand.png", "ragdoll/ProfWhite.png",
			"ragdoll/trevorarm.png",  "ragdoll/dude.png",
			"ragdoll/tux_thigh.png", "ragdoll/tux_shin.png" };

	/** Texture assets for the body parts */
	private TextureRegion[] bodyTextures;

	//private static Vector2 DOLL_POS = new Vector2( 2.5f,  6.0f);
	//private static Vector2 DOLL_POS = new Vector2( 7.5f,  17.0f);
	private static Vector2 DOLL_POS = new Vector2( 4.0f,  7.8f);
	//7.5f,  17.0f
	//10f, 17.1f

	//5.5f, 7.9f


	/** Track asset loading from all instances and subclasses */
	private AssetState ragdollAssetState = AssetState.EMPTY;

	/** Texture asset for the vine */
	private TextureRegion vineTexture;
	/** Texture asset for the branch */
	private TextureRegion branchTexture;

	/** Track asset loading from all instances and subclasses */
	private AssetState platformAssetState = AssetState.EMPTY;

	/** Track asset loading from all instances and subclasses */
	@Getter
	private static boolean playerIsReady = false;

	@Setter
	private String loadLevel;

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
		manager.load(DUDE_FILE, Texture.class);
		assets.add(DUDE_FILE);
		manager.load(BARRIER_FILE, Texture.class);
		assets.add(BARRIER_FILE);
		manager.load(BULLET_FILE, Texture.class);
		assets.add(BULLET_FILE);
		manager.load(ROPE_FILE, Texture.class);
		assets.add(ROPE_FILE);
		manager.load(VINE_FILE, Texture.class);
		assets.add(VINE_FILE);
		manager.load(BRANCH_FILE, Texture.class);
		assets.add(BRANCH_FILE);

		manager.load(JUMP_FILE, Sound.class);
		assets.add(JUMP_FILE);
		manager.load(PEW_FILE, Sound.class);
		assets.add(PEW_FILE);
		manager.load(POP_FILE, Sound.class);
		assets.add(POP_FILE);

		FullAssetTracker.getInstance().preLoadEverything(manager);


		// SLOTH
		for(int ii = 0; ii < RAGDOLL_FILES.length; ii++) {
			manager.load(RAGDOLL_FILES[ii], Texture.class);
			assets.add(RAGDOLL_FILES[ii]);
		}

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

		avatarTexture = createTexture(manager,DUDE_FILE,false);
		bridgeTexture = createTexture(manager,ROPE_FILE,false);
		vineTexture = createTexture(manager,VINE_FILE,false);
		branchTexture = createTexture(manager, BRANCH_FILE, false);

		SoundController sounds = SoundController.getInstance();
		sounds.allocate(manager, JUMP_FILE);
		sounds.allocate(manager, PEW_FILE);
		sounds.allocate(manager, POP_FILE);

		// SLOTH
		bodyTextures = new TextureRegion[RAGDOLL_FILES.length];
		for(int ii = 0; ii < RAGDOLL_FILES.length; ii++) {
			bodyTextures[ii] =  createTexture(manager,RAGDOLL_FILES[ii],false);
		}

		FullAssetTracker.getInstance().loadEverything(this,manager);

		super.loadContent(manager);
		platformAssetState = AssetState.COMPLETE;
	}

	public static boolean getPlayerIsReady (){return playerIsReady;}

	// Physics constants for initialization
	/** The new heavier gravity for this world (so it is not so floaty) */
	private static final float  DEFAULT_GRAVITY = -10.7f;
	/** The density for most physics objects */
	private static final float  BASIC_DENSITY = 0.0f;
	/** Friction of most platforms */
	private static final float  BASIC_FRICTION = 0.4f;
	/** The restitution for all physics objects */
	private static final float  BASIC_RESTITUTION = 0.1f;
	/** The width of the rope bridge */
	private static final float  BRIDGE_WIDTH = 6.0f;

	// Since these appear only once, we do not care about the magic numbers.
	// In an actual game, this information would go in a data file.
	// Wall vertices

	/*Original Values*/
//	private static final float[][] WALLS = {
//			{16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
//					1.0f,  0.0f,  0.0f,  0.0f,  0.0f, 18.0f},
//			{32.0f, 18.0f, 32.0f,  0.0f, 31.0f,  0.0f,
//					31.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f}
//	};
	private static final float[][] WALLS = {
			{16.0f, 28.0f, 16.0f, 27.0f,  1.0f, 27.0f,
					1.0f,  0.0f,  0.0f,  0.0f,  0.0f, 28.0f},
			{34.0f, 28.0f, 34.0f,  0.0f, 33.0f,  0.0f,
					33.0f, 27.0f, 16.0f, 27.0f, 16.0f, 28.0f}
	};

	/*Original Values*/
//	/** The outlines of all of the platforms */
//	private static final float[][] PLATFORMS = {
//			//   x1     y1  x2    y2     x3   y3    x4    y4
//			//{ 1.0f, 3.0f, 3.0f, 3.0f, 3.0f, 2.5f, 1.0f, 2.5f},
//			{ 1.0f, 2.0f, 3.0f, 2.0f, 3.0f, 1.5f, 1.0f, 1.5f},
//			//{ 6.0f, 4.0f, 9.0f, 4.0f, 9.0f, 2.5f, 6.0f, 2.5f},
//			//{23.0f, 4.0f,31.0f, 4.0f,31.0f, 2.5f,23.0f, 2.5f},
//			//{1.0f, 14.0f,6.0f, 24.0f,31.0f, 12.5f,26.0f, 11.5f},
//			//{26.0f, 5.5f,28.0f, 5.5f,28.0f, 5.0f,26.0f, 5.0f},
//			//{29.0f, 7.0f,31.0f, 7.0f,31.0f, 6.5f,29.0f, 6.5f},
//			//{24.0f, 8.5f,27.0f, 8.5f,27.0f, 8.0f,24.0f, 8.0f},
//			//{29.0f,10.0f,31.0f,10.0f,31.0f, 9.5f,29.0f, 9.5f},
//			//{23.0f,11.5f,27.0f,11.5f,27.0f,11.0f,23.0f,11.0f},
//			//{19.0f,12.5f,23.0f,12.5f,23.0f,12.0f,19.0f,12.0f},
//			//{ 1.0f,12.5f, 7.0f,12.5f, 7.0f,12.0f, 1.0f,12.0f}
//			{ 1.0f,8.5f, 23.0f,8.5f, 23.0f,8.0f, 1.0f,8.0f},
//			//{ 1.0f,10.5f, 7.0f,10.5f, 7.0f,10.0f, 1.0f,10.0f},
//			//{ 23.0f,10.5f, 31.0f,10.5f, 31.0f,10.0f, 23.0f,10.0f},
//	};

	/** The outlines of all of the platforms */
	private static final float[][] PLATFORMS = {
			//   x1     y1  x2    y2     x3   y3    x4    y4
			//{ 1.0f, 3.0f, 3.0f, 3.0f, 3.0f, 2.5f, 1.0f, 2.5f},
			//{ 1.0f, 1.0f, 3.0f, 1.0f, 3.0f, 0.5f, 1.0f, 0.5f},
			{ 1.0f, 1.0f, 13.5f, 1.0f, 13.5f, 0.5f, 1.0f, 0.5f},
			//{ 7.0f, 1.0f, 13.0f, 1.0f, 13.0f, 0.5f, 7.0f, 0.5f},
			//{ 6.0f, 4.0f, 9.0f, 4.0f, 9.0f, 2.5f, 6.0f, 2.5f},
			{ 1.0f,8.5f, 21.0f,8.5f, 21.0f,8.0f, 1.0f,8.0f},
			{ 11.0f,23.5f, 13.0f,23.5f, 29.0f,15.0f, 31.0f,15.0f},
			//{ 1.0f,10.5f, 7.0f,10.5f, 7.0f,10.0f, 1.0f,10.0f},
			//{ 23.0f,10.5f, 31.0f,10.5f, 31.0f,10.0f, 23.0f,10.0f},
	};

	// Other game objects
	/** The goal door position */
	private static Vector2 GOAL_POS = new Vector2(2.5f,15.5f);
	/** The position of the spinning barrier */
	private static Vector2 SPIN_POS = new Vector2(13.0f,12.5f);
	/** The initial position of the dude */
	private static Vector2 DUDE_POS = new Vector2(3.5f, 5.0f);
	/** The position of the rope bridge */
	//private static Vector2 BRIDGE_POS  = new Vector2(24.0f, 1.8f);
	private static Vector2 BRIDGE_POS  = new Vector2(18.0f, 0.8f);
	/** The position of the vines */
	private static ArrayList<Vector2> VINE_POS  = new ArrayList<Vector2>(
			Arrays.asList(
//					new Vector2(18f, 17.1f),
//					new Vector2(10f, 17.1f),
//					//new Vector2(10f, 9.1f),
//					//new Vector2(1.5f, 7.9f),
//					new Vector2(5.5f, 7.9f),
//					new Vector2(14f, 7.9f),
//					new Vector2(22f, 7.9f),
//					new Vector2(26f, 14f)

					//new Vector2(18f, 17.1f),
					new Vector2(9.8f, 21.5f),
					//new Vector2(10f, 9.1f),
					//new Vector2(1.5f, 7.9f),
					new Vector2(6.5f, 7.9f),
					new Vector2(14f, 7.9f),
					new Vector2(22f, 7.9f),
					new Vector2(26f, 13.5f),
					new Vector2(15.5f, 16.0f)

			));
	/** The lengths of the vines */
	private static ArrayList<Float> VINE_LENGTH  = new ArrayList<Float>(
			Arrays.asList(
					6f, 5f, 5f, 5f, 5f, 5f

			));
	/** The position of the branches */
	private static ArrayList<Vector2> BRANCH_POS  = new ArrayList<Vector2>(
			Arrays.asList(
//					new Vector2(5f, 10.1f)
					new Vector2(5f, 10.1f),
					new Vector2(21f, 8.3f)
			));
	/** The length of the branches */
	private static ArrayList<Float> BRANCH_LENGTH  = new ArrayList<Float>(
			Arrays.asList(
					5f, 4f
			));
	/** The stiff portions of the branches */
	private static ArrayList<Float> BRANCH_STIFF_LENGTH  = new ArrayList<Float>(
			Arrays.asList(
					3f, 2f
			));

	// Physics objects for the game
	/** Reference to the character avatar */
	private DudeModel avatar;
	private static SlothModel sloth;
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public PlatformController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
		loadLevel = "";
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		playerIsReady = false;
		this.clearGrab();
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
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();

	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		if (loadLevel.equals("")) {
			// Add level goal
			float dwidth  = goalTile.getRegionWidth()/scale.x;
			float dheight = goalTile.getRegionHeight()/scale.y;
			goalDoor = new BoxObstacle(GOAL_POS.x,GOAL_POS.y,dwidth,dheight);
			goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
			goalDoor.setDensity(0.0f);
			goalDoor.setFriction(0.0f);
			goalDoor.setRestitution(0.0f);
			goalDoor.setSensor(true);
			goalDoor.setDrawScale(scale);
			goalDoor.setTexture(goalTile);
			goalDoor.setName("goal");
			addObject(goalDoor);

			String wname = "wall";
			for (int ii = 0; ii < WALLS.length; ii++) {
				PolygonObstacle obj;
				obj = new PolygonObstacle(WALLS[ii], 0, 0);
				obj.setBodyType(BodyDef.BodyType.StaticBody);
				obj.setDensity(BASIC_DENSITY);
				obj.setFriction(BASIC_FRICTION);
				obj.setRestitution(BASIC_RESTITUTION);
				obj.setDrawScale(scale);
				obj.setTexture(earthTile);
				obj.setName(wname+ii);
				addObject(obj);
			}

			String pname = "platform";
			for (int ii = 0; ii < PLATFORMS.length; ii++) {
				PolygonObstacle obj;
				obj = new PolygonObstacle(PLATFORMS[ii], 0, 0);
				obj.setBodyType(BodyDef.BodyType.StaticBody);
				obj.setDensity(BASIC_DENSITY);
				obj.setFriction(BASIC_FRICTION);
				obj.setRestitution(BASIC_RESTITUTION);
				obj.setDrawScale(scale);
				obj.setTexture(earthTile);
				obj.setName(pname+ii);
				addObject(obj);
			}

			// Create dude
//		dwidth  = avatarTexture.getRegionWidth()/scale.x;
//		dheight = avatarTexture.getRegionHeight()/scale.y;
//		avatar = new DudeModel(DUDE_POS.x, DUDE_POS.y, dwidth, dheight);
//		avatar.setDrawScale(scale);
//		avatar.setTexture(avatarTexture);
//		addObject(avatar);

			// Create rope bridge
			dwidth  = bridgeTexture.getRegionWidth()/scale.x;
			dheight = bridgeTexture.getRegionHeight()/scale.y;
			RopeBridge bridge = new RopeBridge(BRIDGE_POS.x, BRIDGE_POS.y, BRIDGE_WIDTH, dwidth, dheight);
			bridge.setTexture(bridgeTexture);
			bridge.setDrawScale(scale);
			addObject(bridge);

//		// Create branch
			Trunk trunk;
			StiffBranch branch;
			float trunklen, branchlen, branch_y, spacing, nLinks, linksize;
			dwidth  = branchTexture.getRegionWidth()/scale.x;
			dheight = branchTexture.getRegionHeight()/scale.y;
			for (int b = 0; b < BRANCH_POS.size(); b++) {
				branchlen = BRANCH_STIFF_LENGTH.get(b);
				trunklen = BRANCH_LENGTH.get(b);
				//branch = new Stiff=Branch(BRANCH_POS.get(b).x, BRANCH_POS.get(b).y, BRANCH_LENGTH.get(b), dwidth, dheight);
				trunk = new Trunk(BRANCH_POS.get(b).x, BRANCH_POS.get(b).y,trunklen, dwidth, dheight, branchlen);
				trunk.setTexture(branchTexture);
				trunk.setDrawScale(scale);
				addObject(trunk);

				linksize = trunk.getLinksize();
				spacing = trunk.spacing;
				nLinks = trunk.getnLinks();
				branch_y = BRANCH_POS.get(b).y+((nLinks-branchlen)*linksize+spacing);
				branch = new StiffBranch(BRANCH_POS.get(b).x, branch_y,branchlen,dwidth,dheight);
				branch.setTexture(branchTexture);
				branch.setDrawScale(scale);
				addObject(branch);
			}

			// Create sloth
			sloth = new SlothModel(DOLL_POS.x, DOLL_POS.y);
			sloth.setDrawScale(scale.x,scale.y);
			sloth.setPartTextures();
			addObject(sloth);
			sloth.activateSlothPhysics(world);

			// Create vine
			Vine s_vine;
			dwidth = vineTexture.getRegionWidth() / scale.x;
			dheight = vineTexture.getRegionHeight() / scale.y;
			for (int v = 0; v < VINE_POS.size(); v++) {
				System.out.println(dwidth);
				System.out.println(dheight);
				s_vine = new Vine(VINE_POS.get(v).x, VINE_POS.get(v).y, VINE_LENGTH.get(v), dwidth, dheight);
				s_vine.setTexture(vineTexture);
				s_vine.setDrawScale(scale);
				addObject(s_vine);
			}
		} else {
			JSONLoaderSaver jls = new JSONLoaderSaver();
			jls.setScale(this.scale);
			try {
				LevelModel lm = jls.loadLevel(loadLevel);
				if (lm == null) {
					lm = new LevelModel();
				}

				for (Obstacle o : lm.getEntities()) {
					addObject(o);
					if (o instanceof SlothModel) {
						sloth = (SlothModel) o;
						sloth.activateSlothPhysics(world);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
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
		InputController	the_controller = InputController.getInstance();

		//If the player pressed "A"
		if(the_controller.didPrimary()){
			return true;
		}
		//If the player pressed "X"
//		else if(the_controller.didAdvance()){
//			return true;
//		}
		//If the player pressed "B"
		else if(the_controller.didRetreat()){
			return true;
		}
		//If the player pressed "Y"
//		else if(the_controller.didDebug()){
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
			canvas.drawText("Hold R \n to start!", displayFont, 0.0f, 500.0f);
//			canvas.end();
		//}
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
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

		// Physics tiem
		// Gribby grab
		if (sloth.isLeftGrab()) {
			sloth.grabLeft(world,leftBody);
		} else {
			sloth.releaseLeft(world);
		}

		if (sloth.isRightGrab()) {
			sloth.grabRight(world,rightBody);
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

		//Draws the force lines
		//SlothModel sloth = PlatformController.getSloth();
		sloth.drawForces(canvas, camTrans);
		//sloth.drawForces(canvas.getWidth()/2,canvas.getHeight()/2);

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * But trevor uses it for something else
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		try {
			Obstacle bd1 = (Obstacle)body1.getUserData();
			Obstacle bd2 = (Obstacle)body2.getUserData();


			if (fd1 != null && fd1.equals("sloth left hand") && bd2 != avatar && (!sloth.badBodies().contains(bd2)) && (!(bd2 instanceof PolygonObstacle))) {
				System.out.println(body2);
				leftBody = body2;
			}
			if (fd1 != null && fd1.equals("sloth right hand") && bd2 != avatar && bd2 != sloth && (!sloth.badBodies().contains(bd2))&& (!(bd2 instanceof PolygonObstacle))) {
				rightBody = body2;
			}

			if (fd2 != null && fd2.equals("sloth left hand") && bd1 != avatar && bd1 != sloth && (!sloth.badBodies().contains(bd1))&& (!(bd1 instanceof PolygonObstacle))) {
				leftBody = body1;
			}
			if (fd2 != null && fd2.equals("sloth right hand") && bd1 != avatar && bd1 != sloth && (!sloth.badBodies().contains(bd1))&& (!(bd1 instanceof PolygonObstacle))) {
				rightBody = body1;
			}

			// Check for win condition
			if ((bd1 == avatar   && bd2 == goalDoor) ||
					(bd1 == goalDoor && bd2 == avatar)) {
				setComplete(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		Object bd1 = body1.getUserData();
		Object bd2 = body2.getUserData();

		if (fd1 != null && fd1.equals("sloth left hand") && body2 == leftBody && !sloth.isLeftGrab()) leftBody = null;
		if (fd2 != null && fd2.equals("sloth left hand") && body1 == leftBody && !sloth.isLeftGrab()) leftBody = null;
		if (fd1 != null && fd1.equals("sloth right hand") && body2 == rightBody && !sloth.isRightGrab()) rightBody = null;
		if (fd2 != null && fd2.equals("sloth right hand") && body1 == rightBody && !sloth.isRightGrab()) rightBody = null;
	}

	// only for forcing release on reset
	public void clearGrab(){
		leftBody = null;
		rightBody = null;
	}
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}