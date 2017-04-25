package askew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Traverses the asset directory to load texture and sounds into the LibGDX asset manager.
 * As it turns out, packing your assets into a jar causes them to lose their directory properties.
 * As a result, our traversal is quite limited to a flat hierarchy and we have to use a different namespacing style.
 */
public class AssetTraversalController {

    public static final String TEXTURE_MANIFEST = "texture_manifest.txt";
    public static final String SOUND_DIRECTORY = "sounds";
    private boolean preloaded;
    private boolean loaded;

    public  AssetTraversalController() {
    }

    private String escapeWindowsFiles(String fileName) {
        return fileName.replace("\\","/");
    }

    private String escapeRelativeDot(String fileName) {
        int spliceIndex = fileName.indexOf("./");
        if (spliceIndex > -1) {
            return fileName.substring(spliceIndex+2);
        }

        return fileName;
    }

    public boolean preLoadEverything(MantisAssetManager manager) {
        if (preloaded) return true;
        // Textures
        FileHandle textureManifestHandle = Gdx.files.internal(TEXTURE_MANIFEST);
        String[] allPaths = textureManifestHandle.readString().split("\\R");
        manager.setTexturePaths(allPaths);
        for (String handleString : allPaths) {
            System.err.println("[debug] " + handleString);
            manager.load(handleString, Texture.class);
        }


        // Sounds
        // TODO
        //manager.load(JUMP_FILE, Sound.class);
        //assets.add(JUMP_FILE);
        preloaded = true;

        return true;
    }

    public TextureRegion getTextureRegion(String key) {
        return null;
    }

}
