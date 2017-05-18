/*
 * DesktopLauncher.java
 * 
 * LibGDX is a cross-platform development library. You write all of your code in 
 * the core project.  However, you still need some extra classes if you want to
 * deploy on a specific platform (e.g. PC, Android, Web).  That is the purpose
 * of this class.  It deploys your game on a PC/desktop computer.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package desktop;

import askew.GDXRoot;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * The main class of the game.
 * <p>
 * This class sets the window size and launches the game.  Aside from modifying
 * the window size, you should almost never need to modify this class.
 */
class DesktopLauncher {

    /**
     * Classic main method that all Java programmers know.
     * <p>
     * This method simply exists to start a new LwjglApplication.  For desktop games,
     * LibGDX is built on top of LWJGL (this is not the case for Android).
     *
     * @param arg Command line arguments
     */
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1600;
        config.height = 900;
        config.samples = 3;
        config.resizable = false;
        config.fullscreen = false;
        config.title = "Askew";
        config.vSyncEnabled = false;
        config.addIcon("texture/icon.png", Files.FileType.Internal);
        new LwjglApplication(new GDXRoot(), config);
    }
}
