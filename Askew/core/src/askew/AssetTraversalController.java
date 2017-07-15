package askew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

/**
 * Traverses the asset directory to load texture and sounds into the LibGDX asset manager.
 * As it turns out, packing your assets into a jar causes them to lose their directory properties.
 * As a result, our traversal is quite limited to a flat hierarchy and we have to use a different namespacing style.
 */
class AssetTraversalController {

    private static final String TEXTURE_MANIFEST = "texture_manifest.txt";
    private boolean preloaded;

    AssetTraversalController() {
    }

    public void preLoadEverything(MantisAssetManager manager) {
        if (preloaded) return;
        // Textures
        FileHandle textureManifestHandle = Gdx.files.internal(TEXTURE_MANIFEST);
        String[] allPaths = textureManifestHandle.readString().split("\\R");
        manager.setTexturePaths(allPaths);
        for (String handleString : allPaths) {
            manager.load(handleString, Texture.class);
        }

        preloaded = true;
    }
}
