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
import askew.playermode.gamemode.GameModeController;
import askew.playermode.gamemode.TutorialModeController;
import askew.playermode.leveleditor.LevelEditorController;
import askew.playermode.loading.LoadingMode;
import askew.playermode.mainmenu.MainMenuController;
import askew.util.ScreenListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

/**
 * Root class for a LibGDX.
 * <p>
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However,
 * those classes are unique to each platform, while this class is the same across all
 * plaforms. In addition, this functions as the root class all intents and purposes,
 * and you would draw it as a root class in an architecture specification.
 */
public class GDXRoot extends Game implements ScreenListener {
    public static final int CON_MM = 0;
    private static final int CON_GM = 1;
    private static final int CON_LE = 2;
    private static final int CON_TL = 3;
    /**
     * AssetManager to load game assets (texture, sounds, etc.)
     */
    private final MantisAssetManager manager;
    /**
     * AssetTraversalController tells manager what to load
     */
    private final AssetTraversalController assetTraversalController;
    /**
     * Drawing context to display graphics (VIEW CLASS)
     */
    private GameCanvas canvas;
    /**
     * Player mode for the asset loading screen (CONTROLLER CLASS)
     */
    private LoadingMode loading;
    /**
     * Player mode for the the game proper (CONTROLLER CLASS)
     */
    private int current;
    /**
     * List of all WorldControllers
     */
    private WorldController[] controllers;


    /**
     * Creates a new game from the configuration settings.
     * <p>
     * This method configures the asset manager, but does not load any assets
     * or assign any screen.
     */
    public GDXRoot() {
        // Start loading with the asset manager
        manager = new MantisAssetManager();
        assetTraversalController = new AssetTraversalController();

        // Add font support to the asset manager
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
    }

    /**
     * Called when the Application is first created.
     * <p>
     * This is method immediately loads assets for the loading screen, and prepares
     * the asynchronous loader for all other assets.
     */
    public void create() {
        canvas = new GameCanvas();
        loading = new LoadingMode(canvas, manager, 1);

        // Initialize the three game worlds
        controllers = new WorldController[4];
        controllers[0] = new MainMenuController();
        controllers[1] = new GameModeController();
        controllers[2] = new LevelEditorController((GameModeController)
                controllers[1]); // pass
        // gamemode for playtesting
        controllers[3] = new TutorialModeController();
        for (WorldController controller : controllers) {
            controller.setWorldScale(canvas);
            controller.preLoadContent(manager);
        }

        assetTraversalController.preLoadEverything(manager);
        manager.preloadProcess();
        current = CON_MM;
        loading.setScreenListener(this);
        setScreen(loading);
    }

    /**
     * Called when the Application is destroyed.
     * <p>
     * This is preceded by a call to pause().
     */
    public void dispose() {
        // Call dispose on our children
        setScreen(null);
        for (WorldController controller : controllers) {
            controller.unloadContent(manager);
            controller.dispose();
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
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to create().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        canvas.resize();
        super.resize(width, height);
    }


    /**
     * MM -> game
     * MM -> level editor
     * game -> MM
     * game -> LE
     * LE -> MM
     * LE -> game
     * The given screen has made a request to exit its player mode.
     * <p>
     * The value exitCode can be used to implement menu options.
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, int exitCode) {
        if (screen == loading) {
            for (WorldController controller : controllers) {
                controller.loadContent(manager);
                controller.setScreenListener(this);
                controller.setCanvas(canvas);
            }
            current = 0;
            controllers[current].reset();
            setScreen(controllers[current]);
            controllers[current].setCanvas(canvas);

            loading.dispose();
            loading = null;
        }
        // Intentional fallthrough
        else if (exitCode == WorldController.EXIT_MM_GM) {
            current = CON_GM;
            ((GameModeController) controllers[current]).setLevel();
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_GM_GM) {
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_MM_TL) {
            current = CON_TL;
            ((TutorialModeController) controllers[current]).restart();
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_TL_GM) {
            current = CON_GM;
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_TL_TL) {
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_MM_LE) {
            current = CON_LE;
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_GM_MM) {
            current = CON_MM;
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_GM_LE) {
            current = CON_LE;
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_LE_MM) {
            current = CON_MM;
            controllers[current].reset();
            setScreen(controllers[current]);

        } else if (exitCode == WorldController.EXIT_LE_GM) {
            if (controllers[current] instanceof LevelEditorController) {
                LevelEditorController lec = (LevelEditorController) controllers[current];
                if (!lec.getCurrentLevel().equals("")) {
                    WorldController GM = controllers[CON_GM];
                    if (GM instanceof GameModeController) {
                        ((GameModeController) GM).setLoadLevel(lec.getCurrentLevel());
                    }
                }
            }
            current = CON_GM;
            controllers[current].reset();
            setScreen(controllers[current]);
            controllers[current].setCanvas(canvas);
        } else if (exitCode == WorldController.EXIT_QUIT) {
            // We quit the main application
            Gdx.app.exit();
        }
    }
}