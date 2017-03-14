package edu.cornell.gdiac.physics.leveleditor;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.physics.WorldController;
import edu.cornell.gdiac.util.SoundController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks every one of our assets and loads them.
 * Does this dumbly.
 */
public class FullAssetTracker {

    private static FullAssetTracker instance;
    public static final String TEXTURE_DIRECTORY = "./textures";
    public static final String SOUND_DIRECTORY = "./sounds";
    private List<String> assets;
    private List<String> textures;
    private Map<String,TextureRegion> textureLookup;
    private boolean preloaded;
    private boolean loaded;

    public static FullAssetTracker getInstance() {
        if (instance == null) {
            instance = new FullAssetTracker();
        }

        return instance;
    }

    private FullAssetTracker() {
        this.assets = new ArrayList<>();
        this.textures = new ArrayList<>();
        this.textureLookup = new HashMap<>();
    }

    public boolean preLoadEverything(AssetManager manager) {
        if (preloaded) return true;
        // Textures
        File textureDirectory = new File(TEXTURE_DIRECTORY);
        File[] textureDirectoryListing = textureDirectory.listFiles();
        if (textureDirectoryListing != null) {
            for (File child : textureDirectoryListing) {
                // Do something with child
                String textureName = child.getPath();
                textureName = textureName.substring(2);
                manager.load(textureName, Texture.class);
                assets.add(textureName);
                textures.add(textureName);
            }
        } else {
            System.err.println("Could not find texture directory!");
            return false;
        }

        // Sounds
        // TODO
        //manager.load(JUMP_FILE, Sound.class);
        //assets.add(JUMP_FILE);
        preloaded = true;

        return true;
    }

    public boolean loadEverything(WorldController wc, AssetManager manager) {
        if (loaded) return true;

        for (String texture : textures) {
            textureLookup.put(texture, wc.createTexture(manager,texture,false));
        }

        System.out.println(textureLookup.keySet());

        loaded = true;
        return true;
    }

    public TextureRegion getTextureRegion(String key) {
        return textureLookup.get(key);
    }

}
