package askew.playermode;/*
 * askew.playermode.WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import askew.*;
import askew.entity.Entity;
import askew.entity.obstacle.Obstacle;
import askew.playermode.gamemode.GameModeController;
import askew.playermode.gamemode.TutorialModeController;
import askew.util.FilmStrip;
import askew.util.ScreenListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import lombok.Getter;

import java.util.ArrayList;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is 
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a askew.GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public abstract class WorldController implements Screen {
	
	/** 
	 * Tracks the asset state.  Otherwise subclasses will try to load assets 
	 */
	protected enum AssetState {
		/** No assets loaded */
		EMPTY,
		/** Still loading assets */
		LOADING,
		/** Assets are complete */
		COMPLETE
	}

	/** Track asset loading from all instances and subclasses */
	protected AssetState worldAssetState = AssetState.EMPTY;
	/** Track all loaded assets (for unloading purposes) */
	protected Array<String> assets;	
	// Pathnames to shared assets
	/** Retro font for displaying messages */
	protected boolean playingMusic;
	private static String FONT_FILE = "shared/ReginaFree.ttf";
	private static int FONT_SIZE = 56;

	/** The font for giving messages to the player */
	protected BitmapFont displayFont;

	//Loading content functions

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
		if (worldAssetState != AssetState.EMPTY) {
			return;
		}
		
		worldAssetState = AssetState.LOADING;
		// Load the font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);
	}

	/**
	 * Loads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(MantisAssetManager manager) {
		if (worldAssetState != AssetState.LOADING) {
			return;
		}

		manager.loadProcess();

		// Allocate the font
		if (manager.isLoaded(FONT_FILE)) {
			displayFont = manager.get(FONT_FILE,BitmapFont.class);
		} else {
			System.out.println("I CANT FIND THE FOOOOOOOONT");
			displayFont = null;
		}

		worldAssetState = AssetState.COMPLETE;
	}
	
	/**
	 * Returns a newly loaded filmstrip for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * the number of animation frames) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param rows 		The number of rows in the filmstrip
	 * @param cols 		The number of columns in the filmstrip
	 * @param size 		The number of frames in the filmstrip
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
		if (manager.isLoaded(file)) {
			FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
			strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			return strip;
		}
		return null;
	}
	
	/** 
	 * Unloads the assets for this game.
	 * 
	 * This method erases the static variables.  It also deletes the associated texture
	 * from the asset manager. If no assets are loaded, this method does nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void unloadContent(AssetManager manager) {
    	for(String s : assets) {
    		if (manager.isLoaded(s)) {
    			manager.unload(s);
    		}
    	}
	}
	
	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
	public static final int EXIT_MM_GM = 1;
	public static final int EXIT_MM_LE = 2;
	public static final int EXIT_GM_MM = 3;
	public static final int EXIT_GM_LE = 4;
	public static final int EXIT_LE_MM = 5;
	public static final int EXIT_LE_GM = 6;
	public static final int EXIT_GM_GM = 7;
	public static final int EXIT_MM_TL = 8;
	public static final int EXIT_TL_GM = 9;
	public static final int EXIT_TL_TL = 10;

	/** How many frames after winning/losing do we continue? */
	public static final int EXIT_COUNT = 120;

	/** The amount of time for a physics engine step. */
	public static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;
	
	/** Width of the game world in Box2d units */
	protected static final float DEFAULT_WIDTH  = 16.0f * 1.3f;
	/** Height of the game world in Box2d units */
	protected static final float DEFAULT_HEIGHT = DEFAULT_WIDTH * (9.f / 16.f);
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = -4.9f;

	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected ArrayList<Entity> objects  = new ArrayList<Entity>();
	/** Listener that will update the player mode when we are done */
	protected ScreenListener listener;

	/** The Box2D world */
	protected World world;
	/** The boundary of the world */
	@Getter
	protected Rectangle bounds;
	/** The world scale */
	protected Vector2 worldScale;

	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Whether or not debug mode is active */
	protected boolean debug;
	/** Countdown active for winning or losing */
	private int countdown;

	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug( ) {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @param value whether debug mode is active.
	 */
	public void setDebug(boolean value) {
		debug = value;
	}

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete( ) {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		failed = value;
	}

	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 *  the canvas associated with this controller
	 */
	public GameCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 *  value the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.worldScale.x = 1.3f * (float)canvas.getWidth()/(float)bounds.getWidth();
		this.worldScale.y = 1.3f * (float)canvas.getHeight()/(float)bounds.getHeight();
	}
	
	/**
	 * Creates a new game world with the default values.
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 */
	protected WorldController() {
		this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT), 
			 new Vector2(0,DEFAULT_GRAVITY));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width  	The width in Box2d coordinates
	 * @param height	The height in Box2d coordinates
	 * @param gravity	The downward gravity
	 */
	protected WorldController(float width, float height, float gravity) {
		this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds	The game bounds in Box2d coordinates
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected WorldController(Rectangle bounds, Vector2 gravity) {
		// Reload global configs
		GlobalConfiguration.update();
		assets = new Array<String>();
		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.worldScale = new Vector2(1,1);
		complete = false;
		failed = false;
		debug  = false;
		active = false;
		countdown = -1;
		playingMusic = GlobalConfiguration.getInstance().getAsBoolean("enableMusic");
		//System.out.println("SETTING SCALE IN CONSTRUCTOR");
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(Entity ent : objects) {
			if(ent instanceof Obstacle) {
				((Obstacle)ent).deactivatePhysics(world);
			}
		}
		objects.clear();
		world.dispose();
		objects = null;
		bounds = null;
		worldScale = null;
		world  = null;
		canvas = null;
	}


	/**
	 * Immediately adds the object to the physics world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Entity obj) {
		//assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		if(obj instanceof Obstacle) {
			((Obstacle)obj).activatePhysics(world);
		}
	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the physics.
	 *
	 * @param obj The object to check.
	 *
	 * @return true if the object is in bounds.
	 */
	public boolean inBounds(Obstacle obj) {
		boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
		boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
		return horiz && vert;
	}
	
	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public abstract void reset();
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 *  Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {

		InputControllerManager.getInstance().inputControllers().forEach(input->input.readInput(bounds, worldScale));

		// player 1 priority
		InputController input = InputControllerManager.getInstance().getController(0);
		if (listener == null) {
			return true;
		}

		// Toggle debug
		if (input.didRightButtonPress() || input.isGKeyPressed()) {
			debug = !debug;
		}
		
		// Handle resets
		if (input.didStartPress()) {
			if(this instanceof GameModeController)
				((GameModeController) this).pause();
			else if(this instanceof TutorialModeController)
				((TutorialModeController) this).pause();
			else
				reset();
		}

		else if (input.didBackPressed()) {
			System.out.println("quit");
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		}

			// Now it is time to maybe switch screens.
//		else if (input.didBackPressed()) {
//			System.out.println("quit");
//			listener.exitScreen(this, EXIT_QUIT);
//			return false;
//		} else if (input.didLeftButtonPress()) {
//			System.out.println("next");
//			listener.exitScreen(this, EXIT_NEXT);
//			return false;
//		} else if (input.didRightButtonPress()) {
//			System.out.println("prev");
//			listener.exitScreen(this, EXIT_PREV);
//			return false;
//		} else if (countdown > 0) {
//			System.out.println("counting down");
//			countdown--;
//		} else if (countdown == 0) {
//			if (failed) {
//				System.out.println("countdown failed");
//				reset();
//			} else if (complete) {
//				System.out.println("quitting");
//				listener.exitScreen(this, EXIT_NEXT);
//				return false;
//			}
//		}

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
	 *  delta Number of seconds since last animation frame
	 */
	public abstract void update(float dt);
	
	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 *  Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Turn the physics engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		for (Entity ent :objects){

			if(ent instanceof Obstacle){
				Obstacle obj  = (Obstacle)ent;
				if (obj.isRemoved()) {
					obj.deactivatePhysics(world);
					objects.remove(ent);
					continue;
				}
			}
			ent.update(dt); // called last!
		}
	}
	
	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 *  canvas The drawing context
	 */
	public void draw(float delta) {
		canvas.clear();
		
		canvas.begin();
		for(Entity obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();
		
		if (debug) {
			canvas.beginDebug();
			for(Entity obj : objects) {
				if( obj instanceof Obstacle) {
					((Obstacle)obj).drawDebug(canvas);
				}
			}
			canvas.endDebug();
		}
		
		// Final message
		if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
			canvas.end();
		}

		//Draws the force lines
//		SlothModel sloth = getSloth();
//		sloth.drawForces();
	}
	
	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	public void setWorldScale(GameCanvas canvas) {
		//System.out.println("IN SET SCALE");
		//System.out.println("pre set ("+scale.x+","+scale.y+")");
		this.worldScale.x = 1.0f * (float)canvas.getWidth()/(float)bounds.getWidth();
		this.worldScale.y = 1.0f * (float)canvas.getHeight()/(float)bounds.getHeight();
		//System.out.println("post set ("+scale.x+","+scale.y+")");
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				update(delta); // This is the one that must be defined.
				postUpdate(delta);
			}
			draw(delta);
		}
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

}
