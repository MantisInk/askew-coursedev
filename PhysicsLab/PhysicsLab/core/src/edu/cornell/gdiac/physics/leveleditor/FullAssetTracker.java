package edu.cornell.gdiac.physics.leveleditor;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks every one of our assets and loads them.
 * Does this dumbly.
 */
public class FullAssetTracker {

    public static final String TEXTURE_DIRECTORY = "./textures";
    public static final String SOUND_DIRECTORY = "./sounds";
    private List<String> assets;

    public FullAssetTracker() {
        this.assets = new ArrayList<String>();
    }

    public boolean loadEverything(AssetManager manager) {
        // Textures
        File textureDirectory = new File(TEXTURE_DIRECTORY);
        File[] textureDirectoryListing = textureDirectory.listFiles();
        if (textureDirectoryListing != null) {
            for (File child : textureDirectoryListing) {
                // Do something with child
                try {
                    String textureName = child.getCanonicalPath();
                    manager.load(textureName, Texture.class);
                    assets.add(textureName);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            System.err.println("Could not find texture directory!");
            return false;
        }

        // Sounds
        // TODO
        //manager.load(JUMP_FILE, Sound.class);
        //assets.add(JUMP_FILE);
        return true;
    }

}
