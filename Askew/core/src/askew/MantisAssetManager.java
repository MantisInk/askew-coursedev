package askew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * In addition to being the actual asset manager, this holds references to certain textures which require
 * processing on the openGL thread.
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class MantisAssetManager extends AssetManager {

    public static final String WALL_TEXTURE = "texture/wall/wall.png";
    public static final String EDGE_TEXTURE = "texture/wall/edge.png";
    public static final String THORN_TEXTURE = "texture/thorn/thorns.png";
    @Getter
    private final Map<String, TextureRegion> processedTextureMap;
    private boolean loaded;
    private boolean preloaded;
    @Getter
    @Setter
    private String[] texturePaths;
    @Getter
    private TextureAtlas textureAtlas;

    public MantisAssetManager() {
        super();
        processedTextureMap = new HashMap<>();
        textureAtlas = new TextureAtlas();
    }

    public void preloadProcess() {
        if (!preloaded) {
            load(WALL_TEXTURE, Texture.class);
            load(THORN_TEXTURE, Texture.class);
            load(EDGE_TEXTURE, Texture.class);
        }

        preloaded = true;
    }

    public void loadProcess() {
        if (!loaded) {
            createTexture(WALL_TEXTURE);
            createTexture(THORN_TEXTURE);
            createTexture(EDGE_TEXTURE);
            textureAtlas = new TextureAtlas(Gdx.files.internal("texture/packed/packed.atlas"));
        }
        loaded = true;
    }

    /**
     * (originally from WorldController)
     * Returns a newly loaded texture region for the given file.
     * <p>
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param file The texture (region) file
     */
    private void createTexture(String file) {
        TextureRegion region = new TextureRegion(get(file, Texture.class));
        region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        processedTextureMap.put(file, region);
    }
}
