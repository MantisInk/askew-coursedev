package askew;/*
 * askew.GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */

import askew.playermode.WorldController;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import askew.playermode.leveleditor.LevelEditorController;
import askew.playermode.gamemode.PlatformController;
import askew.playermode.gamemode.PlatformController2;
import askew.util.ScreenListener;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas; 
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Player mode for the the game proper (CONTROLLER CLASS) */
	private int current;
	/** List of all WorldControllers */
	private WorldController[] controllers;
	public static final int CON_MM = 0;
	public static final int CON_GM = 1;
	public static final int CON_LE = 2;
	public static final int CON_GM_OLD = 3;

	
	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {
		// Start loading with the asset manager
		manager = new AssetManager();
		
		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
	}

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		loading = new LoadingMode(canvas,manager,1);
		
		// Initialize the three game worlds
		controllers = new WorldController[4];
		controllers[0] = new MainMenuController();
		controllers[1] = new PlatformController2();
		controllers[2] = new LevelEditorController();
		controllers[3] = new PlatformController();
		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].setScale(canvas);
			controllers[ii].preLoadContent(manager);
		}
		current = 3;
		loading.setScreenListener(this);
		setScreen(loading);
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].unloadContent(manager);
			controllers[ii].dispose();
		}

		canvas.dispose();
		canvas = null;
	
		// Unload all of the resources
		manager.clear();
		manager.dispose();
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}


	/**
	 * MM -> game
	 * MM -> level editor
	 * game -> MM
	 * game -> LE
	 * LE -> MM
	 * LE -> game
	 * */
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			//System.out.println("loading");
			for(int ii = 0; ii < controllers.length; ii++) {
				controllers[ii].loadContent(manager);
				controllers[ii].setScreenListener(this);
				controllers[ii].setCanvas(canvas);
			}
			controllers[current].reset();
			//System.out.println("scale post reset ("+controllers[current].scale.x+","+controllers[current].scale.y+")");
			setScreen(controllers[current]);
			controllers[current].setCanvas(canvas);
			//System.out.println("scale ("+controllers[current].scale.x+","+controllers[current].scale.y+")");
			
			loading.dispose();
			loading = null;
		} else if (exitCode == WorldController.EXIT_MM_GM) {
			current = CON_GM;
			controllers[current].reset();
			setScreen(controllers[current]);

		} else if (exitCode == WorldController.EXIT_MM_LE) {
			current = CON_LE;
			controllers[current].reset();
			setScreen(controllers[current]);

		} else if (exitCode == WorldController.EXIT_MM_GM_OLD) {
			current = CON_GM_OLD;
			controllers[current].reset();
      controllers[current].setCanvas(canvas);
			setScreen(controllers[current]);

		} else if (exitCode == WorldController.EXIT_GM_MM) {
			current = CON_GM_OLD;
			controllers[current].reset();
			setScreen(controllers[current]);

		} else if (exitCode == WorldController.EXIT_GM_LE) {
			current = CON_GM_OLD;
			controllers[current].reset();
			setScreen(controllers[current]);

		} else if (exitCode == WorldController.EXIT_LE_MM) {
			current = CON_MM;
			controllers[current].reset();
			setScreen(controllers[current]);

		} else if (exitCode == WorldController.EXIT_LE_GM) {
			current = CON_GM;
			if (controllers[current] instanceof LevelEditorController) {
				LevelEditorController lec = (LevelEditorController) controllers[current];
				if (!lec.getTrialLevelName().equals("")) {
					WorldController GM = controllers[CON_GM];
					if (GM instanceof PlatformController) {
						((PlatformController) GM).setLoadLevel(lec.getTrialLevelName());
					}
				}
			}
			controllers[current].reset();
			setScreen(controllers[current]);
			controllers[current].setCanvas(canvas);
		} else if (exitCode == WorldController.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}
}