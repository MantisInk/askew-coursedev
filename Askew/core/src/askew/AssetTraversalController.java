package askew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import askew.playermode.WorldController;

import java.io.File;

/**
 * Traverses the asset directory to load texture and sounds into the LibGDX asset manager.
 */
public class AssetTraversalController {

    public static final String TEXTURE_DIRECTORY = "./texture";
    public static final String SOUND_DIRECTORY = "./sounds";
    private boolean preloaded;
    private boolean loaded;

    public  AssetTraversalController() {
    }

    private String escapeWindowsFiles(String fileName) {
        return fileName.replace("\\","/");
    }

    private void recurseTextures(AssetManager manager, FileHandle fileHandle) {
        if (fileHandle == null || !fileHandle.exists()) return;

        if (fileHandle.isDirectory()) {
            for (FileHandle handle : fileHandle.list()) {
                recurseTextures(manager, handle);
            }
        } else {
            String filePath = escapeWindowsFiles(fileHandle.file().toString());
            manager.load(filePath, Texture.class);
            System.out.println("[debug] loading " + filePath);
        }
    }

    public boolean preLoadEverything(AssetManager manager) {
        if (preloaded) return true;
        // Textures
        FileHandle textureHandle = Gdx.files.internal(TEXTURE_DIRECTORY);

        recurseTextures(manager, textureHandle);

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
